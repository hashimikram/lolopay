package ro.iss.lolopay.actions;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import play.data.FormFactory;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;

public class CustomValidJsonAction extends play.mvc.Action.Simple {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject LogService logService;

  @Override
  public CompletionStage<Result> call(Request request) {

    // get a request id per this session
    String requestId = coreService.getRequestId(request);

    if (request.method().equals("POST")) {
      // get Json from body
      JsonNode json = request.body().asJson();

      if (null == json) {
        // log the error
        logService.error(requestId, "L", "errors", ErrorMessage.ERROR_INVALID_REQUEST_BODY);

        // there is no request id at this point
        return coreService.getErrorResponsePromise(ErrorMessage.ERROR_INVALID_REQUEST_BODY, null);
      } else {
        logService.debug(requestId, "L", "body", json.toString());
      }
    }

    return delegate.call(request);
  }
}
