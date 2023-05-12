package ro.iss.lolopay.services.definition;

import java.util.List;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.ImplementedBy;
import play.data.validation.ValidationError;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.responses.RestResponseBody;
import ro.iss.lolopay.services.implementation.CoreImplementation;

@ImplementedBy(CoreImplementation.class)
public interface CoreService {
  public ApplicationStamp getSystemApplicationStamp();

  public CompletionStage<Result> getErrorResponsePromise(String errorKey, String requestId);

  public Result getErrorResponse(String errorKey, String requestId);

  public Result getErrorResponse(List<ResponseError> errors, String requestId);

  public Result getErrorResponse(GenericRestException gre, String requestId);

  public Result getResponse(RestResponseBody restBody, String requestId);

  public Result getResponse(ObjectNode jsonBody, String requestId);

  public List<ResponseError> getErrorResponseList(
      List<ValidationError> formFieldErrors, String requestId);

  public String parseRequestToPlainText(Request request);

  public String parseRequestToPlainText(String requestId, Request request);

  public <T extends RequestHeader> String getRequestId(T requestHeader);

  public String getRequestAuthorizationToken(Request request);

  public String getRequestUserAgent(Request request);
}
