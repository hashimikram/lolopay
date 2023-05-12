package app.ro.iss.lolopay.controllers.Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mongodb.morphia.Datastore;
import com.fasterxml.jackson.databind.JsonNode;
import classes.HttpHelper;
import play.inject.Injector;
import play.libs.Json;
import play.test.WithApplication;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.UtilsService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RefreshTests extends WithApplication {

  protected static Injector appInjector;
  protected static DatabaseService databaseService;
  protected static Datastore datastoreTestApplication;
  protected static UtilsService utilsService;

  protected static Account testAccount;
  protected static ro.iss.lolopay.models.database.Application testApplication;
  private static String testName;

  private static HttpHelper httpHelper;
  private static String url = routes.AuthenticationController.refresh().url();
  protected static String requestId;

  private static TokenSet tokenSet;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Authentication", "RefreshTests");
  }

  @Before
  public void beforeEachTest() {
    appInjector = app.injector();

    requestId = UUID.randomUUID().toString();

    databaseService = appInjector.instanceOf(DatabaseImplementation.class);
    datastoreTestApplication = databaseService.getMainConnection();

    testAccount = databaseService.getMainConnection().createQuery(Account.class).asList().get(0);

    testApplication =
        databaseService
            .getConnection(testAccount.getId())
            .createQuery(ro.iss.lolopay.models.database.Application.class)
            .asList()
            .get(0);
    utilsService = appInjector.instanceOf(UtilsImplementation.class);
  }

  @Test
  public void test001_Login() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // utilsService.setNowTimestamp(1585229813l);

    String url = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    Date date = new Date();
    long currentDate; // 7 200 000 milliseconds - two hours
    currentDate = date.getTime() - 7200000l;
    utilsService.setNowTimestamp(currentDate);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, url, bodyParameters, requestId, JSON);

    tokenSet = Json.fromJson(jsonResponse.at("/body/tokenSet"), TokenSet.class);

    assertNotNull(tokenSet.getAutheticationToken());
  }

  @Test
  public void test002_Refresh_tokenInvalidOrExpired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    Date date = new Date();
    long currentDate; // 7 200 000 milliseconds - two hours
    currentDate = date.getTime();
    utilsService.setNowTimestamp(currentDate);

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, tokenSet.getRefreshToken(), url, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(
        ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INVALID_OR_EXPIRED,
        responseError.getErrorDescription());
  }

  @Test
  public void test003_Refresh_AuthorizationHeaderMissing() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // Getting the current date
    Date date = new Date();
    // Display the time in miliseconds

    System.out.println(date.getTime() + " current date");

    JsonNode jsonResponse = httpHelper.executeRequest(GET, app, null, url, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(
        ErrorMessage.ERROR_AUTHORIZATION_HEADER_MISSING, responseError.getErrorDescription());
  }
  // made this login because when need a valid token
  // for the success test of the refresh method
  @Test
  public void test004_Login() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // utilsService.setNowTimestamp(1585229813l);

    String url = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    Date date = new Date();
    long currentDate; // 7 200 000 milliseconds - two hours
    currentDate = date.getTime() - 3600000l; // -1 hour
    utilsService.setNowTimestamp(currentDate);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, url, bodyParameters, requestId, JSON);

    tokenSet = Json.fromJson(jsonResponse.at("/body/tokenSet"), TokenSet.class);

    assertNotNull(tokenSet.getAutheticationToken());
  }

  @Test
  public void test005_Refresh_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    Date date = new Date();
    long currentDate;
    currentDate = date.getTime() + 1800000l; // + 30 mins
    utilsService.setNowTimestamp(currentDate);

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, tokenSet.getRefreshToken(), url, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    TokenSet tokenRefresh = Json.fromJson(jsonResponse.at("/body/tokenSet"), TokenSet.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals((date.getTime() + 5400000) / 1000, tokenRefresh.getAutheticationExpiresAt());
  }

  @Test
  public void test006_Login() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // utilsService.setNowTimestamp(1585229813l);

    String url = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    Date date = new Date();
    long currentDate; // 7 200 000 milliseconds - two hours
    currentDate = date.getTime() - 1800000l;
    utilsService.setNowTimestamp(currentDate);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, url, bodyParameters, requestId, JSON);

    tokenSet = Json.fromJson(jsonResponse.at("/body/tokenSet"), TokenSet.class);

    assertNotNull(tokenSet.getAutheticationToken());
  }

  @Test
  public void test007_Refresh_AfterOneHour() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Date date = new Date();
    long currentDate;
    // 7 200 000 milliseconds - two hours
    currentDate = date.getTime() + 1800000l;

    utilsService.setNowTimestamp(currentDate);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, tokenSet.getRefreshToken(), url, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(
        ErrorMessage.ERROR_AUTHORIZATION_TOKEN_INVALID_OR_EXPIRED,
        responseError.getErrorDescription());
  }

  /** Gets called after every test */
  @After
  public void afterEachTest() {

    // delete requestId
    datastoreTestApplication.delete(
        datastoreTestApplication.createQuery(RequestHistory.class).filter("requestId", requestId));
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {
    utilsService.setNowTimestamp(0L);
  }
}
