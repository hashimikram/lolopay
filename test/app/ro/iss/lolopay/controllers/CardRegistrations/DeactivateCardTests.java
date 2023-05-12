package app.ro.iss.lolopay.controllers.CardRegistrations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
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
import play.mvc.Http;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeactivateCardTests extends WithCustomApplication {

  private static String userId;
  private static String cardRegistrationProviderId;
  private static String cardProviderId;
  private static String cardId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("CardRegistrations", "DeactivateCardTests");
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
        .POST(MANGO_CREATE_CARDREGISTRATION.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(MANGO_CREATE_CARDREGISTRATION.getUri() + "/" + cardRegistrationProviderId)
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_CARD.getUri(), "12639018"))
        .routingTo(
            req -> httpHelper.getProviderResponse(testName + "Get", MANGO, responseHttpStatus))
        .PUT(String.format(MANGO_VIEW_CARD.getUri(), "12639018"))
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
    User user = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(user);

    // fill some variables
    userId = user.getId();
  }

  @Test
  public void test002_CreateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.CardRegistrationsController.createCardRegistration().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, url, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    assertTrue(responseSuccess);

    cardRegistrationProviderId =
        jsonResponse.get("body").get("cardRegistration").get("id").asText();
  }

  @Test
  public void test003_UpdateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistrationProviderId).url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            PUT, app, autheticationToken, url, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    assertTrue(responseSuccess);

    cardProviderId =
        jsonResponse.get("body").get("cardRegistration").get("cardProviderId").asText();
    uri = routes.CardRegistrationsController.deactivateCard(cardProviderId).url();
  }

  @Test
  public void test004_GetCardByProviderId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.CardRegistrationsController.getCardByProviderId(cardProviderId).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    assertTrue(responseSuccess);

    cardId = jsonResponse.get("body").get("depositCard").get("id").asText();
    uri = routes.CardRegistrationsController.deactivateCard(cardId).url();
  }

  @Test
  public void test005_DeactivateCardTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    assertTrue(responseSuccess);
  }

  @Test
  public void test006_DeactivateCardTests_InvalidCardId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);
    uri = routes.CardRegistrationsController.deactivateCard("asd@a.d").url();

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
        ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INVALID_CARDID, error.getErrorDescription());
  }

  @Test
  public void test007_DeactivateCardTests_InexistentDepositCard() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);
    uri = routes.CardRegistrationsController.deactivateCard(ObjectId.get().toString()).url();

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
        ErrorMessage.ERROR_DEACTIVATEDEPOSITCARD_INEXISTENT_DEPOSITCARD,
        error.getErrorDescription());
  }

  @Test
  public void test008_DeactivateCardTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);
    responseHttpStatus = BAD_REQUEST;
    uri = routes.CardRegistrationsController.deactivateCard(cardId).url();

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
    assertEquals(ErrorMessage.ERROR_PROVIDER, error.getErrorCode());
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
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("providerId", cardProviderId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
