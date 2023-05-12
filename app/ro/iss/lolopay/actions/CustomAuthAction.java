package ro.iss.lolopay.actions;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.ApplicationStatus;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.models.services.definition.SessionService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.TokenService;

public class CustomAuthAction extends play.mvc.Action.Simple {
  @Inject TokenService tokenService;

  @Inject AccountService accountService;

  @Inject CoreService coreService;

  @Inject SessionService sessionService;

  @Inject ApplicationService applicationService;

  @Inject LogService logService;

  @Override
  public CompletionStage<Result> call(Request request) {

    // get request id for later use in error response
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "Verify credentials process");

    // check if authorization header is present
    if (!request.hasHeader(Http.HeaderNames.AUTHORIZATION)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_HEADER_MISSING);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_HEADER_MISSING, requestId);
    }

    // get JWT token
    String securityToken = coreService.getRequestAuthorizationToken(request);
    logService.debug(requestId, "L", "securityToken", securityToken);

    // check token syntax and dates
    if (!tokenService.isTokenValid(securityToken)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INVALID_OR_EXPIRED);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INVALID_OR_EXPIRED, requestId);
    }

    // get session from token
    String sessionId = tokenService.getSessionIdFromToken(securityToken);
    if (sessionId == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INCOMPLETE);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INCOMPLETE, requestId);
    }
    logService.debug(requestId, "L", "sessionId", sessionId);

    // get session record
    Session sessionRecord = sessionService.getSession(requestId, sessionId);
    if (sessionRecord == null) {
      logService.error(requestId, "L", "errors", "Session not found");
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_SESSION_EXPIRED);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_SESSION_EXPIRED, requestId);
    }

    // extract role from token
    String claimRole = tokenService.getRoleFromToken(securityToken);

    // check token for logical business aspects
    if (!tokenService.isTokenSecure(
        securityToken,
        sessionRecord.getAccountId(),
        sessionRecord.getApplicationId(),
        sessionRecord.getId().toString(),
        request.remoteAddress(),
        claimRole)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_TOKEN_FAILED_SECURITY);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_TOKEN_FAILED_SECURITY, requestId);
    }

    // check roles
    if ((claimRole.equals(ApplicationConstants.CLAIMS_ROLE_REFRESH)
            && !request.path().equals(ApplicationConstants.HTTP_REFRESH_REQUEST_PATH))
        || (claimRole.equals(ApplicationConstants.CLAIMS_ROLE_AUTHENTICATION)
            && request.path().equals(ApplicationConstants.HTTP_REFRESH_REQUEST_PATH))) {
      logService.error(
          requestId,
          "L",
          "errors",
          "You can not access refresh without refresh token or resources with a refresh token");
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_OPERATION_NOT_ALLOWED);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_OPERATION_NOT_ALLOWED, requestId);
    }

    logService.debug(requestId, "L", "securityToken", "JWT token check PASSED");

    // get session Account
    Account sessionAccount = accountService.getAccount(requestId, sessionRecord.getAccountId());
    if (sessionAccount == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_INEXISTENT_ACCOUNT);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_INEXISTENT_ACCOUNT, requestId);
    }
    logService.debug(requestId, "L", "sessionAccount", sessionAccount.getAccountName());

    // get session application
    Application sessionApplication =
        applicationService.getApplication(
            requestId, sessionAccount, sessionRecord.getApplicationId());
    if (sessionApplication == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_INEXISTENT_APPLICATION);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_INEXISTENT_APPLICATION, requestId);
    }
    logService.debug(requestId, "L", "sessionApplication", sessionApplication.getApplicationName());

    // check if application is active
    if (sessionApplication.getApplicationStatus() != ApplicationStatus.ACTIVE) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_AUTHORIZATION_APPLICATION_BLOCKED);
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_AUTHORIZATION_APPLICATION_BLOCKED, requestId);
    }

    // update application dynamic details
    applicationService.updateApplicationDetails(
        requestId,
        sessionAccount,
        sessionApplication,
        request.remoteAddress(),
        coreService.getRequestUserAgent(request));

    // pass session record for later use
    request = request.addAttr(Attrs.SESSION_NAME, sessionRecord);
    request = request.addAttr(Attrs.ACCOUNT_NAME, sessionAccount);
    request = request.addAttr(Attrs.APPLICATION_NAME, sessionApplication);

    logService.debug(requestId, "OUT", "completed", "Request authenticated");
    return delegate.call(request);
  }
}
