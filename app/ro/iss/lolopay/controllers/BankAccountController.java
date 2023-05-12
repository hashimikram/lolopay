package ro.iss.lolopay.controllers;

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
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankAccountService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.requests.RequestCreateBankAccount_CA;
import ro.iss.lolopay.requests.RequestCreateBankAccount_GB;
import ro.iss.lolopay.requests.RequestCreateBankAccount_IBAN;
import ro.iss.lolopay.requests.RequestCreateBankAccount_OTHER;
import ro.iss.lolopay.requests.RequestCreateBankAccount_US;
import ro.iss.lolopay.requests.RequestDeactivateBankAccount;
import ro.iss.lolopay.responses.ResponseBankAccount;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class BankAccountController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject UtilsService utilsService;

  @Inject UserService userService;

  @Inject BankAccountService bankAccountService;

  @Inject BusinessService businessService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createIban(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/create/iban");

    // Move request to object
    Form<RequestCreateBankAccount_IBAN> restForm =
        formFactory.form(RequestCreateBankAccount_IBAN.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankAccount_IBAN requestCreateBankAccount_IBAN = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateBankAccount_IBAN.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save bank account
      BankAccount bankAccount =
          businessService.createBankAccountIBAN(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              requestCreateBankAccount_IBAN);

      // create response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createUs(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/create/us");

    // Move json to object
    Form<RequestCreateBankAccount_US> restForm =
        formFactory.form(RequestCreateBankAccount_US.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankAccount_US requestCreateBankAccount_US = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateBankAccount_US.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save bank account
      BankAccount bankAccount =
          businessService.createBankAccountUS(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              requestCreateBankAccount_US);

      // create response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createGb(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/create/gb");

    // Move json to object
    Form<RequestCreateBankAccount_GB> restForm =
        formFactory.form(RequestCreateBankAccount_GB.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankAccount_GB recreateCreateBankAccount_GB = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, recreateCreateBankAccount_GB.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save bank account
      BankAccount bankAccount =
          businessService.createBankAccountGB(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              recreateCreateBankAccount_GB);

      // create response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createCa(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/create/ca");

    // Move json to object
    Form<RequestCreateBankAccount_CA> restForm =
        formFactory.form(RequestCreateBankAccount_CA.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankAccount_CA requestCreateBankAccount_CA = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateBankAccount_CA.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save bank account
      BankAccount bankAccount =
          businessService.createBankAccountCA(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              requestCreateBankAccount_CA);

      // create response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createOther(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/create/other");

    // Move json to object
    Form<RequestCreateBankAccount_OTHER> restForm =
        formFactory.form(RequestCreateBankAccount_OTHER.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankAccount_OTHER requestCreateBankAccount_Other = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateBankAccount_Other.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save bank account
      BankAccount bankAccount =
          businessService.createBankAccountOTHER(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              requestCreateBankAccount_Other);

      // create response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result deactivateBankAccount(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /bankaccounts/deactivate");

    // Move json to object
    Form<RequestDeactivateBankAccount> restForm =
        formFactory.form(RequestDeactivateBankAccount.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestDeactivateBankAccount requestDeactivateBankAccount = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get the user from database through the userService
    User user =
        userService.getUser(requestId, sessionAccount, requestDeactivateBankAccount.getUserId());

    // test if user is null
    if (user == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_USER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_USER, requestId);
    }

    // test if user is registered to provider
    if ((user.getProviderId() == null) || user.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get bank account from database
    BankAccount bankAccount =
        bankAccountService.getBankAccount(
            requestId,
            sessionAccount,
            requestDeactivateBankAccount.getUserId(),
            requestDeactivateBankAccount.getBankAccountId());

    // test if bank account is not null
    if (bankAccount == null) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_BANKACCOUNT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_BANKACCOUNT, requestId);
    }

    // test if bank account is registered to provider
    if ((bankAccount.getProviderId() == null) || bankAccount.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_OBJECT_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_OBJECT_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // deactivate bank account
      businessService.deactivateBankAccount(
          requestId, sessionAccount, sessionApplication, user, bankAccount);

      // create ban account response
      ResponseBankAccount responseBankAccount = new ResponseBankAccount();
      responseBankAccount.setBankAccount(bankAccount);

      // return response
      return coreService.getResponse(responseBankAccount, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }
}
