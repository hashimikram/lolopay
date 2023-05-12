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
import ro.iss.lolopay.classes.AuthenticationResponse;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.requests.RequestLogin;
import ro.iss.lolopay.responses.ResponseTokenSet;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.SecurityService;
import ro.iss.lolopay.services.definition.TokenService;
import ro.iss.lolopay.services.definition.UtilsService;

public class AuthenticationController extends RestController {
  @Inject FormFactory formFactory;

  @Inject AccountService accountService;

  @Inject ApplicationService applicationService;

  @Inject SecurityService securityService;

  @Inject UtilsService utilsService;

  @Inject CoreService coreService;

  @Inject TokenService tokenService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  public Result login(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /login");

    // Move json to object
    Form<RequestLogin> restForm = formFactory.form(RequestLogin.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestLogin formLogin = restForm.value().get();
    logService.debug(requestId, "L", "accountId", formLogin.getAccountId());
    logService.debug(requestId, "L", "applicationId", formLogin.getApplicationId());

    // process request
    // try to authenticate the application
    AuthenticationResponse authenticationResponse =
        securityService.authenticateApplication(
            requestId,
            formLogin.getAccountId(),
            formLogin.getApplicationId(),
            formLogin.getPassword());

    // check if validation is OK
    if (authenticationResponse == null) {
      return coreService.getErrorResponse(ErrorMessage.ERROR_LOGIN_INVALID_CREDENTIALS, requestId);
    }

    // update details and send response
    // update details - login moment
    applicationService.updateApplicationDetailsLogin(
        requestId,
        authenticationResponse.getAuthenticatedAccount(),
        authenticationResponse.getAuthenticatedApplication());

    // register application session
    TokenSet sessionToken =
        securityService.registerSession(requestId, authenticationResponse, request.remoteAddress());

    ResponseTokenSet responseLogin = new ResponseTokenSet();
    responseLogin.setTokenSet(sessionToken);

    return coreService.getResponse(responseLogin, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result refresh(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /refresh");

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // update details - refresh moment
    applicationService.updateApplicationDetailsRefresh(
        requestId, sessionAccount, sessionApplication);

    // remove current session
    securityService.unregisterSession(requestId, request.attrs().get(Attrs.SESSION_NAME));

    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setAuthenticatedAccount(sessionAccount);
    authenticationResponse.setAuthenticatedApplication(sessionApplication);

    // register application session
    TokenSet newTokenSet =
        securityService.registerSession(requestId, authenticationResponse, request.remoteAddress());

    // return new token in the same format as login response
    ResponseTokenSet responseLogin = new ResponseTokenSet();
    responseLogin.setTokenSet(newTokenSet);
    return coreService.getResponse(responseLogin, requestId);
  }
}
