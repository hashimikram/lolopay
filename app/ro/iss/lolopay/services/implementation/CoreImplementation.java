package ro.iss.lolopay.services.implementation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.models.services.definition.RequestHistoryService;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.responses.RestResponse;
import ro.iss.lolopay.responses.RestResponseBody;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.EmailService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;
import scala.collection.mutable.StringBuilder;

public class CoreImplementation implements CoreService {
  @Inject ApplicationErrorService applicationErrorService;

  @Inject LogService logService;

  @Inject EmailService emailService;

  @Inject RequestHistoryService requestHistoryService;

  @Inject UtilsService utilsService;

  /** Retrieve application stamp for system based db operations */
  @Override
  public ApplicationStamp getSystemApplicationStamp() {

    ApplicationStamp systemApplication = new ApplicationStamp();
    systemApplication.setApplicationName("CORE SYSTEM");
    systemApplication.setApplicationEmail(ConfigFactory.load().getString("application.email"));
    return systemApplication;
  }

  @Override
  public CompletionStage<Result> getErrorResponsePromise(String errorKey, String requestId) {

    logService.debug(requestId, "IN", "errorKey", errorKey);
    logService.debug(requestId, "IN", "requestId", requestId);

    // build email content with error details
    StringBuffer sb = new StringBuffer();
    sb.append("Request Id:");
    sb.append(requestId);
    sb.append(System.lineSeparator());
    sb.append("Error Key:");
    sb.append(errorKey);
    sb.append(System.lineSeparator());
    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment")
            + " LoLo getErrorResponsePromise - "
            + errorKey,
        sb.toString());

