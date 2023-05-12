package app.ro.iss.lolopay.controllers.Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
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
import ro.iss.lolopay.services.implementation.DatabaseImplementation;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginTests extends WithApplication {

  protected static Injector appInjector;
  protected static DatabaseService databaseService;
  protected static Datastore datastoreTestApplication;

  protected static Account testAccount;
  protected static ro.iss.lolopay.models.database.Application testApplication;
  private static String testName;

  private static HttpHelper httpHelper;
  protected static String requestId;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Authentication", "LoginTests");
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
  }

  @Test
  public void test001_Login() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    TokenSet token = Json.fromJson(jsonResponse.at("/body/tokenSet"), TokenSet.class);

    assertNotNull(token.getAutheticationToken());
  }

  @Test
  public void test002_LoginTests_InvalidPassword() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_INVALID_CREDENTIALS, responseError.getErrorDescription());
  }

  @Test
  public void test003_LoginTests_AccountIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_ACCOUNTID_REQUIRED, responseError.getErrorDescription());
  }

  @Test
  public void test004_LoginTests_AccountIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", "sadasda");
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_ACCOUNTID_INVALID, responseError.getErrorDescription());
  }

  @Test
  public void test005_LoginTests_AppIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_APPID_REQUIRED, responseError.getErrorDescription());
  }

  @Test
  public void test006_LoginTests_AppIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", "32432");

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_APPID_INVALID, responseError.getErrorDescription());
  }

  @Test
  public void test007_LoginTests_InvalidCredentials() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_INVALID_CREDENTIALS, responseError.getErrorDescription());
  }

  @Test
  public void test008_LoginTests_PasswordRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_LOGIN_PASSWORD_REQUIRED, responseError.getErrorDescription());
  }

  @Test
  public void test009_LoginTests_RequestIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.AuthenticationController.login().url();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("accountId", testAccount.getId());
    bodyParameters.put("applicationId", testApplication.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, null, uri, bodyParameters, " ", JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(ErrorMessage.ERROR_HEADER_REQUESTID_INVALID, responseError.getErrorDescription());
  }

  /** Gets called after every test */
  @After
  public void afterEachTest() {

    // delete requestId
    datastoreTestApplication.delete(
        datastoreTestApplication.createQuery(RequestHistory.class).filter("requestId", requestId));
  }
}
