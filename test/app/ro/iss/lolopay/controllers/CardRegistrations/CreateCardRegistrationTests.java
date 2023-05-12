package app.ro.iss.lolopay.controllers.CardRegistrations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
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
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateCardRegistrationTests extends WithCustomApplication {

  private static String userId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.CardRegistrationsController.createCardRegistration().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("CardRegistrations", "CreateCardRegistrationTests");
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
  public void test002_CreateCardRegistrationTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    assertTrue(responseSuccess);
  }

  @Test
  public void test003_CreateCardRegistrationTests_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_USERID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test004_CreateCardRegistrationTests_UserIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", "asdsa@.asd");

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_USERID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test005_CreateCardRegistrationTests_CurrencyRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_CURRENCY_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test006_CreateCardRegistrationTests_CurrencyInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_CURRENCY_INVALID, error.getErrorDescription());
  }

  @Test
  public void test007_CreateCardRegistrationTests_CardTypeInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_CARDTYPE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test008_CreateCardRegistrationTests_UserInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_USER_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test009_CreateCardRegistrationTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = BAD_REQUEST;

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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

  @Test
  public void test010_CreateCardRegistrationTests_TagInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
        ErrorMessage.ERROR_CREATECARDREGISTRATION_TAG_INVALID, error.getErrorDescription());
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
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
