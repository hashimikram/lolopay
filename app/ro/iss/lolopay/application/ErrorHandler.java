package ro.iss.lolopay.application;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.typesafe.config.ConfigFactory;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.responses.RestResponse;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.EmailService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class ErrorHandler implements HttpErrorHandler {

  @Inject ApplicationErrorService applicationErrorService;

  @Inject EmailService emailService;

  @Inject CoreService coreService;

  @Inject LogService logService;

  @Inject UtilsService utilsService;

  @Override
  public CompletionStage<Result> onClientError(
      RequestHeader requestHeader, int statusCode, String message) {

    if (statusCode == 404) {
      return CompletableFuture.completedFuture(
          Results.badRequest("Request from IP " + requestHeader.remoteAddress() + " reported"));
    }
    logService.debug(coreService.getRequestId(requestHeader), "IN", "start", "no params");

    // build email content
    StringBuffer sb = new StringBuffer();
    sb.append(requestHeader.toString());
    sb.append(System.lineSeparator());
    sb.append("Status Code:");
    sb.append(statusCode);
    sb.append(System.lineSeparator());
    sb.append("Error message:");
    sb.append(message);
    sb.append(System.lineSeparator());
    sb.append("Error stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment")
            + " LoLo onClientError - "
            + message,
        sb.toString());

    // build error response
    ResponseError responseError = new ResponseError();
    responseError.setErrorCode(applicationErrorService.getErrorCode("", ErrorMessage.ERROR_CLIENT));
    responseError.setErrorDescription(message);

    // format client error
    RestResponse standardErrorResponse = new RestResponse();
    standardErrorResponse.setSuccess(false);
    standardErrorResponse.addError(responseError);

    return CompletableFuture.completedFuture(
        Results.badRequest(Json.toJson(standardErrorResponse)));
  }

  @Override
  public CompletionStage<Result> onServerError(RequestHeader requestHeader, Throwable exception) {

    logService.debug(coreService.getRequestId(requestHeader), "IN", "start", "no params");

    // build email content
    StringBuffer sb = new StringBuffer();
    sb.append(requestHeader.toString());
    sb.append(System.lineSeparator());
    sb.append("Exception Message:");
    sb.append(exception.getMessage());
    sb.append(System.lineSeparator());
    sb.append("Exception stack:");
    sb.append(ExceptionUtils.getStackTrace(exception));
    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment")
            + " LoLo onServerError - "
            + exception.getMessage(),
        sb.toString());

    // build error response
    ResponseError responseError = new ResponseError();
    responseError.setErrorCode(applicationErrorService.getErrorCode("", ErrorMessage.ERROR_SERVER));
    responseError.setErrorDescription(exception.getMessage());

    // format server error
    RestResponse standardErrorResponse = new RestResponse();
    standardErrorResponse.setSuccess(false);
    standardErrorResponse.addError(responseError);

    return CompletableFuture.completedFuture(
        Results.internalServerError(Json.toJson(standardErrorResponse)));
  }
}