    return CompletableFuture.supplyAsync(
        new Supplier<Result>() {
          @Override
          public Result get() {

            // format response object
            RestResponse restResponse = new RestResponse();
            restResponse.setSuccess(false);

            // build error response
            ResponseError responseError = new ResponseError();
            responseError.setErrorCode(applicationErrorService.getErrorCode(requestId, errorKey));
            responseError.setErrorDescription(errorKey);

            // add error to response
            restResponse.addError(responseError);

            // create JSON response
            JsonNode jsonResponse = Json.toJson(restResponse);
            logService.debug(requestId, "OUT", "jsonResponse", jsonResponse.toString());

            // save response in database
            if ((requestId != null) && (!requestId.equals(""))) {
              RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
              if (requestHistory != null) {
                requestHistory.setResponse(jsonResponse.toString());
                requestHistoryService.saveRequestHistory(requestId, requestHistory);
              }
            }

            // return error response
            return Results.badRequest(jsonResponse);
          }
        });
  }

  @Override
  public Result getErrorResponse(String errorKey, String requestId) {

    logService.debug(requestId, "IN", "errorKey", errorKey);
    logService.debug(requestId, "IN", "requestId", requestId);

    // build email content with error details
    StringBuffer sb = new StringBuffer();
    sb.append("Request Id:");
    sb.append(requestId);
    sb.append(System.lineSeparator());
    sb.append("Error Key:");
    sb.append(errorKey);
    sb.append(System.lineSeparator());
    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment")
            + " LoLo getErrorResponse - "
            + errorKey,
        sb.toString());

    // format response object
    RestResponse restResponse = new RestResponse();
    restResponse.setSuccess(false);

    // build error response
    ResponseError responseError = new ResponseError();
    responseError.setErrorCode(applicationErrorService.getErrorCode(requestId, errorKey));
    responseError.setErrorDescription(errorKey);

    // build error response
    restResponse.addError(responseError);

    // create JSON response
    JsonNode jsonResponse = Json.toJson(restResponse);
    logService.debug(requestId, "OUT", "jsonResponse", jsonResponse.toString());

    // save response in database
    if ((requestId != null) && (!requestId.equals(""))) {
      RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
      if (requestHistory != null) {
        requestHistory.setResponse(jsonResponse.toString());
        requestHistoryService.saveRequestHistory(requestId, requestHistory);
      }
    }

    // return error response
    return Results.badRequest(jsonResponse);
  }

  @Override
  public Result getErrorResponse(List<ResponseError> errors, String requestId) {

    logService.debug(requestId, "IN", "requestId", requestId);

    // build email content with error details
    StringBuffer sb = new StringBuffer();
    sb.append("Request Id:");
    sb.append(requestId);
    sb.append(System.lineSeparator());

    for (ResponseError responseError : errors) {
      // log error
      logService.debug(requestId, "IN", "errors.code", responseError.getErrorCode());
      logService.debug(requestId, "IN", "errors.description", responseError.getErrorDescription());

      // add to email content
      sb.append("Error Key:");
      sb.append(responseError.getErrorCode());
      sb.append(" - ");
      sb.append(responseError.getErrorDescription());
      sb.append(System.lineSeparator());
    }

    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment") + " LoLo getErrorResponse - List",
        sb.toString());

    // format response object
    RestResponse restResponse = new RestResponse();
    restResponse.setSuccess(false);
    restResponse.setErrors(errors);

    // create JSON response
    JsonNode jsonResponse = Json.toJson(restResponse);
    logService.debug(requestId, "OUT", "jsonResponse", jsonResponse.toString());

    // save response in database
    if ((requestId != null) && (!requestId.equals(""))) {
      RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
      if (requestHistory != null) {
        requestHistory.setResponse(jsonResponse.toString());
        requestHistoryService.saveRequestHistory(requestId, requestHistory);
      }
    }

    // return error response
    return Results.badRequest(jsonResponse);
  }

  @Override
  public Result getErrorResponse(GenericRestException gre, String requestId) {

    logService.debug(requestId, "IN", "requestId", requestId);

    // build email content with error details
    StringBuffer sb = new StringBuffer();
    sb.append("Request Id:");
    sb.append(requestId);
    sb.append(System.lineSeparator());

    for (ResponseError responseError : gre.getResponseErrors()) {
      // log error
      logService.debug(requestId, "IN", "errors.code", responseError.getErrorCode());
      logService.debug(requestId, "IN", "errors.description", responseError.getErrorDescription());

      // add to email content
      sb.append("Error Key:");
      sb.append(responseError.getErrorCode());
      sb.append(" - ");
      sb.append(responseError.getErrorDescription());
      sb.append(System.lineSeparator());
    }

    sb.append("Error stack:");
    sb.append(ExceptionUtils.getStackTrace(gre));

    sb.append("Framework stack:");
    sb.append(ExceptionUtils.getStackTrace(new Throwable()));

    // send an email
    emailService.email(
        ConfigFactory.load().getString("application.noreplyemail"),
        ConfigFactory.load().getString("application.name"),
        ConfigFactory.load().getString("application.devemail"),
        "LoLo Developer",
        ConfigFactory.load().getString("application.environment")
            + " LoLo getErrorResponse - GenericRestException",
        sb.toString());

    // format response object
    RestResponse restResponse = new RestResponse();
    restResponse.setSuccess(false);
    restResponse.setErrors(gre.getResponseErrors());

    // create JSON response
    JsonNode jsonResponse = Json.toJson(restResponse);
    logService.debug(requestId, "OUT", "jsonResponse", jsonResponse.toString());

    // save response in database
    if ((requestId != null) && (!requestId.equals(""))) {
      RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
      if (requestHistory != null) {
        requestHistory.setResponse(jsonResponse.toString());
        requestHistoryService.saveRequestHistory(requestId, requestHistory);
      }
    }

    // return error response
    return Results.badRequest(jsonResponse);
  }

  @Override
  public Result getResponse(RestResponseBody restBody, String requestId) {

    logService.debug(requestId, "IN", "requestId", requestId);

    // format response object
    RestResponse restResponse = new RestResponse();
    restResponse.setBody(restBody);
    restResponse.setSuccess(true);

    // clean rest body variables of any sensitive data
    restBody = null;

    // create JSON response
    JsonNode jsonResponse = Json.toJson(restResponse);

    // clean restResponse of any sensitive data
    restResponse = null;

    logService.debug(requestId, "OUT", "jsonResponse", jsonResponse.toString());

    // save response in database
    if ((requestId != null) && (!requestId.equals(""))) {
      RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
      if (requestHistory != null) {
        requestHistory.setResponse(jsonResponse.toString());
        requestHistoryService.saveRequestHistory(requestId, requestHistory);
      }
    }

    // return response
    return Results.ok(jsonResponse);
  }

  @Override
  public Result getResponse(ObjectNode jsonBody, String requestId) {

    logService.debug(requestId, "IN", "jsonBody", jsonBody.toString());
    logService.debug(requestId, "IN", "requestId", requestId);

    // create custom JSON response node
    ObjectNode restResponse = Json.newObject();
    restResponse.put("success", true);
    restResponse.putPOJO("body", jsonBody);

    logService.debug(requestId, "OUT", "jsonResponse", restResponse.toString());

    // save response in database
    if ((requestId != null) && (!requestId.equals(""))) {
      RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);
      if (requestHistory != null) {
        requestHistory.setResponse(restResponse.toString());
        requestHistoryService.saveRequestHistory(requestId, requestHistory);
      }
    }

    // return response
    return Results.ok(restResponse);
  }

  @Override
  public String parseRequestToPlainText(Request request) {

    return parseRequestToPlainText(UUID.randomUUID().toString(), request);
  }

  @Override
  public String parseRequestToPlainText(String requestId, Request request) {

    String contentType = new String();

    // build request string
    StringBuffer requestString = new StringBuffer();

    requestString
        .append(request.method())
        .append(request.path())
        .append(System.lineSeparator())
        .append(request.uri())
        .append(System.lineSeparator())
        .append("Remote Address: ")
        .append(request.remoteAddress())
        .append(System.lineSeparator());

    Iterator<Entry<String, List<String>>> it = request.getHeaders().toMap().entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, List<String>> pair = it.next();

      if (request.hasHeader(pair.getKey().toString())) {
        requestString
            .append(pair.getKey().toString())
            .append(": ")
            .append(request.header(pair.getKey().toString()).get())
            .append(System.lineSeparator());
        if (pair.getKey().toString().equals("Content-Type")) {
          contentType = request.header(pair.getKey().toString()).get();
        }
      }
    }

    requestString.append(System.lineSeparator());

    if (contentType != null && request.method().equals("POST")) {
      switch (contentType) {
        case "text/plain":
          requestString.append(request.body().asText());
          break;
        case "application/json":
          requestString.append(request.body().asJson().toString());
          break;
        case "application/xml":
        case "text/xml":
        case "application/XXX+xml":
          requestString.append(request.body().asXml().toString());
          break;
        case "application/x-www-form-urlencoded":
          Map<String, String[]> formMap = request.body().asFormUrlEncoded();
          for (Map.Entry<String, String[]> entry : formMap.entrySet()) {
            StringBuilder value = new StringBuilder();

            for (int i = 0; i < entry.getValue().length; i++) {
              value = value.append(entry.getValue()[i]);
            }
            requestString
                .append(entry.getKey().toString())
                .append(": ")
                .append(value.toString())
                .append(System.lineSeparator());
          }

          break;
        default:
          logService.debug(requestId, "L", "switch statment", "CASE NOT FOUND");
          break;
      }
    }

    return requestString.toString();
  }

  /** Extract all errors from form fields into a plain list of errors */
  @Override
  public List<ResponseError> getErrorResponseList(
      List<ValidationError> formFieldErrors, String requestId) {

    List<ResponseError> response = new ArrayList<ResponseError>();

    for (ValidationError validationError : formFieldErrors) {
      for (String message : validationError.messages()) {
        if (message.equals("error.invalid")) {
          // build error response
          ResponseError responseError = new ResponseError();
          responseError.setErrorCode(
              applicationErrorService.getErrorCode(
                  requestId, ErrorMessage.ERROR_INVALID_TYPE_USED));
          responseError.setErrorDescription(ErrorMessage.ERROR_INVALID_TYPE_USED);

          response.add(responseError);
        } else {
          // build error response
          ResponseError responseError = new ResponseError();
          responseError.setErrorCode(applicationErrorService.getErrorCode(requestId, message));
          responseError.setErrorDescription(message);

          response.add(responseError);
        }
      }
    }

    return response;
  }

  @Override
  public <T extends RequestHeader> String getRequestId(T request) {

    // Initialise response
    String response = "";

    // check if header exists
    if (request.hasHeader(ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID)) {
      try {
        // try to get the header value
        response = request.header(ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID).get();
      } catch (Exception e) {
        // header value extraction failed
        response = "";
      }
    }

    // if result is empty, we have no request Id, and in this case we generate one which is not
    // valid but good for logs
    if ((response == null) || (response.equals(""))) {
      // first we check if we did not generated before already
      if (request.attrs().containsKey(Attrs.HEADER_CUSTOM_REQUESTID)) {
        response = request.attrs().get(Attrs.HEADER_CUSTOM_REQUESTID);
      }

      // if we did not put it before we generate it we put it now
      if ((response == null) || (response.equals(""))) {
        response = utilsService.generateRandomString(16);
      }
    }

    // return value converted to string
    return response.toString();
  }

  @Override
  public String getRequestAuthorizationToken(Request request) {

    // Initialise response
    String response = "";

    // check if header exists
    if (request.hasHeader(Http.HeaderNames.AUTHORIZATION)) {
      try {
        // try to get the header value
        response = request.header(Http.HeaderNames.AUTHORIZATION).get();
      } catch (Exception e) {
        // header value extraction failed
        response = "";
      }
    }

    // if result is empty, we have no request Id, and in this case we generate one which is not
    // valid but good for logs
    if (response == null) {
      response = "";
    }

    // return value converted to string
    return response.toString();
  }

  @Override
  public String getRequestUserAgent(Request request) {

    // Initialise response
    String response = "";

    // check if header exists
    if (request.hasHeader(Http.HeaderNames.USER_AGENT)) {
      try {
        // try to get the header value
        response = request.header(Http.HeaderNames.USER_AGENT).get();
      } catch (Exception e) {
        // header value extraction failed
        response = "";
      }
    }

    // if result is empty, we have no request Id, and in this case we generate one which is not
    // valid but good for logs
    if (response == null) {
      response = "";
    }

    // return value converted to string
    return response.toString();
  }
}
