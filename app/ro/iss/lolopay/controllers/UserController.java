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
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.Dispute;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankAccountService;
import ro.iss.lolopay.models.services.definition.DocumentService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.requests.RequestCreateLegalUser;
import ro.iss.lolopay.requests.RequestCreateNaturalUser;
import ro.iss.lolopay.requests.RequestSaveLegalUser;
import ro.iss.lolopay.requests.RequestSaveNaturalUser;
import ro.iss.lolopay.responses.ResponseBankAccount;
import ro.iss.lolopay.responses.ResponseBankAccounts;
import ro.iss.lolopay.responses.ResponseDocuments;
import ro.iss.lolopay.responses.ResponseGetDisputes;
import ro.iss.lolopay.responses.ResponseUser;
import ro.iss.lolopay.responses.ResponseWallets;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class UserController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject UtilsService utilsService;

  @Inject UserService userService;

  @Inject BankAccountService bankAccountService;

  @Inject WalletService walletService;

  @Inject DocumentService documentService;

  @Inject BusinessService businessService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result anonymize(Request request, String userId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/anonymize");

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID, requestId);
    }

    // get the account and application from context
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETUSER_INEXISTENT_USER);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETUSER_INEXISTENT_USER, requestId);
    }

    try {
      // update natural user
      businessService.anonymizeUser(requestId, sessionAccount, sessionApplication, user);
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "errors", "Failed anonymizing the user");
    }

    // create a new user response
    ResponseUser responseUser = new ResponseUser();

    // set user to the user response
    responseUser.setUser(user);

    return coreService.getResponse(responseUser, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createNatural(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /users/create/natural");

    // Move json to object
    Form<RequestCreateNaturalUser> restForm =
        formFactory.form(RequestCreateNaturalUser.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateNaturalUser requestCreateNaturalUser = restForm.value().get();

    logService.debug(requestId, "L", "ATTRS: ", request.attrs().toString());

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get running application
    // Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // process request
      User newUser =
          businessService.createNaturalUser(
              requestId, sessionAccount, sessionApplication, requestCreateNaturalUser);

      // return new user
      ResponseUser responseUser = new ResponseUser();
      responseUser.setUser(newUser);
      return coreService.getResponse(responseUser, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result saveNatural(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /users/save/natural");

    // Move json to object
    Form<RequestSaveNaturalUser> restForm =
        formFactory.form(RequestSaveNaturalUser.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestSaveNaturalUser requestSaveNaturalUser = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // get User
    User user = userService.getUser(requestId, sessionAccount, requestSaveNaturalUser.getId());

    // test user
    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SAVEUSER_INEXISTENT_USER);

      return coreService.getErrorResponse(ErrorMessage.ERROR_SAVEUSER_INEXISTENT_USER, requestId);
    }

    // test user type
    if (!user.getType().equals(UserType.NATURAL)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SAVEUSER_USERTYPE_INVALID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_SAVEUSER_USERTYPE_INVALID, requestId);
    }

    if ((user.getProviderId() == null) || user.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SAVEUSER_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SAVEUSER_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      // process user save
      businessService.saveNaturalUser(
          requestId, sessionAccount, sessionApplication, user, requestSaveNaturalUser);

      // return new saved user
      ResponseUser responseUser = new ResponseUser();
      responseUser.setUser(user);
      return coreService.getResponse(responseUser, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createLegal(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /users/create/legal");

    // Move json to object
    Form<RequestCreateLegalUser> restForm =
        formFactory.form(RequestCreateLegalUser.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateLegalUser requestCreateLegalUser = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // process request
      User newLegalUser =
          businessService.createLegalUser(
              requestId, sessionAccount, sessionApplication, requestCreateLegalUser);

      // return new user
      ResponseUser responseUser = new ResponseUser();
      responseUser.setUser(newLegalUser);
      return coreService.getResponse(responseUser, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result saveLegal(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /users/save/legal");

    // Move json to object
    Form<RequestSaveLegalUser> restForm =
        formFactory.form(RequestSaveLegalUser.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestSaveLegalUser requestSaveLegalUser = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // get User
    User legalUser = userService.getUser(requestId, sessionAccount, requestSaveLegalUser.getId());

    // test user
    if (legalUser == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SAVELEGALUSER_INEXISTENT_USER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SAVELEGALUSER_INEXISTENT_USER, requestId);
    }

    // test user type
    if (!legalUser.getType().equals(UserType.LEGAL)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SAVELEGALUSER_USERTYPE_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SAVELEGALUSER_USERTYPE_INVALID, requestId);
    }

    if ((legalUser.getProviderId() == null) || legalUser.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SAVELEGALUSER_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SAVELEGALUSER_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      // save legal user
      businessService.saveLegalUser(
          requestId, sessionAccount, sessionApplication, legalUser, requestSaveLegalUser);

      // return new user
      ResponseUser responseUser = new ResponseUser();
      responseUser.setUser(legalUser);
      return coreService.getResponse(responseUser, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result get(Request request, String userId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/" + userId);

    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETUSER_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETUSER_INVALID_USERID, requestId);
    }

    // check user provided
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETUSER_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETUSER_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETUSER_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETUSER_INVALID_USERID, requestId);
    }

    // get the account and application from context
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETUSER_INEXISTENT_USER);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETUSER_INEXISTENT_USER, requestId);
    }

    // do not update data from provider if user has anonimyzed gdpr data
    if (!user.getEmail().equals("anonymized@email.gdpr")) {
      try {
        // update natural user
        businessService.updateUserBasicDataFromProvider(
            requestId, sessionAccount, sessionApplication, user);
      } catch (GenericRestException gre) {
        logService.error(requestId, "L", "errors", "Failed updating from Provider");
      }
    }

    // create a new user response
    ResponseUser responseUser = new ResponseUser();

    // set user to the user response
    responseUser.setUser(user);

    return coreService.getResponse(responseUser, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getBankAccount(Request request, String userId, String bankAccountId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId, "IN", "start", "GET /users/" + userId + "/bankaccounts/" + bankAccountId);

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_USERID, requestId);
    }

    // validate bank account id
    if ((bankAccountId == null) || bankAccountId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_BANKACCOUNTID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_BANKACCOUNTID, requestId);
    }

    // check id is valid
    if (!bankAccountId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_BANKACCOUNTID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INVALID_BANKACCOUNTID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    // test if user is null
    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INEXISTENT_USER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INEXISTENT_USER, requestId);
    }

    // get bank account from database
    BankAccount bankAccount =
        bankAccountService.getBankAccount(requestId, sessionAccount, userId, bankAccountId);

    if (bankAccount == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNT_INEXISTENT_BANKACCOUNT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNT_INEXISTENT_BANKACCOUNT, requestId);
    }

    // create response bank account
    ResponseBankAccount responseBankAccount = new ResponseBankAccount();
    responseBankAccount.setBankAccount(bankAccount);

    // return response
    return coreService.getResponse(responseBankAccount, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @SuppressWarnings("unchecked")
  public Result getBankAccounts(Request request, String userId, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/" + userId + "/bankaccounts");

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNTS_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNTS_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNTS_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNTS_INVALID_USERID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    // test if user is null
    if (user == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETBANKACCOUNTS_INEXISTENT_USER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETBANKACCOUNTS_INEXISTENT_USER, requestId);
    }

    // get bank account from database
    PaginatedList paginatedList =
        bankAccountService.getBankAccountsPerUser(
            requestId, sessionAccount, userId, page, pageSize);

    // create response bank account
    ResponseBankAccounts responseBankAccounts = new ResponseBankAccounts();
    responseBankAccounts.setBankAccounts((List<BankAccount>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseBankAccounts, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @SuppressWarnings("unchecked")
  public Result getWallets(Request request, String userId, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/" + userId + "/wallets");

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETS_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLETS_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETS_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLETS_INVALID_USERID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    // test if user is null
    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETWALLETS_INEXISTENT_USER);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETWALLETS_INEXISTENT_USER, requestId);
    }

    // get wallets from database
    PaginatedList paginatedList =
        walletService.getWalletsPerUser(requestId, sessionAccount, userId, page, pageSize);

    // create response list
    ResponseWallets responseWallets = new ResponseWallets();
    responseWallets.setWallets((List<Wallet>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseWallets, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @SuppressWarnings("unchecked")
  public Result getDocuments(Request request, String userId, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/" + userId + "/documents");

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENTS_INVALID_USERID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDOCUMENTS_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENTS_INVALID_USERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDOCUMENTS_INVALID_USERID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    // test if user is null
    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENTS_INEXISTENT_USER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDOCUMENTS_INEXISTENT_USER, requestId);
    }

    // get documents from database
    PaginatedList paginatedList =
        documentService.getDocumentsPerUser(requestId, sessionAccount, userId, page, pageSize);

    // create response list
    ResponseDocuments responseDocuments = new ResponseDocuments();
    responseDocuments.setDocuments((List<Document>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseDocuments, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @SuppressWarnings("unchecked")
  public Result getDisputes(Request request, String userId, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /users/" + userId + "/disputes");

    logService.debug(requestId, "IN", "userId", userId);
    logService.debug(requestId, "IN", "page", page);
    logService.debug(requestId, "IN", "pageSize", pageSize);

    // validate user id
    if ((userId == null) || userId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDISPUTES_INVALID_USERID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_GETDISPUTES_INVALID_USERID, requestId);
    }

    // check id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDISPUTES_INVALID_USERID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETDISPUTES_INVALID_USERID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user = userService.getUser(requestId, sessionAccount, userId);

    // test if user is null
    if (user == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDISPUTES_INEXISTENT_USER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDISPUTES_INEXISTENT_USER, requestId);
    }

    try {
      // get documents from provider
      PaginatedList paginatedList =
          businessService.getUserDisputes(requestId, sessionAccount, user, page, pageSize);

      ResponseGetDisputes responseGetDisputes = new ResponseGetDisputes();
      responseGetDisputes.setDisputes((List<Dispute>) paginatedList.getList());

      // add pagination headers
      Result result = coreService.getResponse(responseGetDisputes, requestId);
      result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
      result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
      result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
      result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

      // return response
      return result;
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }
}
