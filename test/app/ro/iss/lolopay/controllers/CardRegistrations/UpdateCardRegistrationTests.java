package app.ro.iss.lolopay.controllers.CardRegistrations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
import static shared.Providers.MANGO;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.fasterxml.jackson.databind.JsonNode;
import classes.HttpHelper;
import classes.WithCustomApplication;
import play.BuiltInComponents;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import ro.iss.lolopay.classes.CardRegistration;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateCardRegistrationTests extends WithCustomApplication {

  private static String registrationData;
  private static CardRegistration cardRegistration = new CardRegistration();
  private static User user;

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  private static String cardRegistrationIdInexistent = ObjectId.get().toString();
  private static String errorDescription = "The ressource does not exist";
  String errorDescription400 =
      "One or several required parameters are missing or incorrect. An incorrect resource ID also raises this kind of error.";

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("CardRegistrations", "UpdateCardRegistrationTests");
  }

  @Override
  @Before
  public void beforeEachTest() {
    super.beforeEachTest();

    responseHttpStatus = Http.Status.OK;
    server = new Server.Builder().http(80).build(components -> getRoutingDsl(components));
  }

  private Router getRoutingDsl(BuiltInComponents components) {
    return RoutingDsl.fromComponents(components)
        .POST(MANGO_OAUTH.getUri())
        .routingTo(req -> HttpHelper.getProviderSharedResponse("oauth", MANGO, Http.Status.OK))
        .POST(MANGO_CREATE_NATURAL_USER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CARD_REGISTRATION_TOKEN.getUri())
        .routingTo(req -> HttpHelper.getProviderSharedResponse("card_token", MANGO, Http.Status.OK))
        .POST(MANGO_CREATE_CARDREGISTRATION.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(MANGO_CREATE_CARDREGISTRATION.getUri() + "/" + cardRegistration.getId())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_CARD.getUri(), "74803462"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse("test003_GetDepositCard", MANGO, responseHttpStatus))
        .PUT(String.format(MANGO_UPDATE_CARDREGISTRATION.getUri(), cardRegistrationIdInexistent))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }

  /** Create natural user */
  @Test
  public void test001_CreateNatural() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.UserController.createNatural().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // get data from response as object
    user = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(user);
  }

  @Test
  public void test002_CreateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", user.getId());
    parameters.put("currency", "EUR");

    String url = routes.CardRegistrationsController.createCardRegistration().url();
    System.out.println("url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    cardRegistration =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("CREATED", cardRegistration.getStatus());

    System.out.println("cardRegistration: " + utilsService.prettyPrintObject(cardRegistration));

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("CREATED", cardRegistration.getStatus());
    assertEquals(null, cardRegistration.getResultCode());
    assertEquals(user.getProviderId(), cardRegistration.getUserProviderId());
    assertEquals(null, cardRegistration.getRegistrationData());
    assertEquals(null, cardRegistration.getCardProviderId());
    assertEquals(CardType.CB_VISA_MASTERCARD, cardRegistration.getCardType());
    assertEquals(CurrencyISO.EUR, cardRegistration.getCurrency());
    assertNotEquals("", cardRegistration.getAccessKey());
    assertNotEquals("", cardRegistration.getPreRegistrationData());
    assertNotEquals("", cardRegistration.getCardRegistrationUrl());
    assertNotEquals("", cardRegistration.getReturnUrl());

    assertEquals(null, cardRegistration.getCustomTag());

    // cardRegistrationId = cardRegistration.getId();
    // values needed for token authentication of creation of depositCard in next testcase

    System.out.println(
        "UpdateCardRegistrationTests - cardRegistrationId=" + cardRegistration.getId());
    System.out.println(
        "UpdateCardRegistrationTests - cardRegistrationUrl="
            + cardRegistration.getCardRegistrationUrl());

    uri = routes.CardRegistrationsController.updateCardRegistration(cardRegistration.getId()).url();
  }

  @Test
  public void test003_UpdateCardRegistration_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // ====== SEND CARD DETAILS AND GET THE RegistrationData TO AN EXTERNAL
    // SERVER======
    // We have to send the fields AccessKey, PreregistrationData and the user card
    // details (card
    // number, expire date and CSC) to the tokenization server through a form posted
    // on the
    // CardRegistrationURL
    WSClient wsClient = appInjector.instanceOf(WSClient.class);
    WSRequest request = wsClient.url(cardRegistration.getCardRegistrationUrl());
    request.addHeader("Content-Type", "application/x-www-form-urlencoded");
    request.addHeader("Cache-Control", "no-cache");

    Map<String, String> bodyMap = new HashMap<String, String>();
    bodyMap.put("accessKeyRef", cardRegistration.getAccessKey());
    bodyMap.put("data", cardRegistration.getPreRegistrationData());
    bodyMap.put("cardNumber", "4706750000000025");
    bodyMap.put("cardExpirationDate", "1230");
    bodyMap.put("cardCvx", "148");

    WSResponse wsResponse = request.post(Json.toJson(bodyMap)).toCompletableFuture().get();
    registrationData = wsResponse.asJson().get(0).textValue();

    // ====== Update card registration with obtained registrationData value ======

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", user.getId());
    parameters.put("registrationData", registrationData);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistration.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    cardRegistration =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("VALIDATED", cardRegistration.getStatus());
    System.out.println("cardRegistration: " + cardRegistration);
  }

  @Test
  public void test004_UpdateCardRegistration_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_ID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test005_UpdateCardRegistration_UserIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", "asd@.asd");

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_ID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test006_UpdateCardRegistration_UserInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test007_UpdateCardRegistration_CardRegistrationNotFound() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.NOT_FOUND;

    uri =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistrationIdInexistent)
            .url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", user.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(errorDescription, error.getErrorDescription());
    assertEquals(ErrorMessage.ERROR_PROVIDER, error.getErrorCode());
  }

  @Test
  public void test008_UpdateCardRegistration_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);
    responseHttpStatus = BAD_REQUEST;

    uri = routes.CardRegistrationsController.updateCardRegistration(cardRegistration.getId()).url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", user.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_PROVIDER, error.getErrorCode());
    assertEquals(errorDescription400, error.getErrorDescription().toString());
  }

  @Test
  public void test009_UpdateCardRegistration_TagInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_UPDATECARDREGISTRATION_TAG_INVALID, error.getErrorDescription());
  }

  @Test
  public void test010_UpdateCardRegistration_RegistrationDataInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_UPDATECARDREGISTRATION_REGISTRATIONDATA_INVALID,
        error.getErrorDescription());
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {
    super.afterEachTest();

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    // delete created user from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(User.class).filter("id", user.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(DepositCard.class)
            .filter("providerId", cardRegistration.getCardProviderId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
