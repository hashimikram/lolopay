package ro.iss.lolopay.controllers;

import javax.inject.Inject;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.responses.ResponseDummy;
import ro.iss.lolopay.services.definition.CoreService;

public class HomeController extends Controller {
  @Inject CoreService coreService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result index(Request request) {

    Logger.of(this.getClass()).debug("GET /");

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    ResponseDummy rd = new ResponseDummy();
    rd.setResponse("OK");
    return coreService.getResponse(rd, requestId);
  }
}
