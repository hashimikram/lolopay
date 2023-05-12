package ro.iss.lolopay.controllers;

import java.util.List;
import javax.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.FeeModel;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankAccountService;
import ro.iss.lolopay.models.services.definition.DepositCardService;
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.models.services.definition.PayOutService;
import ro.iss.lolopay.models.services.definition.RefundService;
import ro.iss.lolopay.models.services.definition.TransactionService;
import ro.iss.lolopay.models.services.definition.TransferService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.requests.RequestCreateAVSDirectPayIn;
import ro.iss.lolopay.requests.RequestCreateDirectPayIn;
import ro.iss.lolopay.requests.RequestCreatePayIn;
import ro.iss.lolopay.requests.RequestCreatePayOut;
import ro.iss.lolopay.requests.RequestCreateTransfer;
import ro.iss.lolopay.requests.RequestRefundPayIn;
import ro.iss.lolopay.requests.RequestRefundTransfer;
import ro.iss.lolopay.responses.ResponseGetPayInStatusByProviderId;
import ro.iss.lolopay.responses.ResponseTransaction;
import ro.iss.lolopay.responses.ResponseTransactions;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;

public class TransactionController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject WalletService walletService;

  @Inject BankAccountService bankAccountService;

  @Inject UserService userService;

  @Inject TransactionService transactionService;

  @Inject TransferService transferService;

  @Inject PayOutService transactionOutgoingService;

  @Inject RefundService transactionRefundService;

  @Inject BusinessService businessService;

  @Inject LogService logService;

  @Inject DepositCardService depositCardService;

  @Inject PayInService payInService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createDirectPayIn(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/create/direct/payin");

    // Move json to object
    Form<RequestCreateDirectPayIn> restForm =
        formFactory.form(RequestCreateDirectPayIn.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateDirectPayIn requestCreateDirectPayIn = restForm.value().get();

    // check pay in currencies from request
    if (!requestCreateDirectPayIn
        .getAmount()
        .getCurrency()
        .equals(requestCreateDirectPayIn.getFees().getCurrency())) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_MISSMATCH, requestId);
    }

    // check pay in amounts from request
    if (requestCreateDirectPayIn.getAmount().getValue() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AMOUNT_INVALID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_AMOUNT_INVALID, requestId);
    }

    // check pay in amounts from request
    if (requestCreateDirectPayIn.getAmount().getValue()
        <= requestCreateDirectPayIn.getFees().getValue()) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get credit wallet
    Wallet creditWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreateDirectPayIn.getCreditedWalletId());

    // validate credit wallet
    if (creditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_INEXISTENT, requestId);
    }

    // check credit wallet is registered to provider
    if ((creditWallet.getProviderId() == null) || (creditWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if request transfer currency is the same as credited wallet
    // currency
    if (!creditWallet
        .getCurrency()
        .toString()
        .equals(requestCreateDirectPayIn.getAmount().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_INVALID, requestId);
    }

    // retrieve credit wallet user
    User creditUser = userService.getUser(requestId, sessionAccount, creditWallet.getUserId());

    // validate credit wallet user
    if (creditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_INEXISTENT, requestId);
    }

    // check credit user is registered to provider
    if ((creditUser.getProviderId() == null) || (creditUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve deposit card
    DepositCard depositCard =
        depositCardService.getDepositCardById(
            requestId, sessionAccount, requestCreateDirectPayIn.getCardId());
    if (depositCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEPOSITCARD_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEPOSITCARD_INEXISTENT, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // start transaction process
    List<Transaction> involvedTransactions =
        businessService.createDirectPayIn(
            requestId,
            sessionAccount,
            sessionApplication,
            depositCard,
            creditUser,
            creditWallet,
            requestCreateDirectPayIn);

    // return new transactions
    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions(involvedTransactions);

    return coreService.getResponse(responseTransactions, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createAVSDirectPayIn(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/create/direct/payin");

    // Move json to object
    Form<RequestCreateAVSDirectPayIn> restForm =
        formFactory.form(RequestCreateAVSDirectPayIn.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateAVSDirectPayIn requestCreateAVSDirectPayIn = restForm.value().get();

    // check pay in currencies from request
    if (!requestCreateAVSDirectPayIn
        .getAmount()
        .getCurrency()
        .equals(requestCreateAVSDirectPayIn.getFees().getCurrency())) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_MISSMATCH, requestId);
    }

    // check pay in amounts from request
    if (requestCreateAVSDirectPayIn.getAmount().getValue() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AMOUNT_INVALID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_AMOUNT_INVALID, requestId);
    }

    // check pay in amounts from request
    if (requestCreateAVSDirectPayIn.getAmount().getValue()
        <= requestCreateAVSDirectPayIn.getFees().getValue()) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get credit wallet
    Wallet creditWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreateAVSDirectPayIn.getCreditedWalletId());

    // validate credit wallet
    if (creditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_INEXISTENT, requestId);
    }

    // check credit wallet is registered to provider
    if ((creditWallet.getProviderId() == null) || (creditWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if request transfer currency is the same as credited wallet
    // currency
    if (!creditWallet
        .getCurrency()
        .toString()
        .equals(requestCreateAVSDirectPayIn.getAmount().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_INVALID, requestId);
    }

    // retrieve credit wallet user
    User creditUser = userService.getUser(requestId, sessionAccount, creditWallet.getUserId());

    // validate credit wallet user
    if (creditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_INEXISTENT, requestId);
    }

    // check credit user is registered to provider
    if ((creditUser.getProviderId() == null) || (creditUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve deposit card
    DepositCard depositCard =
        depositCardService.getDepositCardById(
            requestId, sessionAccount, requestCreateAVSDirectPayIn.getCardId());
    if (depositCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEPOSITCARD_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEPOSITCARD_INEXISTENT, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // start transaction process
    List<Transaction> involvedTransactions =
        businessService.createAVSDirectPayIn(
            requestId,
            sessionAccount,
            sessionApplication,
            depositCard,
            creditUser,
            creditWallet,
            requestCreateAVSDirectPayIn);

    // return new transactions
    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions(involvedTransactions);

    return coreService.getResponse(responseTransactions, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createPayIn(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/create/payin");

    // Move json to object
    Form<RequestCreatePayIn> restForm =
        formFactory.form(RequestCreatePayIn.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreatePayIn requestCreatePayIn = restForm.value().get();

    // check pay in currencies from request
    if (!requestCreatePayIn
        .getAmount()
        .getCurrency()
        .equals(requestCreatePayIn.getFees().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_MISSMATCH, requestId);
    }

    // check pay in amounts from request
    if (requestCreatePayIn.getAmount().getValue() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AMOUNT_INVALID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_AMOUNT_INVALID, requestId);
    }

    // check pay in amounts from request
    if (requestCreatePayIn.getAmount().getValue() <= requestCreatePayIn.getFees().getValue()) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get credit wallet
    Wallet creditWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreatePayIn.getCreditedWalletId());

    // validate credit wallet
    if (creditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLET_INEXISTENT, requestId);
    }

    // check credit wallet is registered to provider
    if ((creditWallet.getProviderId() == null) || (creditWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if request transfer currency is the same as credited wallet
    // currency
    if (!creditWallet
        .getCurrency()
        .toString()
        .equals(requestCreatePayIn.getAmount().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_INVALID, requestId);
    }

    // retrieve credit wallet user
    User creditUser = userService.getUser(requestId, sessionAccount, creditWallet.getUserId());

    // validate credit wallet user
    if (creditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYIN_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_CREDITUSER_INEXISTENT, requestId);
    }

    // check credit user is registered to provider
    if ((creditUser.getProviderId() == null) || (creditUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYIN_CREDITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // start transaction process
    List<Transaction> involvedTransactions =
        businessService.createPayIn(
            requestId,
            sessionAccount,
            sessionApplication,
            creditUser,
            creditWallet,
            requestCreatePayIn.getAmount(),
            requestCreatePayIn.getFees(),
            requestCreatePayIn.getFeeModel(),
            requestCreatePayIn.getCustomTag(),
            requestCreatePayIn.getReturnURL(),
            requestCreatePayIn.getCardType(),
            requestCreatePayIn.getSecureMode(),
            requestCreatePayIn.getCulture(),
            requestCreatePayIn.getTemplateURL(),
            requestCreatePayIn.getStatementDescriptor());

    // return new transactions
    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions(involvedTransactions);

    return coreService.getResponse(responseTransactions, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createTransfer(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/create/transfer");

    // Move json to object
    Form<RequestCreateTransfer> restForm =
        formFactory.form(RequestCreateTransfer.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateTransfer requestCreateTransfer = restForm.value().get();

    // check transfer currencies from request
    if (!requestCreateTransfer
        .getAmount()
        .getCurrency()
        .equals(requestCreateTransfer.getFees().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_MISSMATCH, requestId);
    }

    // check transfer amounts from wallet to the same wallet - not allowed
    // of course
    if (requestCreateTransfer
        .getCreditedWalletId()
        .equals(requestCreateTransfer.getDebitedWalletId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_CREDIT_DEBIT_EQUAL);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_CREDIT_DEBIT_EQUAL, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get debit wallet
    Wallet debitWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreateTransfer.getDebitedWalletId());

    // validate debit wallet
    if (debitWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_INEXISTENT, requestId);
    }

    // check credit wallet is registered to provider
    if ((debitWallet.getProviderId() == null) || (debitWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve debit wallet user
    User debitUser = userService.getUser(requestId, sessionAccount, debitWallet.getUserId());

    // validate debit wallet user
    if (debitUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_DEBITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_DEBITUSER_INEXISTENT, requestId);
    }

    // check debit user is registered to provider
    if ((debitUser.getProviderId() == null) || (debitUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_DEBITUSER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_DEBITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get credit wallet
    Wallet creditWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreateTransfer.getCreditedWalletId());

    // validate credit wallet
    if (creditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_INEXISTENT, requestId);
    }

    // check credit wallet is registered to provider
    if ((creditWallet.getProviderId() == null) || (creditWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve credit wallet user
    User creditUser = userService.getUser(requestId, sessionAccount, creditWallet.getUserId());

    // validate credit wallet user
    if (creditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_CREDITUSER_INEXISTENT, requestId);
    }

    // check credit user is registered to provider
    if ((creditUser.getProviderId() == null) || (creditUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_CREDITUSER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_CREDITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if credit and debit wallets have the same currency
    if (!debitWallet.getCurrency().equals(creditWallet.getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_WALLET_CURRENCY_MISSMATCH);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_WALLET_CURRENCY_MISSMATCH, requestId);
    }

    // check if request transfer currency is the same as debited wallet
    // currency
    if (!debitWallet
        .getCurrency()
        .toString()
        .equals(requestCreateTransfer.getAmount().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_INVALID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_INVALID, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // STARTING FROM HERE WE VERIFY ERRORS WHICH WILL NOT REJECT THE REQUEST BUT THEY WILL CREATE
    // THE TRANSACTION AS FAILED

    // create response variable
    List<Transaction> responseTransactions = null;

    // check transfer amounts from request
    if ((responseTransactions == null) && (requestCreateTransfer.getAmount().getValue() <= 0)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AMOUNT_INVALID);
      responseTransactions =
          businessService.createFailedTransfer(
              requestId,
              sessionAccount,
              sessionApplication,
              debitWallet,
              creditWallet,
              requestCreateTransfer.getAmount(),
              requestCreateTransfer.getCustomTag(),
              ErrorMessage.ERROR_AMOUNT_INVALID);
    }

    // check transfer amounts from request
    if ((responseTransactions == null)
        && (requestCreateTransfer.getAmount().getValue()
            <= requestCreateTransfer.getFees().getValue())) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_REQUEST_AMOUNT_SMALLER_THAN_FEE);
      responseTransactions =
          businessService.createFailedTransfer(
              requestId,
              sessionAccount,
              sessionApplication,
              debitWallet,
              creditWallet,
              requestCreateTransfer.getAmount(),
              requestCreateTransfer.getCustomTag(),
              ErrorMessage.ERROR_CREATETRANSFER_REQUEST_AMOUNT_SMALLER_THAN_FEE);
    }

    // check balance
    if ((responseTransactions == null)
        && (requestCreateTransfer.getFeeModel() == null
            || requestCreateTransfer.getFeeModel().equals(FeeModel.INCLUDED.toString()))) {
      // check wallet balance as well
      if (debitWallet.getBalance().getValue() < requestCreateTransfer.getAmount().getValue()) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE);
        responseTransactions =
            businessService.createFailedTransfer(
                requestId,
                sessionAccount,
                sessionApplication,
                debitWallet,
                creditWallet,
                requestCreateTransfer.getAmount(),
                requestCreateTransfer.getCustomTag(),
                ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE);
      }
    }

    // check balance
    if ((responseTransactions == null)
        && (requestCreateTransfer.getFeeModel().equals(FeeModel.NOT_INCLUDED.toString()))) {
      // check wallet balance as well
      if (debitWallet.getBalance().getValue()
          < (requestCreateTransfer.getAmount().getValue()
              + requestCreateTransfer.getFees().getValue())) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE);
        responseTransactions =
            businessService.createFailedTransfer(
                requestId,
                sessionAccount,
                sessionApplication,
                debitWallet,
                creditWallet,
                requestCreateTransfer.getAmount(),
                requestCreateTransfer.getCustomTag(),
                ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE);
      }
    }

    // check debit wallet is registered to provider
    if ((responseTransactions == null)
        && ((debitWallet.getProviderId() == null) || (debitWallet.getProviderId().equals("")))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_NOT_REGISTERED2PROVIDER);
      responseTransactions =
          businessService.createFailedTransfer(
              requestId,
              sessionAccount,
              sessionApplication,
              debitWallet,
              creditWallet,
              requestCreateTransfer.getAmount(),
              requestCreateTransfer.getCustomTag(),
              ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_NOT_REGISTERED2PROVIDER);
    }

    // check credit wallet is registered to provider
    if ((responseTransactions == null)
        && ((creditWallet.getProviderId() == null) || (creditWallet.getProviderId().equals("")))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_NOT_REGISTERED2PROVIDER);
      responseTransactions =
          businessService.createFailedTransfer(
              requestId,
              sessionAccount,
              sessionApplication,
              debitWallet,
              creditWallet,
              requestCreateTransfer.getAmount(),
              requestCreateTransfer.getCustomTag(),
              ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_NOT_REGISTERED2PROVIDER);
    }

    // if there are no errors, start normal transaction process
    if (responseTransactions == null) {
      // start transaction process
      responseTransactions =
          businessService.createTransfer(
              requestId,
              sessionAccount,
              sessionApplication,
              debitUser,
              debitWallet,
              creditUser,
              creditWallet,
              requestCreateTransfer.getAmount(),
              requestCreateTransfer.getFees(),
              requestCreateTransfer.getFeeModel(),
              requestCreateTransfer.getCustomTag());
    }

    // return new transaction
    ResponseTransactions response = new ResponseTransactions();
    response.setTransactions(responseTransactions);

    return coreService.getResponse(response, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result refundTransaction(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/reverse");

    // Move json to object
    Form<RequestRefundTransfer> restForm =
        formFactory.form(RequestRefundTransfer.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestRefundTransfer formRefundTransfer = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // find transaction
    Transfer originalTransaction =
        transferService.getTransfer(
            requestId, sessionAccount, formRefundTransfer.getTransactionId());

    // check if we have a transaction with this id
    if (originalTransaction == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTION_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTION_INEXISTENT, requestId);
    }

    // validate transaction type allowed to be refunded - refund of this type can only be made for
    // TRANSFER
    if (!originalTransaction.getType().equals(TransactionType.TRANSFER)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_INVALID_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_INVALID_TYPE, requestId);
    }

    // get debit wallet
    Wallet originalDebitWallet =
        walletService.getWallet(
            requestId, sessionAccount, originalTransaction.getDebitedWalletId());

    // validate debit wallet
    if (originalDebitWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_DEBITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_DEBITWALLET_INEXISTENT, requestId);
    }

    // retrieve debit wallet user
    User originalDebitUser =
        userService.getUser(requestId, sessionAccount, originalDebitWallet.getUserId());

    // validate debit wallet user
    if (originalDebitUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_DEBITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_DEBITUSER_INEXISTENT, requestId);
    }

    // get credit wallet
    Wallet originalCreditWallet =
        walletService.getWallet(
            requestId, sessionAccount, originalTransaction.getCreditedWalletId());

    // validate credit wallet
    if (originalCreditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_CREDITWALLET_INEXISTENT, requestId);
    }

    // retrieve credit wallet user
    User originalCreditUser =
        userService.getUser(requestId, sessionAccount, originalCreditWallet.getUserId());

    // validate credit wallet user
    if (originalCreditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REFUNDTRANSACTION_CREDITUSER_INEXISTENT, requestId);
    }

    // STARTING FROM HERE WE VERIFY ERRORS WHICH WILL NOT REJECT THE REQUEST BUT THEY WILL CREATE
    // THE TRANSACTION AS FAILED

    // retrieved logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // create response variable
    List<Transaction> responseTransactions = null;

    // check debit wallet is registered to provider
    if ((responseTransactions == null)
        && (!originalTransaction.getStatus().equals(TransactionStatus.SUCCEEDED))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_REFUNDTRANSACTION_ORIGINALTRANSACTION_NOT_SUCCEEDED);
      responseTransactions =
          businessService.createFailedRefund(
              requestId,
              sessionAccount,
              sessionApplication,
              originalDebitWallet,
              originalCreditWallet,
              originalTransaction,
              formRefundTransfer.getCustomTag(),
              ErrorMessage.ERROR_REFUNDTRANSACTION_ORIGINALTRANSACTION_NOT_SUCCEEDED);
    }

    // check debit wallet is registered to provider
    if ((responseTransactions == null)
        && ((originalTransaction.getProviderId() == null)
            || (originalTransaction.getProviderId().equals("")))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_REFUNDTRANSACTION_ORIGINALTRANSACTION_NOT_REGISTERED2PROVIDER);
      responseTransactions =
          businessService.createFailedRefund(
              requestId,
              sessionAccount,
              sessionApplication,
              originalDebitWallet,
              originalCreditWallet,
              originalTransaction,
              formRefundTransfer.getCustomTag(),
              ErrorMessage.ERROR_REFUNDTRANSACTION_ORIGINALTRANSACTION_NOT_REGISTERED2PROVIDER);
    }

    // check balance for credit party , because we are dealing a refund and credit party become the
    // debit party
    if ((responseTransactions == null)
        && (originalCreditWallet.getBalance().getValue()
            < originalTransaction.getAmount().getValue())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REFUNDTRANSACTION_INSUFFICIENT_BALANCE);
      responseTransactions =
          businessService.createFailedRefund(
              requestId,
              sessionAccount,
              sessionApplication,
              originalDebitWallet,
              originalCreditWallet,
              originalTransaction,
              formRefundTransfer.getCustomTag(),
              ErrorMessage.ERROR_REFUNDTRANSACTION_INSUFFICIENT_BALANCE);
    }

    // if there are no errors, start normal transaction process
    if (responseTransactions == null) {
      // start transaction process
      responseTransactions =
          businessService.createTransferRefund(
              requestId,
              sessionAccount,
              sessionApplication,
              originalDebitUser,
              originalDebitWallet,
              originalCreditUser,
              originalCreditWallet,
              originalTransaction,
              formRefundTransfer.getCustomTag());
    }

    // return new transaction
    ResponseTransactions response = new ResponseTransactions();
    response.setTransactions(responseTransactions);

    return coreService.getResponse(response, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createPayOut(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/create/payout");

    // Move json to object
    Form<RequestCreatePayOut> restForm =
        formFactory.form(RequestCreatePayOut.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreatePayOut requestCreatePayOut = restForm.value().get();

    // check pay out currencies from request
    if (!requestCreatePayOut
        .getAmount()
        .getCurrency()
        .equals(requestCreatePayOut.getFees().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_MISSMATCH, requestId);
    }

    // check pay out amounts from request
    if (requestCreatePayOut.getAmount().getValue() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AMOUNT_INVALID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_AMOUNT_INVALID, requestId);
    }

    // check pay out amounts from request
    if (requestCreatePayOut.getAmount().getValue() <= requestCreatePayOut.getFees().getValue()) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_AMOUNT_SMALLER_THAN_FEE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_AMOUNT_SMALLER_THAN_FEE, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get debit wallet
    Wallet debitWallet =
        walletService.getWallet(
            requestId, sessionAccount, requestCreatePayOut.getDebitedWalletId());

    // validate debit wallet
    if (debitWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLET_INEXISTENT, requestId);
    }

    // check debit wallet is registered to provider
    if ((debitWallet.getProviderId() == null) || (debitWallet.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLET_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLET_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve debit wallet user
    User debitUser = userService.getUser(requestId, sessionAccount, debitWallet.getUserId());

    // validate debit wallet user
    if (debitUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_DEBITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITUSER_INEXISTENT, requestId);
    }

    // check debit user is registered to provider
    if ((debitUser.getProviderId() == null) || (debitUser.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITUSER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_DEBITUSER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if request transfer currency is the same as debited wallet
    // currency
    if (!debitWallet
        .getCurrency()
        .toString()
        .equals(requestCreatePayOut.getAmount().getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_INVALID, requestId);
    }

    // check balance
    if (requestCreatePayOut.getFeeModel() == null
        || requestCreatePayOut.getFeeModel().equals(FeeModel.INCLUDED.toString())) {
      // check wallet balance as well
      if (debitWallet.getBalance().getValue() < requestCreatePayOut.getAmount().getValue()) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE);

        return coreService.getErrorResponse(
            ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE, requestId);
      }
    }

    // check balance
    if (requestCreatePayOut.getFeeModel().equals(FeeModel.NOT_INCLUDED.toString())) {
      // check wallet balance as well
      if (debitWallet.getBalance().getValue()
          < (requestCreatePayOut.getAmount().getValue()
              + requestCreatePayOut.getFees().getValue())) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE);

        return coreService.getErrorResponse(
            ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE, requestId);
      }
    }

    // get bank account from database
    BankAccount bankAccount =
        bankAccountService.getBankAccount(
            requestId,
            sessionAccount,
            debitUser.getId().toString(),
            requestCreatePayOut.getBankAccountId());

    // test if bank account is not null
    if (bankAccount == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYOUT_INEXISTENT_BANKACCOUNT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYOUT_INEXISTENT_BANKACCOUNT, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // start transaction process
    List<Transaction> involvedTransactions =
        businessService.createPayOut(
            requestId,
            sessionAccount,
            sessionApplication,
            debitUser,
            debitWallet,
            bankAccount,
            requestCreatePayOut.getAmount(),
            requestCreatePayOut.getFees(),
            requestCreatePayOut.getFeeModel(),
            requestCreatePayOut.getCustomTag(),
            requestCreatePayOut.getBankWireRef());

    // return new transaction
    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions(involvedTransactions);

    return coreService.getResponse(responseTransactions, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getPayOut(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /transactions/" + transactionId + "/transfer");

    // check transaction id format
    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    PayOut transactionOutgoing =
        transactionOutgoingService.getPayOut(requestId, sessionAccount, transactionId);

    if (transactionOutgoing == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_PAYOUT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_PAYOUT, requestId);
    }

    if (!transactionOutgoing.getType().equals(TransactionType.PAYOUT)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE, requestId);
    }

    // return transaction
    ResponseTransaction responseTransaction = new ResponseTransaction();
    responseTransaction.setTransaction(transactionOutgoing);

    return coreService.getResponse(responseTransaction, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getPayIn(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /transactions/" + transactionId + "/payin");

    // check id
    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    PayIn transactionIncoming = payInService.getPayIn(requestId, sessionAccount, transactionId);

    if (transactionIncoming == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_PAYIN);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_PAYIN, requestId);
    }

    if (!transactionIncoming.getType().equals(TransactionType.PAYIN)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE, requestId);
    }

    // return transaction
    ResponseTransaction responseTransaction = new ResponseTransaction();
    responseTransaction.setTransaction(transactionIncoming);

    return coreService.getResponse(responseTransaction, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getTransferRefund(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /transactions/" + transactionId + "/refund");

    // check transaction id format
    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    Refund transactionRefund =
        transactionRefundService.getRefund(requestId, sessionAccount, transactionId);

    if (transactionRefund == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION, requestId);
    }

    /*
     * if (!(transactionRefund.getType().equals(TransactionType.TRANSFER) && transactionRefund.getNature().equals(TransactionNature.REFUND))) { logService.error(requestId, "TransactionController", "getTransferRefund", "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE); return coreService.getErrorResponse(ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE, requestId); }
     */

    // return refund
    ResponseTransaction responseTransaction = new ResponseTransaction();
    responseTransaction.setTransaction(transactionRefund);

    return coreService.getResponse(responseTransaction, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getTransfer(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /transactions/" + transactionId + "/transfer");

    // check id
    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // find transaction
    Transfer transfer = transferService.getTransfer(requestId, sessionAccount, transactionId);

    if (transfer == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION, requestId);
    }

    if (!transfer.getType().equals(TransactionType.TRANSFER)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE, requestId);
    }

    // return transaction
    ResponseTransaction responseTransaction = new ResponseTransaction();
    responseTransaction.setTransaction(transfer);

    return coreService.getResponse(responseTransaction, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result get(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /transactions/" + transactionId);

    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // find transaction
    Transaction transaction =
        transactionService.getTransaction(requestId, sessionAccount, transactionId);

    if (transaction == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_TRANSACTION, requestId);
    }

    // return transaction
    ResponseTransaction responseTransaction = new ResponseTransaction();
    responseTransaction.setTransaction(transaction);

    return coreService.getResponse(responseTransaction, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result refundPayIn(Request request, String payInId) {

    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /transactions/payin/" + payInId + "/refunds");

    // check id
    if ((payInId == null) || payInId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_PAYINID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_PAYINID, requestId);
    }

    // check id is valid
    if (!payInId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_PAYINID_FORMAT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_PAYINID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    PayIn payIn = payInService.getPayIn(requestId, sessionAccount, payInId);

    if (payIn == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_INEXISTENT_PAYIN);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_INEXISTENT_PAYIN, requestId);
    }

    if (!payIn.getType().equals(TransactionType.PAYIN)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_TYPE, requestId);
    }

    // get credit wallet
    Wallet creditWallet =
        walletService.getWallet(requestId, sessionAccount, payIn.getCreditedWalletId());

    // validate credit wallet
    if (creditWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_CREDITWALLET_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_CREDITWALLET_INEXISTENT, requestId);
    }

    // retrieve debit wallet user
    User creditUser = userService.getUser(requestId, sessionAccount, creditWallet.getUserId());

    // validate debit wallet user
    if (creditUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEPAYINREFUND_CREDITUSER_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEPAYINREFUND_CREDITUSER_INEXISTENT, requestId);
    }

    // Move json to object
    Form<RequestRefundPayIn> restForm =
        formFactory.form(RequestRefundPayIn.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestRefundPayIn formRefundPayIn = restForm.value().get();

    // retrieved logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // create response variable
    List<Transaction> responseTransactions = null;

    // if there are no errors, start normal transaction process
    if (responseTransactions == null) {

      // start transaction process
      responseTransactions =
          businessService.createPayInRefund(
              requestId,
              sessionAccount,
              sessionApplication,
              formRefundPayIn,
              payIn,
              creditWallet,
              creditUser);
    }

    // return new transaction
    ResponseTransactions response = new ResponseTransactions();
    response.setTransactions(responseTransactions);

    return coreService.getResponse(response, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getPayInStatusByProviderId(Request request, String transactionId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId, "IN", "start", "GET /transactions/" + transactionId + "/payin/byproviderid");

    // check id
    if ((transactionId == null) || transactionId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INVALID_TRANSACTIONID, requestId);
    }

    // check id is valid
    if (!transactionId.matches(ApplicationConstants.REGEX_VALIDATE_DIGITS)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INVALID_TRANSACTIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INVALID_TRANSACTIONID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    Application application = request.attrs().get(Attrs.APPLICATION_NAME);

    ProviderResponse providerResponse =
        businessService.getProviderPayInStatus(
            requestId, sessionAccount, application, transactionId);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // get transaction status
      TransactionStatus transactionStatus =
          (TransactionStatus) providerResponse.getProviderData("payInStatus");
      logService.debug(
          requestId, "L", "payInStatus", providerResponse.getProviderData("payInStatus"));

      // return transaction
      ResponseGetPayInStatusByProviderId responseGetPayInStatusByProviderId =
          new ResponseGetPayInStatusByProviderId();
      responseGetPayInStatusByProviderId.setStatus(String.valueOf(transactionStatus));

      return coreService.getResponse(responseGetPayInStatusByProviderId, requestId);
    } else {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INEXISTENT_PAYIN);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INEXISTENT_PAYIN, requestId);
    }
  }
}
