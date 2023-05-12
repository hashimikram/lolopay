package ro.iss.lolopay.controllers;

import java.util.List;
import javax.inject.Inject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.models.services.definition.RequestHistoryService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class HelperController extends Controller {
  @Inject private ApplicationErrorService applicationErrorService;

  @Inject private DatabaseService databaseService;

  @Inject private CoreService coreService;

  @Inject private WalletService walletService;

  @Inject private UserService userService;

  @Inject RequestHistoryService requestHistoryService;

  @Inject UtilsService utilsService;

  @Inject LogService logService;

  @Inject Config config;

  public Result test(Request request) {

    if (!isTestingRequest(request)) {
      return badRequest("Op not allowd in this environment");
    }

    return ok("Done");
  }

  @CustomStart
  public Result getErrors(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "GET /getErrors");

    if (!isTestingRequest(request) && !isFromLiveServer(request)) {
      return badRequest("Op not allowed from this environment");
    }

    List<ApplicationError> allErrors = applicationErrorService.getErrors("");

    return ok(Json.toJson(allErrors));
  }

  public Result testHook(Request request) {

    if (!isTestingRequest(request)) {
      return badRequest("Op not allowd in this environment");
    }

    Logger.of(this.getClass()).debug("Start /testHook");

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(request);
    Logger.of(this.getClass()).debug("FULL REQUEST: " + fullRequestString);
    return ok();
  }

  public Result getRequest(Request request, String requestId) {

    if (!isFromOffice(request)) {
      return badRequest("Op not allowd in this location");
    }

    // find request
    RequestHistory requestHistory = requestHistoryService.findByRequestId(requestId);

    if (requestHistory == null) {
      return notFound("Request Not Found");
    }

    String responsePretty;
    JsonNode responseNode = Json.parse(requestHistory.getResponse());
    ObjectMapper mapper = new ObjectMapper();
    try {
      responsePretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNode);
    } catch (JsonProcessingException e) {
      responsePretty = requestHistory.getResponse();
    }

    // create response
    StringBuffer response = new StringBuffer();
    response.append("Record Id: ").append(requestHistory.getId()).append(System.lineSeparator());
    response
        .append("Record Created At: ")
        .append(requestHistory.getCreatedAt())
        .append(System.lineSeparator());
    response
        .append("Record Created At: ")
        .append(
            utilsService.timeStampToDate(requestHistory.getCreatedAt(), "MM/dd/yyyy hh:mm:ss a"))
        .append(System.lineSeparator());
    response
        .append("Record Updated At: ")
        .append(requestHistory.getUpdatedAt())
        .append(System.lineSeparator());
    response
        .append("Record Updated At: ")
        .append(
            utilsService.timeStampToDate(requestHistory.getUpdatedAt(), "MM/dd/yyyy hh:mm:ss a"))
        .append(System.lineSeparator());
    response
        .append("Request Id: ")
        .append(requestId)
        .append(System.lineSeparator())
        .append(System.lineSeparator());
    response
        .append(
            "Request: ----------------------------------------------------------------------------------------------------------------------------------------- ")
        .append(System.lineSeparator())
        .append(requestHistory.getRequest())
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append(System.lineSeparator());
    response
        .append(
            "Response: ----------------------------------------------------------------------------------------------------------------------------------------- ")
        .append(System.lineSeparator())
        .append(responsePretty)
        .append(System.lineSeparator());

    // extract request string
    return ok(response.toString());
  }

  @CustomAuth
  public Result updateWalletBalance(Request request, String walletId, Integer amount) {

    if (!isTestingRequest(request)) {
      return badRequest("Op not allowd in this environment");
    }

    Logger.of(this.getClass()).debug("*** updateWalletBalance ***");

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    Wallet wallet1 = walletService.getWallet("", sessionAccount, walletId);
    walletService.updateWalletBalance("", sessionAccount, wallet1, amount);

    // return fake response
    return ok("Done update wallet balance!");
  }

  @CustomAuth
  public Result deleteUser(Request request, String userId) {

    if (!isTestingRequest(request)) {
      return badRequest("Op not allowd in this environment");
    }

    Logger.of(this.getClass()).debug("*** deleteUser ***");

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // delete user
    User user = userService.getUser("", sessionAccount, userId);
    databaseService.getConnection(sessionAccount.getId().toString()).delete(user);

    // return fake response
    return ok("Done delete user!");
  }

  @CustomAuth
  public Result deleteWallet(Request request, String walletId) {

    if (!isTestingRequest(request)) {
      return badRequest("Op not allowd in this environment");
    }

    Logger.of(this.getClass()).debug("*** deleteWallet ***");

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // delete user
    Wallet wallet = walletService.getWallet("", sessionAccount, walletId);
    databaseService.getConnection(sessionAccount.getId().toString()).delete(wallet);

    // return fake response
    return ok("Done delete wallet!");
  }

  @CustomStart
  private boolean isFromOffice(Request request) {

    if (request.remoteAddress().equals("5.2.206.244")
        || request.remoteAddress().equals("127.0.0.1")
        || request.remoteAddress().equals("localhost")
        || request.remoteAddress().equals("0:0:0:0:0:0:0:1")
        || request
            .remoteAddress()
            .matches("[0-1]{2}[\\.]{1}[0]{1}[\\.]{1}[0-1]{2}[\\.]{1}[0-9]{1,3}")) {
      return true;
    }
    return false;
  }

  @CustomStart
  private boolean isTestingRequest(Request request) {

    logService.debug(
        coreService.getRequestId(request),
        "IN",
        "request.remoteAddress()",
        request.remoteAddress());

    if (!ConfigFactory.load().getString("application.environment").equals("local")
        && !ConfigFactory.load().getString("application.environment").equals("test")
        && !ConfigFactory.load().getString("application.environment").equals("staging")) {
      return false;
    }
    return true;
  }

  @CustomStart
  private boolean isFromLiveServer(Request request) {

    logService.debug(
        coreService.getRequestId(request),
        "IN",
        "request.remoteAddress()",
        request.remoteAddress());

    // test environment
    if (request.remoteAddress().equals("52.3.172.69")) {
      return true;
    }

    // live environment
    if (request.remoteAddress().equals("10.0.22.22")) {
      return true;
    }

    return false;
  }

  /**
   * Method used by monitors to test if app is live
   *
   * @return
   */
  public Result monitor(Request request) {

    String mykey = config.getString("application.dbAccounts.cacheTime");

    return ok("System up\n" + "application.dbAccounts.cacheTime: " + mykey + "\n");
  }
}
