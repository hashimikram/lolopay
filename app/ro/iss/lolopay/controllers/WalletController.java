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
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.TransactionService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.requests.RequestCreateWallet;
import ro.iss.lolopay.responses.ResponseTransactions;
import ro.iss.lolopay.responses.ResponseWallet;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class WalletController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject BusinessService businessService;

  @Inject UtilsService utilsService;

  @Inject WalletService walletService;

  @Inject UserService userService;

  @Inject TransactionService transactionService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result updateCompanyWallets(Request request, String walletId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /wallets/company/" + walletId + "/update");

    // check wallet id format
    if ((walletId == null) || walletId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID, requestId);
    }

    // check id is valid
    if (!walletId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // get wallet by id
    Wallet wallet = walletService.getWallet(requestId, sessionAccount, walletId);

    // test wallet
    if (wallet == null && walletId.matches(ApplicationConstants.REGEX_VALIDATE_DIGITS)) {
      // get wallet by providerId
      wallet = walletService.getWalletByProviderId(requestId, sessionAccount, walletId);
    }

    if (wallet == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INEXISTENT_WALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLET_INEXISTENT_WALLET, requestId);
    }

    logService.debug(requestId, "L", "wallet", utilsService.prettyPrintObject(wallet));
    String ccy = String.valueOf(wallet.getCurrency());
    if (!wallet.getDescription().equals("Main " + ccy + " account wallet")) {
      logService.error(requestId, "L", "errors", "It's not a company wallet");
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLET_INEXISTENT_WALLET, requestId);
    }

    try {
      businessService.updateCompanyWalletBalance(
          requestId, sessionAccount, sessionApplication, wallet);

      // create response wallet
      ResponseWallet responseWallet = new ResponseWallet();
      responseWallet.setWallet(wallet);

      return coreService.getResponse(responseWallet, requestId);

    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result create(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /wallets/create");

    // Move json to object
    Form<RequestCreateWallet> restForm =
        formFactory.form(RequestCreateWallet.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateWallet requestCreateWallet = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateWallet.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEWALLET_USER_ID_INEXISTENT_USER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEWALLET_USER_ID_INEXISTENT_USER, requestId);
    }

    // check if user is registered to provider
    if ((existingUser.getProviderId() == null) || (existingUser.getProviderId().equals(""))) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEWALLET_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEWALLET_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if wallet already exists
    Wallet existingWallet =
        walletService.getWallet(
            requestId,
            sessionAccount,
            requestCreateWallet.getUserId(),
            CurrencyISO.valueOf(requestCreateWallet.getCurrency()));

    if (existingWallet != null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEWALLET_DUPLICATED_WALLET);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEWALLET_DUPLICATED_WALLET, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save wallet
      Wallet newWallet =
          businessService.createWallet(
              requestId, sessionAccount, sessionApplication, existingUser, requestCreateWallet);

      // create response
      ResponseWallet responseWallet = new ResponseWallet();
      responseWallet.setWallet(newWallet);

      // return response
      return coreService.getResponse(responseWallet, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result get(Request request, String walletId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /wallets/" + walletId);

    // check wallet id format
    if ((walletId == null) || walletId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID, requestId);
    }

    // check id is valid
    if (!walletId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLET_INVALID_WALLETID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get wallet by id
    Wallet wallet = walletService.getWallet(requestId, sessionAccount, walletId);

    // test wallet
    if (wallet == null && walletId.matches(ApplicationConstants.REGEX_VALIDATE_DIGITS)) {
      // get wallet by providerId
      wallet = walletService.getWalletByProviderId(requestId, sessionAccount, walletId);
    }

    if (wallet == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLET_INEXISTENT_WALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLET_INEXISTENT_WALLET, requestId);
    }

    // create response wallet
    ResponseWallet responseWallet = new ResponseWallet();
    responseWallet.setWallet(wallet);

    return coreService.getResponse(responseWallet, requestId);
  }

  @SuppressWarnings("unchecked")
  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result transactions(Request request, String walletId, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /wallets/" + walletId + "/transactions");

    // check walletid format
    if ((walletId == null) || walletId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID, requestId);
    }

    // check id is valid
    if (!walletId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID, requestId);
    }

    // retrieve account from session
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // retrieve wallet by id
    Wallet wallet = walletService.getWallet(requestId, sessionAccount, walletId);

    if (wallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_INEXISTENT_WALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_INEXISTENT_WALLET, requestId);
    }

    PaginatedList paginatedList =
        transactionService.getTransactionsPerWallet(
            requestId, sessionAccount, walletId, page, pageSize);

    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions((List<Transaction>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseTransactions, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }

  @SuppressWarnings("unchecked")
  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result transactionsNoFees(
      Request request, String walletId, int page, int pageSize, String sort) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /wallets/" + walletId + "/transactionsNoFees");

    // check wallet id format
    // if ((walletId != null) && !walletId.equals(""))
    if ((walletId == null) || walletId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID, requestId);
    }

    // check id is valid
    if (!walletId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_WALLETID_INVALID, requestId);
    }

    // retrieve account from session
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // retrieve wallet by id
    Wallet wallet = walletService.getWallet(requestId, sessionAccount, walletId);

    if (wallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETTRANSACTIONS_INEXISTENT_WALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETWALLETTRANSACTIONS_INEXISTENT_WALLET, requestId);
    }

    PaginatedList paginatedList =
        transactionService.getTransactionsPerWalletNoFee(
            requestId, sessionAccount, walletId, page, pageSize, sort);

    ResponseTransactions responseTransactions = new ResponseTransactions();
    responseTransactions.setTransactions((List<Transaction>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseTransactions, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }
}
