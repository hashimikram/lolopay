package ro.iss.lolopay.services.implementation;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.client.utils.URIBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.mvc.Results;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.requests.RequestClientHook;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.NotificationService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class NotificationImplementation implements NotificationService {
  @Inject private WSClient ws;

  @Inject LogService logService;

  @Inject Config config;

  @Inject UtilsService utilsService;

  @Inject private play.Environment environment;

  @Override
  public void notifySelf(
      String requestId,
      Account account,
      Application application,
      NotificationType notificationType,
      String resourceId) {

    URIBuilder builder = new URIBuilder();
    builder.setScheme(config.getString("application.scheme"));
    builder.setHost(config.getString("application.host"));
    builder.setPort(config.getInt("application.port"));
    builder.setPath(
        String.format(
            config.getString("mangopay.returnUrlPath"),
            account.getAccountId(),
            application.getApplicationId()));
    builder.addParameter("RessourceId", resourceId);
    builder.addParameter("EventType", notificationType.toString());
    builder.addParameter("Date", String.valueOf(utilsService.getTimeStamp()));

    // Initialise the request object
    WSRequest request = ws.url(builder.toString()).setRequestTimeout(getAllowedExecutionTime());
    request.get();
  }

  @Override
  public void notifyClient(
      String requestId,
      Account account,
      Application application,
      NotificationType notificationType,
      String resourceId) {

    logService.debug(requestId, "IN", "application.id", application.getId().toString());
    logService.debug(requestId, "IN", "application.name", application.getApplicationName());
    logService.debug(requestId, "IN", "application.hookUrl", application.getApplicationHookUrl());
    logService.debug(requestId, "IN", "notificationType", notificationType);
    logService.debug(requestId, "IN", "resourceId", resourceId);

    // if we have a hook configured
    if ((application.getApplicationHookUrl() != null)
        && (!application.getApplicationHookUrl().equals(""))) {
      // build hook data
      RequestClientHook requestClientHook = new RequestClientHook();
      requestClientHook.setApplicationId(application.getId().toString());
      requestClientHook.setApplicationName(application.getApplicationName());
      requestClientHook.setNotificationType(notificationType);
      requestClientHook.setResourceId(resourceId);

      // Initialise the request object
      WSRequest request =
          ws.url(application.getApplicationHookUrl()).setRequestTimeout(getAllowedExecutionTime());

      // perform asynchrony post operation in hook and attach callback to it
      if (!environment.isTest()) {
        request
            .post(Json.toJson(requestClientHook))
            .whenComplete(
                (wSResponse, executionException) ->
                    processResponseFromHookCall(requestId, wSResponse, executionException));
      }
    }
  }

  /**
   * Retrieve the number of milliseconds the server has to wait for hook implementation
   *
   * @return
   */
  private Duration getAllowedExecutionTime() {

    if (ConfigFactory.load().hasPath("application.maxMsToWaitForClientHook")) {
      return Duration.of(
          ConfigFactory.load().getInt("application.maxMsToWaitForClientHook"), ChronoUnit.MILLIS);
    } else {
      // default wait time is half second
      return Duration.of(500, ChronoUnit.MILLIS);
    }
  }

  /**
   * Catch the response when it comes from hook call
   *
   * @param wsResponse
   * @param exception
   * @return
   */
  private Result processResponseFromHookCall(
      String requestId, WSResponse wsResponse, Throwable exception) {

    // test if hook call comes out with an error
    if (exception != null) {

      logService.debug(requestId, "IN", "exception", exception.getMessage());
      logService.debug(requestId, "IN", "getLocalizedMessage", exception.getLocalizedMessage());
      logService.debug(requestId, "IN", "getCause", exception.getCause());
      logService.debug(requestId, "IN", "trace", "");

      // create default error message
      GenericRestException gre = new GenericRestException();

      // create return error object
      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_NOT_SENT_TO_CLIENT);
      responseError.setErrorDescription("Hook not sent : " + exception.getMessage());

      gre.addResponseError(responseError);

      throw gre;
    }

    logService.debug(requestId, "IN", "wsResponse", wsResponse.getStatus());

    // it is not relevant the response from client hook
    return Results.ok();
  }
}
