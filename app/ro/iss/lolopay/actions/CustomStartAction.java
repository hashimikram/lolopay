package ro.iss.lolopay.actions;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class CustomStartAction extends play.mvc.Action.Simple {
  @Inject LogService logService;

  @Inject CoreService coreService;

  @Inject UtilsService utilsService;

  @Override
  public CompletionStage<Result> call(Request request) {

    // get a request id per this session
    String requestId = coreService.getRequestId(request);

    // enter log header
    logService.header(
        requestId, "LoLo", request.path(), requestId, request.remoteAddress(), request.uri());

    // save requestId for all other logs
    request = request.addAttr(Attrs.HEADER_CUSTOM_REQUESTID, requestId);

    // delegate request
    return delegate.call(request);
  }
}
