package ro.iss.lolopay.controllers;

import javax.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import play.data.Form;
import play.data.FormFactory;
import play.http.HttpEntity;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.classes.AuthenticationResponse;
import ro.iss.lolopay.classes.CardRegistration;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.DepositCardService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;
import ro.iss.lolopay.responses.ResponseCreateCardRegistration;
import ro.iss.lolopay.responses.ResponseGetDepositCard;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.SecurityService;
import ro.iss.lolopay.services.definition.UtilsService;

public class CardRegistrationsController extends Controller {
  @Inject CoreService coreService;

  @Inject LogService logService;

  @Inject UserService userService;

  @Inject FormFactory formFactory;

  @Inject BusinessService businessService;

  @Inject UtilsService utilsService;

  @Inject SecurityService securityService;

  @Inject DatabaseService databaseService;

  @Inject DepositCardService depositCardService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createCardRegistration(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /createCardRegistration");

    // Move json to object
    Form<RequestCreateCardRegistration> restForm =
        formFactory.form(RequestCreateCardRegistration.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateCardRegistration requestCreateCardRegistration = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    User user =
        userService.getUser(requestId, sessionAccount, requestCreateCardRegistration.getUserId());

    // validate user
    if (user == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATECARDREGISTRATION_USER_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATECARDREGISTRATION_USER_INEXISTENT, requestId);
    }

    // check user is registered to provider
    if ((user.getProviderId() == null) || (user.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATECARDREGISTRATION_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATECARDREGISTRATION_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      CardRegistration cardRegistration =
          businessService.createCardRegistrations(
              requestId, sessionAccount, user, requestCreateCardRegistration);
      logService.debug(
          requestId, "L", "cardRegistration", utilsService.prettyPrintObject(cardRegistration));

      ResponseCreateCardRegistration responseCreateCardRegistration =
          new ResponseCreateCardRegistration();
      responseCreateCardRegistration.setCardRegistration(cardRegistration);

      return coreService.getResponse(responseCreateCardRegistration, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result updateCardRegistration(Request request, String cardRegistrationProviderId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "PUT /updateCardRegistration");

    // check id
    if ((cardRegistrationProviderId == null) || cardRegistrationProviderId.equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_UPDATECARDREGISTRATION_CARDREGISTRATIONID_INVALID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATECARDREGISTRATION_CARDREGISTRATIONID_INVALID, requestId);
    }

    // Move json to object
    Form<RequestUpdateCardRegistration> restForm =
        formFactory.form(RequestUpdateCardRegistration.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestUpdateCardRegistration requestUpdateCardRegistration = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    User user =
        userService.getUser(requestId, sessionAccount, requestUpdateCardRegistration.getUserId());

    // validate user
    if (user == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_INEXISTENT);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_INEXISTENT, requestId);
    }

    // check user is registered to provider
    if ((user.getProviderId() == null) || (user.getProviderId().equals(""))) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      CardRegistration cardRegistration =
          businessService.updateCardRegistrations(
              requestId,
              sessionAccount,
              sessionApplication,
              user,
              cardRegistrationProviderId,
              requestUpdateCardRegistration);
      logService.debug(
          requestId, "L", "cardRegistration", utilsService.prettyPrintObject(cardRegistration));

      ResponseCreateCardRegistration responseCreateCardRegistration =
          new ResponseCreateCardRegistration();
      responseCreateCardRegistration.setCardRegistration(cardRegistration);

      return coreService.getResponse(responseCreateCardRegistration, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result deactivateCard(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId, "IN", "start", "PUT /createCardRegistration/deactivateCard/" + cardId);

    if ((cardId == null) || cardId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INVALID_CARDID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGOID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INVALID_CARDID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INVALID_CARDID, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // retrieve logged application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    // find DepositCard
    DepositCard depositCard =
        depositCardService.getDepositCardById(requestId, sessionAccount, cardId);

    if (depositCard == null) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INEXISTENT_DEPOSITCARD);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INEXISTENT_DEPOSITCARD, requestId);
    }

    try {
      businessService.deactivateDepositCard(
          requestId, sessionAccount, sessionApplication, depositCard);

      // return transaction
      ResponseGetDepositCard responseGetDepositCard = new ResponseGetDepositCard();
      responseGetDepositCard.setDepositCard(depositCard);

      return coreService.getResponse(responseGetDepositCard, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardByProviderId(Request request, String cardProviderId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId, "IN", "start", "GET /createCardRegistration/card/" + cardProviderId);

    if ((cardProviderId == null) || cardProviderId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETDEPOSITCARD_INVALID_PROVIDERCARDID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDEPOSITCARD_INVALID_PROVIDERCARDID, requestId);
    }

    // check id is valid
    if (!cardProviderId.matches(ApplicationConstants.REGEX_VALIDATE_DIGITS)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETDEPOSITCARD_INVALID_PROVIDERCARDID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDEPOSITCARD_INVALID_PROVIDERCARDID, requestId);
    }

    // retrieve session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // find DepositCard
    DepositCard depositCard =
        depositCardService.getDepositCardByProviderId(requestId, sessionAccount, cardProviderId);
    logService.error(requestId, "L", "depositCard", Json.toJson(depositCard));

    if (depositCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETDEPOSITCARD_INEXISTENT_DEPOSITCARD);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDEPOSITCARD_INEXISTENT_DEPOSITCARD, requestId);
    }

    try {
      // return transaction
      ResponseGetDepositCard responseGetDepositCard = new ResponseGetDepositCard();
      responseGetDepositCard.setDepositCard(depositCard);

      return coreService.getResponse(responseGetDepositCard, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  // TODO delete this controller
  @CustomStart
  public Result receiveToken(Request request, String cardRegistrationId, String data) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /createCardRegistration");

    logService.debug(requestId, "L", "cardRegistrationId", cardRegistrationId);
    logService.debug(requestId, "L", "data", data);
    HttpResponse<String> response = null;

    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);
    ro.iss.lolopay.models.database.Application testApplication =
        databaseService
            .getConnection(sessionAccount.getId().toString())
            .createQuery(ro.iss.lolopay.models.database.Application.class)
            .asList()
            .get(0);

    try {
      AuthenticationResponse authenticationResponse =
          securityService.authenticateApplication(
              "",
              sessionAccount.getId().toString(),
              testApplication.getId().toString(),
              testApplication.getApplicationPassword());
      TokenSet sessionToken =
          securityService.registerSession("", authenticationResponse, "localhost");

      response =
          Unirest.put("http://localhost:9001/cardregistrations/" + cardRegistrationId)
              .header(
                  ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID,
                  utilsService.generateRandomString(16))
              .header(Http.HeaderNames.CONTENT_TYPE, "application/json")
              .header(Http.HeaderNames.USER_AGENT, "Testing Platform")
              .header(Http.HeaderNames.AUTHORIZATION, sessionToken.getAutheticationToken())
              .body("registrationData=" + data)
              .asString();
      logService.debug(requestId, "L", "response", utilsService.prettyPrintObject(response));
    } catch (UnirestException e) {
      e.printStackTrace();
    }

    Result returnResult =
        new Result(response.getStatus(), HttpEntity.fromString(response.getBody(), "utf-8"));
    return returnResult;
  }
}
