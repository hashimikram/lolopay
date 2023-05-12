package ro.iss.lolopay.models.services.implementation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.query.Query;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.Json;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankCardTransactionsService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class BankCardTransactionsImplementation implements BankCardTransactionsService {

  private final LogService logService;

  private final DatabaseService databaseService;

  private final UtilsService utilsService;

  /** Log singleton creation moment */
  @Inject
  public BankCardTransactionsImplementation(
      DatabaseService databaseService, LogService logService, UtilsService utilsService) {

    Logger.of(this.getClass()).debug("Singleton created");
    this.databaseService = databaseService;
    this.logService = logService;
    this.utilsService = utilsService;
  }

  @Override
  public List<BankCardTransaction> getBankCardWalletTransaction(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      long startDate,
      long endDate,
      List<BankCardTransaction> providerBankCardTransactions) {

    logService.debug(requestId, "IN", "startDate", Instant.ofEpochSecond(startDate));
    logService.debug(requestId, "IN", "endDate", Instant.ofEpochSecond(endDate));
    logService.debug(requestId, "IN", "currency", bankCardWallet.getCurrency());
    logService.debug(requestId, "IN", "bankCardProviderId", bankCard.getProviderId());
    logService.debug(
        requestId, "IN", "providerBankCardTransactions size", providerBankCardTransactions.size());

    Query<BankCardTransaction> query =
        databaseService
            .getConnection(account.getId().toString())
            .createQuery(BankCardTransaction.class);

    query.and( //
        query.criteria("date").greaterThan(startDate), //
        query.criteria("date").lessThanOrEq(endDate), //
        query.criteria("currency").equal(bankCardWallet.getCurrency()), //
        query.criteria("bankCardProviderId").equal(bankCard.getProviderId()) //
        );

    logService.debug(requestId, "L", "query", query.toString());
    List<BankCardTransaction> databaseTransactions = query.asList(); //

    // log transactions from database
    databaseTransactions
        .stream()
        .forEach(p -> logService.debug(requestId, "L", "transactions from database", p.getId()));

    logService.debug(requestId, "L", "endDate", Instant.ofEpochSecond(endDate));
    providerBankCardTransactions
        .stream() //
        .filter(
            (providerBankCardTransaction) -> {
              long providerEndDate = providerBankCardTransaction.getDate();
              long banckCardEndDate =
                  bankCard.getTransactionDates().get(bankCardWallet.getCurrency()).getEndDate();
              return (providerEndDate <= endDate && providerEndDate > banckCardEndDate);
            }) //
        .forEach(
            providerBankCardTransaction -> databaseTransactions.add(providerBankCardTransaction));

    logService.debug(requestId, "L", "databaseTransactions count", databaseTransactions.size());
    return databaseTransactions;
  }

  @Override
  public TransactionDate getDatesForProvider(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      long startDate,
      long endDate) {

    logService.debug(requestId, "IN", "startDate", Instant.ofEpochSecond(startDate));
    logService.debug(requestId, "IN", "endDate", Instant.ofEpochSecond(endDate));

    // Initialise variables
    LocalDate today =
        LocalDate.now(
            Clock.fixed(Instant.ofEpochSecond(utilsService.getTimeStamp()), ZoneOffset.UTC));
    logService.debug(requestId, "L", "today", today.toString());

    // end date will always be today
    TransactionDate transactionsDate =
        new TransactionDate(
            bankCardWallet.getCurrency(),
            today.atTime(LocalTime.NOON).toEpochSecond(ZoneOffset.UTC));

    // check if property exists in the database
    Map<CurrencyISO, TransactionDate> bankCardRetrievedDates = bankCard.getTransactionDates();
    if (bankCardRetrievedDates == null) {
      bankCardRetrievedDates = new HashMap<CurrencyISO, TransactionDate>();
    }

    if (!bankCardRetrievedDates.containsKey(bankCardWallet.getCurrency())) {

      // set dates for provider: startDate = projectStartDate, endDate = today
      transactionsDate.setStartDate(ConfigFactory.load().getLong("application.projectStartDate"));

    } else {
      // we have transaction dates for this currency, this means that startDate is already
      // projectStartDate
      long bankCardEndDate = bankCardRetrievedDates.get(bankCardWallet.getCurrency()).getEndDate();

      // set dates for provider: startDate = bankCardEndDate, endDate = today
      transactionsDate.setStartDate(bankCardEndDate);
    }

    // log retrieved dates
    logService.debug(
        requestId,
        "OUT",
        "providerTransactionsDate startDate",
        Instant.ofEpochSecond(transactionsDate.getStartDate()));
    logService.debug(
        requestId,
        "OUT",
        "providerTransactionsDate endDate",
        Instant.ofEpochSecond(transactionsDate.getEndDate()));

    return transactionsDate;
  }

  @Override
  public void saveTransactionsWithOffset(
      String requestId,
      Account account,
      List<BankCardTransaction> retrievedTransactionsFromProvider,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      TransactionDate providerTransactionDate) {

    logService.debug(requestId, "IN", "start", "no params");
    logService.debug(
        requestId, "IN", "bankCardTransactions count", retrievedTransactionsFromProvider.size());

    // Initialise variables
    LocalDate today =
        LocalDate.now(
            Clock.fixed(Instant.ofEpochSecond(utilsService.getTimeStamp()), ZoneOffset.UTC));
    logService.debug(requestId, "L", "today", today.toString());

    // set dates for bankCard: startDate = projectStartDate, endDate = today - 7 days atStartOfDay
    long sevenDaysAgo = today.minusDays(7).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    logService.debug(requestId, "L", "sevedDaysAgo", Instant.ofEpochSecond(sevenDaysAgo));

    List<BankCardTransaction> saveBankCardTransactions = new ArrayList<>();
    if (bankCard.getTransactionDates() == null
        || !bankCard.getTransactionDates().containsKey(bankCardWallet.getCurrency())) {
      saveBankCardTransactions =
          retrievedTransactionsFromProvider
              .stream() //
              // save all transactions, older then seven days from provider
              .filter(p -> p.getDate() < sevenDaysAgo) //
              .collect(Collectors.toList());
    } else {
      saveBankCardTransactions =
          retrievedTransactionsFromProvider
              .stream() // save all transactions, older then seven days from provider and newer the
              // dbEndDate
              .filter(p -> p.getDate() < sevenDaysAgo) //
              .filter(
                  p ->
                      p.getDate()
                          >= bankCard
                              .getTransactionDates()
                              .get(bankCardWallet.getCurrency())
                              .getEndDate()) //
              .collect(Collectors.toList());
    }

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(saveBankCardTransactions);

    Map<CurrencyISO, TransactionDate> bankCardTransactionDates = bankCard.getTransactionDates();
    if (bankCardTransactionDates == null) {
      bankCardTransactionDates = new HashMap<>();
    }
    bankCardTransactionDates.put(
        bankCardWallet.getCurrency(),
        new TransactionDate(
            bankCardWallet.getCurrency(),
            ConfigFactory.load().getLong("application.projectStartDate"),
            sevenDaysAgo));
    bankCard.setTransactionDates(bankCardTransactionDates);

    // log save action
    logService.debug(
        requestId, "L", "transactionsSavedToDatabase count", saveBankCardTransactions.size());
    saveBankCardTransactions
        .stream()
        .forEach(
            bankCardTransaction -> {
              logService.debug(
                  requestId,
                  "L",
                  "bankCardTransaction",
                  Json.toJson(bankCardTransaction).toString());
            });
    logService.debug(requestId, "OUT", "END", "no params");
  }
}
