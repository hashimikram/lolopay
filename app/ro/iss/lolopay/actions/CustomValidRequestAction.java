package ro.iss.lolopay.actions;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.services.definition.RequestHistoryService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;

public class CustomValidRequestAction extends play.mvc.Action.Simple {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject private RequestHistoryService requestHistoryService;

  @Inject LogService logService;

  @Override
  public CompletionStage<Result> call(Request request) {

    // check request id - can't really happen anymore
    if (!request.hasHeader(ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID)) {
      return coreService.getErrorResponsePromise(ErrorMessage.ERROR_HEADER_REQUESTID_MISSING, null);
    }

    // get a request id per this session
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "L", "start", "Request Check User Agent and Request ID");

    // validate request id
    if (!requestId.matches(ApplicationConstants.REGEX_VALIDATE_UUID)) {
      logService.error(requestId, "L", "requestId", "RequestId header is not valid");
      return coreService.getErrorResponsePromise(ErrorMessage.ERROR_HEADER_REQUESTID_INVALID, null);
    }

    logService.debug(requestId, "L", "requestId", requestId);

    // check user agent
    if (!request.hasHeader(Http.HeaderNames.USER_AGENT)) {
      logService.error(requestId, "L", "requestId", "User-Agent header missing");
      return coreService.getErrorResponsePromise(ErrorMessage.ERROR_HEADER_USERAGENT_MISSING, null);
    }

    // check if request id exists in database
    RequestHistory existingRequestHistory = requestHistoryService.findByRequestId(requestId);

    if (existingRequestHistory != null) {
      logService.error(requestId, "L", "requestId", "RequestId header is duplicated");
      return coreService.getErrorResponsePromise(
          ErrorMessage.ERROR_HEADER_REQUESTID_DUPLICATED, null);
    }

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(request);
    logService.debug(requestId, "L", "fullRequest", fullRequestString);

    // save request
    RequestHistory newRequestHistory = new RequestHistory();
    newRequestHistory.setRequestId(requestId);
    newRequestHistory.setRequest(fullRequestString);
    requestHistoryService.saveRequestHistory(requestId, newRequestHistory);

    // move forward
    return delegate.call(request);
  }
}
