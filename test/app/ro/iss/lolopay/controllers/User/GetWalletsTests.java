package app.ro.iss.lolopay.controllers.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.Providers.MANGO;
import java.util.HashMap;
import java.util.Map;
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
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GetWalletsTests extends WithCustomApplication {

  private static String userId;
  private static String walletId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("User", "GetWalletsTests");
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
        .POST(MANGO_CREATE_WALLET.getUri())
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

    String uri = routes.UserController.createNatural().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get data from response as object
    User user = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(user);

    // fill some variables
    userId = user.getId();

    GetWalletsTests.uri = routes.UserController.getWallets(userId, 1, 10).url();
  }

  @Test
  public void test002_CreateWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.WalletController.create().url();
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    walletId = jsonResponse.get("body").get("wallet").get("id").asText();

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // perform asserts
    assertEquals(true, responseSuccess);
  }

  @Test
  public void test003_GetWalletsTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    Wallet[] wallets = Json.fromJson(jsonResponse.at("/body/wallets"), Wallet[].class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals(1, wallets.length);
  }

  @Test
  public void test004_GetWalletsTests_InexistentUser() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UserController.getWallets("5e3bed96af28e18b574e5aaa", 1, 10).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_GETWALLETS_INEXISTENT_USER, error.getErrorDescription());
  }

  @Test
  public void test005_GetWalletsTests_InvalidUserId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UserController.getWallets("asd@.sd", 1, 10).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_GETWALLETS_INVALID_USERID, error.getErrorDescription());
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
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
