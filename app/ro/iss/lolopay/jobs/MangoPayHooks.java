package ro.iss.lolopay.jobs;

import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.libs.Json;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.ExecutionType;
import ro.iss.lolopay.models.classes.PaymentType;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.services.definition.AccountSettingsService;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.models.services.definition.DepositCardService;
import ro.iss.lolopay.models.services.definition.DocumentService;
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.models.services.definition.PayOutService;
import ro.iss.lolopay.models.services.definition.RefundService;
import ro.iss.lolopay.models.services.definition.TransferService;
import ro.iss.lolopay.models.services.definition.UboService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.NotificationService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class MangoPayHooks {
  @Inject NotificationService notificationService;

  @Inject BusinessService businessService;

  @Inject PayInService transactionIncomingService;

  @Inject PayOutService transactionOutgoingService;

  @Inject RefundService transactionRefundService;

  @Inject TransferService transferService;

  @Inject WalletService walletService;

  @Inject UserService userService;

  @Inject DocumentService documentService;

  @Inject UboService uboService;

  @Inject AccountSettingsService accountSettingsService;

  @Inject ApplicationErrorService applicationErrorService;

  @Inject LogService logService;

  @Inject DepositCardService depositCardService;

  @Inject UtilsService utilsService;

  @Inject RefundService refundService;

  public void process(String requestId, Account account, Application application, Callback callback)
      throws GenericRestException {

    // log
    logService.debug(requestId, "IN", "requestId", requestId);
    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "application", application.getId());
    logService.debug(requestId, "IN", "parameters", callback.getParameters());

    // process mango hook
    HashMap<String, Object> mangoParameters = callback.getParameters();
    NotificationType incommingMangoNotification =
        NotificationType.valueOf(mangoParameters.get("EventType").toString());
    String resourceId = mangoParameters.get("RessourceId").toString();

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    switch (incommingMangoNotification) {
      case PAYIN_NORMAL_SUCCEEDED:
      case PAYIN_NORMAL_FAILED:
      case PAYIN_NORMAL_CREATED:
        processPayInHook(requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case PAYOUT_NORMAL_SUCCEEDED:
      case PAYOUT_NORMAL_FAILED:
      case PAYOUT_NORMAL_CREATED:
        processPayOutHook(requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case TRANSFER_NORMAL_SUCCEEDED:
      case TRANSFER_NORMAL_FAILED:
      case TRANSFER_NORMAL_CREATED:
        processTransferHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case PAYIN_REFUND_CREATED:
      case PAYIN_REFUND_SUCCEEDED:
      case PAYIN_REFUND_FAILED:
        processPayInRefundHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case PAYOUT_REFUND_CREATED:
      case PAYOUT_REFUND_SUCCEEDED:
      case PAYOUT_REFUND_FAILED:
        processPayOutRefundHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case TRANSFER_REFUND_SUCCEEDED:
      case TRANSFER_REFUND_FAILED:
      case TRANSFER_REFUND_CREATED:
        processTransferRefundHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case TRANSFER_SETTLEMENT_CREATED:
      case TRANSFER_SETTLEMENT_SUCCEEDED:
      case TRANSFER_SETTLEMENT_FAILED:
        processSettlementHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;

      case KYC_CREATED:
      case KYC_SUCCEEDED:
      case KYC_FAILED:
      case KYC_VALIDATION_ASKED:
      case KYC_OUTDATED:
        processDocumentsHook(
            requestId, account, application, incommingMangoNotification, resourceId);
        break;
      case USER_KYC_LIGHT:
      case USER_KYC_REGULAR:
        processKycLevelChangedHook(
            requestId, account, application, incommingMangoNotification, resourceId);

        break;
      case UBO_DECLARATION_CREATED:
      case UBO_DECLARATION_REFUSED:
      case UBO_DECLARATION_INCOMPLETE:
      case UBO_DECLARATION_VALIDATED:
      case UBO_DECLARATION_VALIDATION_ASKED:
        processUboHook(requestId, account, application, incommingMangoNotification, resourceId);
        break;

      default:
        processOtherHook(requestId, account, application, incommingMangoNotification, resourceId);
    }
  }

  /**
   * Process hook which does not have implementation - case in which the hook is just forwarded
   *
   * @param requestId
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   * @throws GenericRestException
   */
  private void processOtherHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);
    logService.debug(requestId, "L", "start", "sendAsyncNotifications");
    logService.debug(requestId, "OUT", "end", "These notification was burnt - not sent further");
    // notificationService.notifyClient(requestId, account, application, incommingMangoNotification,
    // resourceId);
  }

  /**
   * Process pay in hook, close transaction and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processPayInHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find pay in
    PayIn payIn = transactionIncomingService.getPayInByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "payIn", payIn);

    // verify pay in
    if (payIn == null) {
      logService.error(requestId, "L", "error", "Pay in not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Pay In not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if ((incommingMangoNotification == NotificationType.PAYIN_NORMAL_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.PAYIN_NORMAL_FAILED)) {
      // verify pay in status
      if (!payIn.getStatus().equals(TransactionStatus.CREATED)) {
        logService.error(
            requestId, "L", "error", "Close Pay In is called only for pay in with status CREATED");

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
        responseError.setErrorDescription(
            "Pay In status must be CREATED and is : " + payIn.getStatus());

        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);

        throw gre;
      }

      // update card validity and active fields if we have a DIRECT CARD PAYIN
      logService.info(
          requestId, "L", "payIn.getExecutionType", payIn.getExecutionType().toString());
      logService.info(requestId, "L", "payIn.getPaymentType", payIn.getPaymentType().toString());
      logService.info(requestId, "L", "payIn.getCardProviderId", payIn.getCardProviderId());

      if (payIn.getExecutionType().equals(ExecutionType.DIRECT)
          && payIn.getPaymentType().equals(PaymentType.CARD)
          && payIn.getCardProviderId() != null) {
        // get deposit card from database
        DepositCard databaseDepositCard =
            depositCardService.getDepositCardByProviderId(
                requestId, account, payIn.getCardProviderId());
        logService.info(
            requestId,
            "L",
            "databaseDepositCard",
            utilsService.prettyPrintObject(databaseDepositCard));

        // get the same deposit card from provider
        DepositCard providerDepositCard =
            businessService.getProviderDepositCard(requestId, account, payIn.getCardProviderId());
        logService.info(
            requestId,
            "L",
            "providerDepositCard",
            utilsService.prettyPrintObject(providerDepositCard));

        // update validity and active fields if necessary
        depositCardService.updateDepositCardValidity(
            requestId, account, application, databaseDepositCard, providerDepositCard);
      }

      Transfer payInFee = null;
      Wallet accountWallet = null;

      // check for fee and retrieve the fee transaction and company wallet
      if (payIn.getRelatedTransactionId() != null && !payIn.getRelatedTransactionId().equals("")) {
        payInFee = transferService.getTransfer(requestId, account, payIn.getRelatedTransactionId());
        accountWallet =
            accountSettingsService.getAccountWallet(
                requestId, account, payInFee.getAmount().getCurrency());
      }

      // Get credit wallet
      Wallet creditWallet =
          walletService.getWallet(requestId, account, payIn.getCreditedWalletId());

      // log process start
      logService.info(requestId, "L", "process", "start close Pay In process");

      // start process
      businessService.closePayIn(
          requestId, account, application, payIn, payInFee, creditWallet, accountWallet);

    } else {
      logService.info(requestId, "L", "process", "sendAsyncNotifications");
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.PAYIN_NORMAL_CREATED,
          payIn.getId().toString());
    }
  }

  /**
   * Process transfer, close transaction and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processTransferHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find transaction
    Transfer transfer = transferService.getTransferByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "transfer", transfer);

    // test transaction
    if (transfer == null) {
      logService.error(requestId, "L", "error", "Transfer not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Transfer not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if ((incommingMangoNotification == NotificationType.TRANSFER_NORMAL_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.TRANSFER_NORMAL_FAILED)) {
      // check transaction status
      if (!transfer.getStatus().equals(TransactionStatus.CREATED)) {
        logService.error(
            requestId,
            "L",
            "error",
            "Close Transfer is called only for transfer with status CREATED");

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
        responseError.setErrorDescription(
            "Transfer status must be CREATED and is : " + transfer.getStatus());

        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);

        throw gre;
      }

      Transfer transactionFee = null;
      Wallet accountWallet = null;

      // check for fee and retrieve the fee transaction and company wallet
      if (transfer.getRelatedTransactionId() != null
          && !transfer.getRelatedTransactionId().equals("")) {
        transactionFee =
            transferService.getTransfer(requestId, account, transfer.getRelatedTransactionId());
        accountWallet =
            accountSettingsService.getAccountWallet(
                requestId, account, transfer.getAmount().getCurrency());
      }

      // get debit user
      User debitUser = userService.getUser(requestId, account, transfer.getDebitedUserId());

      // get credit user
      User creditUser = userService.getUser(requestId, account, transfer.getCreditedUserId());

      // get debit wallet
      Wallet debitWallet =
          walletService.getWallet(requestId, account, transfer.getDebitedWalletId());

      // get credit wallet
      Wallet creditWallet =
          walletService.getWallet(requestId, account, transfer.getCreditedWalletId());

      // log process start
      logService.info(requestId, "L", "process", "start close Transfer process");

      // start process
      businessService.closeTransfer(
          requestId,
          account,
          application,
          transfer,
          transactionFee,
          debitUser,
          debitWallet,
          creditUser,
          creditWallet,
          accountWallet);
    } else {
      logService.info(requestId, "L", "process", "sendAsyncNotifications");

      // forward notification
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.TRANSFER_NORMAL_CREATED,
          transfer.getId().toString());
    }
  }

  /**
   * Process refund transfer, close transaction and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processTransferRefundHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find refund
    Refund refund = transactionRefundService.getRefundByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "refund", refund);

    // test refund
    if (refund == null) {
      logService.error(requestId, "L", "error", "Refund not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Refund not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if ((incommingMangoNotification == NotificationType.TRANSFER_REFUND_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.TRANSFER_REFUND_FAILED)) {
      // check refund status
      if (!refund.getStatus().equals(TransactionStatus.CREATED)) {
        logService.error(
            requestId, "L", "error", "Close Refund is called only for refunds with status CREATED");

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
        responseError.setErrorDescription(
            "Refund status must be CREATED and is : " + refund.getStatus());

        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);

        throw gre;
      }

      Transfer refundFee = null;
      Wallet accountWallet = null;

      // check for fee and retrieve the fee transaction and company wallet
      if (refund.getRelatedTransactionId() != null
          && !refund.getRelatedTransactionId().equals("")) {
        refundFee =
            transferService.getTransfer(requestId, account, refund.getRelatedTransactionId());
        accountWallet =
            accountSettingsService.getAccountWallet(
                requestId, account, refund.getAmount().getCurrency());
      }

      // get debit user - original credit user
      User originalCreditUser = userService.getUser(requestId, account, refund.getDebitedUserId());

      // get credit user - original debit user
      User originalDebitUser = userService.getUser(requestId, account, refund.getCreditedUserId());

      // get debit wallet - original credit wallet
      Wallet originalCreditWallet =
          walletService.getWallet(requestId, account, refund.getDebitedWalletId());

      // get credit wallet - original debit wallet
      Wallet originalDebitWallet =
          walletService.getWallet(requestId, account, refund.getCreditedWalletId());

      // log process start
      logService.info(requestId, "L", "process", "start close Refund process");

      // start process
      businessService.closeRefund(
          requestId,
          account,
          application,
          refund,
          refundFee,
          originalCreditUser,
          originalCreditWallet,
          originalDebitUser,
          originalDebitWallet,
          accountWallet);
    } else {
      logService.info(requestId, "L", "process", "sendAsyncNotifications");

      // forward notification
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.TRANSFER_REFUND_CREATED,
          refund.getId().toString());
    }
  }

  private void processSettlementHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find refund
    Transfer settlement = transferService.getTransfer(requestId, account, resourceId);
    logService.debug(requestId, "L", "settlement", settlement);

    // test refund
    if ((settlement != null)
        && (settlement.getStatus().equals(TransactionStatus.SUCCEEDED)
            || settlement.getStatus().equals(TransactionStatus.FAILED))) {
      logService.error(requestId, "L", "error", "Settlement already processed");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
      responseError.setErrorDescription(
          "Settlement already processed for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if (incommingMangoNotification == NotificationType.TRANSFER_SETTLEMENT_SUCCEEDED) {
      // log process start
      logService.info(requestId, "L", "process", "start register settlement process");

      // start process
      businessService.registerSettlement(requestId, account, application, resourceId);
    } else {
      logService.info(requestId, "L", "process", "ignore settlement created or failed");
    }
  }

  /**
   * Process a pay out refund which is actually a refund for bank withdrawal
   *
   * @param requestId
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   * @throws GenericRestException
   */
  private void processPayOutRefundHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find refund
    PayIn withdrawRefund = transactionIncomingService.getPayIn(requestId, account, resourceId);
    logService.debug(requestId, "L", "withdrawRefund", withdrawRefund);

    // test refund
    if ((withdrawRefund != null)
        && (withdrawRefund.getStatus().equals(TransactionStatus.SUCCEEDED)
            || withdrawRefund.getStatus().equals(TransactionStatus.FAILED))) {
      logService.error(requestId, "L", "error", "Pay Out Refund already processed");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
      responseError.setErrorDescription(
          "Pay Out Refund already processed for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if (incommingMangoNotification == NotificationType.PAYOUT_REFUND_SUCCEEDED) {
      // log process start
      logService.info(requestId, "L", "process", "start register deposit refund process");

      // start process
      businessService.registerWithdrawRefund(requestId, account, application, resourceId);
    } else {
      logService.info(requestId, "L", "process", "ignore withdraw refund created or failed");
    }
  }

  /**
   * Process a pay in refund, which is actually a refund to a card deposit
   *
   * @param requestId
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   * @throws GenericRestException
   */
  private void processPayInRefundHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find refund
    Refund payInRefund = refundService.getRefundByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "withdrawRefund", Json.toJson(payInRefund));

    // test refund
    if (payInRefund == null) {
      logService.error(requestId, "L", "error", "PayInRefund not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("PayInRefund not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if ((incommingMangoNotification == NotificationType.PAYIN_REFUND_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.PAYIN_REFUND_FAILED)) {
      // check PayInRefund status
      if (!payInRefund.getStatus().equals(TransactionStatus.CREATED)) {
        logService.error(
            requestId,
            "L",
            "error",
            "Close PayInRefund is called only for transfer with status CREATED");

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
        responseError.setErrorDescription(
            "PayInRefund status must be CREATED and is : " + payInRefund.getStatus());

        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);

        throw gre;
      }
      // log process start
      logService.info(requestId, "L", "process", "start register PayInRefund process");

      Transfer refundFee = null;
      Wallet accountWallet = null;

      // check for fee and retrieve the fee transaction and company wallet
      if (payInRefund.getRelatedTransactionId() != null
          && !payInRefund.getRelatedTransactionId().equals("")) {
        refundFee =
            transferService.getTransfer(requestId, account, payInRefund.getRelatedTransactionId());
        accountWallet =
            accountSettingsService.getAccountWallet(
                requestId, account, payInRefund.getAmount().getCurrency());
      }

      // get user wallet
      Wallet debitedUserWallet =
          walletService.getWallet(
              requestId,
              account,
              payInRefund.getDebitedUserId(),
              payInRefund.getAmount().getCurrency());
      logService.debug(
          requestId, "L", "debitedUserWallet", Json.toJson(debitedUserWallet).toString());

      // start process
      businessService.closeDepositRefund(
          requestId,
          account,
          application,
          resourceId,
          debitedUserWallet,
          payInRefund,
          refundFee,
          accountWallet);
    } else {
      logService.info(requestId, "L", "process", "sendAsyncNotifications");

      // forward notification
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.PAYIN_REFUND_CREATED,
          payInRefund.getId().toString());
    }
  }

  /**
   * Process pay out, close transaction and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processPayOutHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // find transaction
    PayOut payOut =
        transactionOutgoingService.getPayOutByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "payOut", payOut);

    // test transaction
    if (payOut == null) {
      logService.error(requestId, "L", "error", "Pay out not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Pay Out not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    if ((incommingMangoNotification == NotificationType.PAYOUT_NORMAL_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.PAYOUT_NORMAL_FAILED)) {
      // check transaction status
      if (!payOut.getStatus().equals(TransactionStatus.CREATED)) {
        logService.error(
            requestId,
            "L",
            "error",
            "Close Pay Out is called only for pay out with status CREATED");

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_RECORD_STATUS);
        responseError.setErrorDescription(
            "Pay out status must be CREATED and is : " + payOut.getStatus());

        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);

        throw gre;
      }

      Transfer transactionFee = null;
      Wallet accountWallet = null;

      // check for fee and retrieve the fee transaction and company wallet
      if (payOut.getRelatedTransactionId() != null
          && !payOut.getRelatedTransactionId().equals("")) {
        transactionFee =
            transferService.getTransfer(requestId, account, payOut.getRelatedTransactionId());
        accountWallet =
            accountSettingsService.getAccountWallet(
                requestId, account, payOut.getAmount().getCurrency());
      }

      // get debit user
      User debitUser = userService.getUser(requestId, account, payOut.getDebitedUserId());

      // get debit wallet
      Wallet debitWallet = walletService.getWallet(requestId, account, payOut.getDebitedWalletId());

      // log process start
      logService.info(requestId, "L", "process", "start close Pay Out process");

      // start process
      businessService.closePayOut(
          requestId,
          account,
          application,
          payOut,
          transactionFee,
          debitUser,
          debitWallet,
          accountWallet);
    } else {
      logService.info(requestId, "L", "process", "sendAsyncNotifications");

      // forward notification
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.PAYOUT_NORMAL_CREATED,
          payOut.getId().toString());
    }
  }

  /**
   * Process an update to user KYC and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processKycLevelChangedHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    User existingUser = userService.getUserByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "existingUser", existingUser);

    // test document
    if (existingUser == null) {
      logService.error(requestId, "L", "error", "User not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("User not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    businessService.updateUserKyc(requestId, account, application, existingUser);
    logService.info(requestId, "L", "process", "sendAsyncNotifications");

    // forward notification
    notificationService.notifyClient(
        requestId,
        account,
        application,
        incommingMangoNotification,
        existingUser.getId().toString());
  }

  /**
   * Process document, update user KYC and forward the notification to client
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processDocumentsHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // identify document in the system
    Document existingDocument =
        documentService.getDocumentByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "existingDocument", existingDocument);

    // test document
    if (existingDocument == null) {
      logService.error(requestId, "L", "error", "Document not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Document not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    // identify owner of document in the system
    User documentOwner = userService.getUser(requestId, account, existingDocument.getUserId());

    // check to see if is the case to update user KYC level
    if ((incommingMangoNotification == NotificationType.KYC_SUCCEEDED)
        || (incommingMangoNotification == NotificationType.KYC_FAILED)
        || (incommingMangoNotification == NotificationType.KYC_OUTDATED)) {

      // log process start
      logService.info(requestId, "L", "process", "start update User Kyc process");

      // check new user kyc status and update current local status if required
      businessService.updateUserKyc(requestId, account, application, documentOwner);

      // log process start
      logService.info(requestId, "L", "process", "start update Document Status process");

      // check document status and update local if required
      businessService.updateDocumentStatus(
          requestId, account, application, documentOwner, existingDocument);
    }

    logService.info(requestId, "L", "process", "sendAsyncNotifications");

    // forward notification
    notificationService.notifyClient(
        requestId,
        account,
        application,
        incommingMangoNotification,
        existingDocument.getId().toString());
  }

  /**
   * Process Ubo declaration
   *
   * @param account
   * @param application
   * @param incommingMangoNotification
   * @param resourceId
   */
  private void processUboHook(
      String requestId,
      Account account,
      Application application,
      NotificationType incommingMangoNotification,
      String resourceId)
      throws GenericRestException {

    logService.debug(requestId, "IN", "incommingMangoNotification", incommingMangoNotification);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // identify ubo declaration in the system
    UboDeclaration existingUboDeclaration =
        uboService.getUboDeclarationByProviderId(requestId, account, resourceId);
    logService.debug(requestId, "L", "existingDocument", existingUboDeclaration);

    // test document
    if (existingUboDeclaration == null) {
      logService.error(requestId, "L", "error", "Document not found");

      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_RECORD_NOT_FOUND);
      responseError.setErrorDescription("Document not found for providerId : " + resourceId);

      GenericRestException gre = new GenericRestException();
      gre.addResponseError(responseError);

      throw gre;
    }

    // identify owner of ubo declaration in the system
    User uboDeclarationOwner =
        userService.getUser(requestId, account, existingUboDeclaration.getUserId());

    logService.debug(requestId, "L", "uboDeclarationOwner", uboDeclarationOwner);

    // check to see if is the case to update the Ubo declaration status
    if ((incommingMangoNotification == NotificationType.UBO_DECLARATION_VALIDATED)
        || (incommingMangoNotification == NotificationType.UBO_DECLARATION_REFUSED
            || incommingMangoNotification == NotificationType.UBO_DECLARATION_INCOMPLETE)) {

      // log process start
      logService.info(requestId, "L", "process", "start update Ubo declaration status");

      // log process start
      logService.info(requestId, "L", "process", "start update Document Status process");

      // check Ubo declaration status and update local if required
      businessService.updateUboDeclarationStatus(
          requestId, account, application, uboDeclarationOwner, existingUboDeclaration);
    }

    logService.info(requestId, "L", "process", "sendAsyncNotifications");

    // forward notification
    notificationService.notifyClient(
        requestId,
        account,
        application,
        incommingMangoNotification,
        existingUboDeclaration.getId().toString());
  }

  /** @param notificationService the notificationService to set */
  public void setNotificationService(NotificationService notificationService) {

    this.notificationService = notificationService;
  }
}
