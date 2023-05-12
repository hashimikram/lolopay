package ro.iss.lolopay.jobs;

import javax.inject.Inject;
import javax.inject.Singleton;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.CompanyBankCardTransaction;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.services.definition.CompanyBankCardTransactionService;
import ro.iss.lolopay.models.services.definition.CompanyBankCardWalletService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.NotificationService;

@Singleton
public class PFSHooks {
  @Inject LogService logService;

  @Inject CompanyBankCardWalletService companyBankCardWalletService;

  @Inject CompanyBankCardTransactionService companyBankCardTransactionService;

  @Inject NotificationService notificationService;

  public void process(String requestId, Account account, Application application, Callback callback)
      throws GenericRestException {

    // log
    logService.debug(requestId, "IN", "requestId", requestId);
    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "application", application.getId());
    logService.debug(requestId, "IN", "parameters", callback.getParameters());

    // process pfs hook

    // check callback type
    NotificationType notificationType =
        NotificationType.valueOf(callback.getParameters().get("EventType").toString());
    logService.debug(requestId, "L", "notificationType", String.valueOf(notificationType));

    switch (notificationType) {
      case PFS_CALLBACK_COMPANY_WALLET:
        logService.debug(requestId, "L", "case", "PFS_CALLBACK_COMPANY_WALLET");
        processCompanyWallet(requestId, account, application, callback);
        break;
      case PFS_CALLBACK_TRANSACTION:
        logService.debug(requestId, "L", "case", "PFS_CALLBACK_TRANSACTION");
        processTransaction(requestId, account, application, callback);
        break;
      default:
        logService.debug(requestId, "L", "case", "DEFAULT");
        break;
    }
  }

  private void processCompanyWallet(
      String requestId, Account account, Application application, Callback callback)
      throws GenericRestException {

    // Get transaction currency
    String currency = (String) callback.getParameters().get("Currency");
    logService.debug(requestId, "L", "currency", currency);

    Double balance = new Double(0);

    if (callback.getParameters().get("Balance") != null) {
      try {
        balance = Double.parseDouble(callback.getParameters().get("Balance").toString());
        logService.debug(requestId, "L", "balance", balance);
      } catch (Exception e) {
        logService.debug(requestId, "L", "convertError", e.getMessage());
      }
    }

    // create amount object
    Amount amountBalance = new Amount();
    amountBalance.setCurrency(CurrencyISO.valueOf(currency));
    amountBalance.setValue((int) balance.doubleValue());

    // get the company wallet
    CompanyBankCardWallet companyBankCardWallet =
        companyBankCardWalletService.getCompanyBankCardWallet(
            requestId, account, CurrencyISO.valueOf(currency));
    companyBankCardWallet.setBalance(amountBalance);

    // save last balance
    companyBankCardWalletService.saveCompanyBankCardWallet(
        requestId, account, companyBankCardWallet);
    logService.debug(requestId, "L", "companyBankCardWallet", "saved");

    // create wallet transaction
    CompanyBankCardTransaction companyBankCardTransaction = new CompanyBankCardTransaction();
    companyBankCardTransaction.setCompanyBankCardWalletId(companyBankCardWallet.getId());
    companyBankCardTransaction.setCardholderId(
        (String) callback.getParameters().get("CardholderId"));
    companyBankCardTransaction.setAmount((String) callback.getParameters().get("Amount"));
    companyBankCardTransaction.setCurrency((String) callback.getParameters().get("Currency"));
    companyBankCardTransaction.setTransactionId(
        (String) callback.getParameters().get("TransactionId"));
    companyBankCardTransaction.setTrantype((String) callback.getParameters().get("Trantype"));
    companyBankCardTransaction.setBalance((String) callback.getParameters().get("Balance"));

    // save wallet transaction
    companyBankCardTransactionService.saveCompanyBankCardTransaction(
        requestId, account, companyBankCardTransaction);
    logService.debug(requestId, "L", "companyBankCardTransaction", "saved");
  }

  private void processTransaction(
      String requestId, Account account, Application application, Callback callback)
      throws GenericRestException {

    // forward notification
    notificationService.notifyClient(
        requestId,
        account,
        application,
        NotificationType.PFS_CALLBACK_TRANSACTION,
        callback.getId());
  }

  /** @param notificationService the notificationService to set */
  public void setNotificationService(NotificationService notificationService) {

    this.notificationService = notificationService;
  }
}
