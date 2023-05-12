package ro.iss.lolopay.programs;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mongodb.morphia.Datastore;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.typesafe.config.ConfigFactory;
import play.Application;
import play.inject.Injector;
import play.inject.guice.GuiceApplicationBuilder;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.jobs.JobsModule;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.programs.classes.WithApplicationForPrograms;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.ProviderService;
import ro.iss.lolopay.services.definition.UtilsService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;
import ro.iss.lolopay.services.implementation.PFSImplementation;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

public class InitAddTerminalInfoForBankCardTransactions extends WithApplicationForPrograms {
  private LogService logService;

  private DatabaseService databaseService;

  private Datastore datastore;

  private Account account;

  private ProviderService providerService;

  private UtilsService utilsService;

  /** Provides a Play application, used for injecting the needed services */
  @Override
  protected Application provideApplication() {

    return new GuiceApplicationBuilder().disable(JobsModule.class).build();
  }

  private void initServices() {

    // get application injector
    Injector applicationEnvironmentInjector = app.injector();

    // instantiate database service
    logService = applicationEnvironmentInjector.instanceOf(LogImplementation.class);
    databaseService = applicationEnvironmentInjector.instanceOf(DatabaseImplementation.class);
    // logService = new LogImplementation();

    // databaseService = new DatabaseImplementation(logService);
    account = databaseService.getMainConnection().createQuery(Account.class).asList().get(0);
    datastore = databaseService.getConnection(account.getId().toString());

    // instantiate other services
    utilsService = applicationEnvironmentInjector.instanceOf(UtilsImplementation.class);

    providerService = applicationEnvironmentInjector.instanceOf(PFSImplementation.class);
  }

  private void addTerminalInfoForBankCardTransactions(String requestId) {

    ro.iss.lolopay.models.database.Application application =
        new ro.iss.lolopay.models.database.Application();
    application.setApplicationName("M3 Service Application");
    application.setApplicationEmail("contact@moneymail.me");

    DBCollection bankTransactionsCollection = datastore.getCollection(BankCardTransaction.class);
    @SuppressWarnings("unchecked")
    List<String> cardProviderIdsInBankCardTransactions =
        bankTransactionsCollection.distinct("bankCardProviderId");
    int noTransactionsUpdated = 0;
    for (String providerId : cardProviderIdsInBankCardTransactions) {
      BankCard bankCard = getBankCardByProviderId(requestId, providerId);
      for (String currencyString : bankCard.getCurrencies()) {
        CurrencyISO currency = CurrencyISO.valueOf(currencyString);
        try {
          TransactionDate transactionDate = getTransactionDateUntilEndOfCurrentDay(currency);
          List<BankCardTransaction> bankCardTransactionsFromProvider =
              getProviderBankCardWalletTransactions(requestId, account, bankCard, transactionDate);
          for (BankCardTransaction transactionFromProvider : bankCardTransactionsFromProvider) {
            updateBankTransactionInDb(requestId, application, transactionFromProvider);
            noTransactionsUpdated++;
          }
          // wait 2 seconds between in order not to stress the provider's server
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          logService.error(requestId, "L", "InterruptedException", e.getMessage());
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        } catch (GenericRestException gre) {
          logService.error(
              requestId, "L", "GenericRestException", getErrorResponse(requestId, gre));
        }
      }
    }
    logService.info(requestId, "L", "No of bankCardTransactions updated", noTransactionsUpdated);
  }

  private TransactionDate getTransactionDateUntilEndOfCurrentDay(CurrencyISO currency) {

    LocalDate today =
        LocalDate.now(
            Clock.fixed(Instant.ofEpochSecond(utilsService.getTimeStamp()), ZoneOffset.UTC));
    long startDate = ConfigFactory.load().getLong("application.projectStartDate");
    long endDate = today.atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
    TransactionDate transactionDate = new TransactionDate(currency, startDate, endDate);
    return transactionDate;
  }

  private BankCard getBankCardByProviderId(String requestId, String providerId) {

    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    BankCard bankCard =
        (BankCard) databaseService.getRecord(requestId, account, filters, BankCard.class);
    return bankCard;
  }

  private void updateBankTransactionInDb(
      String requestId,
      ro.iss.lolopay.models.database.Application application,
      BankCardTransaction transactionFromProvider) {

    Map<String, Object> fieldsToUpdate = new HashMap<String, Object>();
    fieldsToUpdate.put("originalAmount", transactionFromProvider.getOriginalAmount());
    fieldsToUpdate.put("originalCurrency", transactionFromProvider.getOriginalCurrency());
    fieldsToUpdate.put("terminalName", transactionFromProvider.getTerminalName());
    fieldsToUpdate.put("terminalCity", transactionFromProvider.getTerminalCity());
    fieldsToUpdate.put("terminalCountry", transactionFromProvider.getTerminalCountry());
    fieldsToUpdate.put(
        "transactionStatus", transactionFromProvider.getTransactionStatus().toString());

    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("transactionId", transactionFromProvider.getTransactionId());

    databaseService.updateRecord(
        requestId,
        account,
        application,
        filters,
        fieldsToUpdate,
        WriteConcern.UNACKNOWLEDGED,
        BankCardTransaction.class);
  }

  @SuppressWarnings("unchecked")
  private List<BankCardTransaction> getProviderBankCardWalletTransactions(
      String requestId, Account account, BankCard bankCard, TransactionDate transactionDate) {

    logService.debug(requestId, "L", "requestId", requestId);

    // get records from provider
    ProviderResponse providerResponse =
        providerService.getProviderBankCardWalletTransaction(requestId, bankCard, transactionDate);

    // init response
    List<BankCardTransaction> responseBankCardTransactions = new ArrayList<BankCardTransaction>();

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {

      // get transaction list from provider response
      responseBankCardTransactions =
          (List<BankCardTransaction>) providerResponse.getProviderData("listBankCardTransactions");

      // return list
      return responseBankCardTransactions;
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // log retrieved dates and return
    responseBankCardTransactions
        .stream()
        .forEach(p -> logService.debug(requestId, "L", "responseBankCardTransactions", p.getId()));
    return responseBankCardTransactions;
  }

  private String getErrorResponse(String requestId, GenericRestException gre) {

    StringBuffer sb = new StringBuffer();
    sb.append("Request Id:");
    sb.append(requestId);
    sb.append(System.lineSeparator());

    for (ResponseError responseError : gre.getResponseErrors()) {
      // log error
      logService.debug(requestId, "IN", "errors.code", responseError.getErrorCode());
      logService.debug(requestId, "IN", "errors.description", responseError.getErrorDescription());

      // add to email content
      sb.append("Error Key:");
      sb.append(responseError.getErrorCode());
      sb.append(" - ");
      sb.append(responseError.getErrorDescription());
      sb.append(System.lineSeparator());
    }

    sb.append("Error stack:");
    sb.append(ExceptionUtils.getStackTrace(gre));

    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));
    return sb.toString();
  }

  public static void main(String[] args) {

    String requestId = "1234";

    InitAddTerminalInfoForBankCardTransactions programClass =
        new InitAddTerminalInfoForBankCardTransactions();
    try {
      // start the Play application and inject services
      programClass.startPlay();
      programClass.initServices();

      programClass.addTerminalInfoForBankCardTransactions(requestId);
    } finally {
      // stop the Play application
      programClass.stopPlay();
    }
  }
}
