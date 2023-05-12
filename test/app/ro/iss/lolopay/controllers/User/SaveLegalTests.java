package app.ro.iss.lolopay.controllers.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_LEGAL_USER;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_SAVE_LEGAL;
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
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SaveLegalTests extends WithCustomApplication {

  private static User userLegal;
  private static User userNatural;
  private static String providerId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.UserController.saveLegal().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("User", "SaveLegalTests");
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
        .PUT(String.format(MANGO_SAVE_LEGAL.getUri(), providerId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_LEGAL_USER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_NATURAL_USER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }

  @Test
  public void test000_CreateLegal() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.UserController.createLegal().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get data from response as object
    userLegal = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userLegal);

    // fill some variables
    providerId = userLegal.getProviderId();
  }

  /** Create natural user */
  @Test
  public void test001_SaveLegalTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get data from response as object
    userLegal = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userLegal);

    // fill some variables
    providerId = userLegal.getProviderId();
  }

  @Test
  public void test003_SaveLegalTests_CompanyEmailInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_SAVELEGALUSER_COMPANYEMAIL_INVALID, error.getErrorDescription());
  }

  @Test
  public void test006_SaveLegalTests_CompanyTypeInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_COMPANYTYPE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test008_SaveLegalTests_CountryInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_COUNTRY_INVALID, error.getErrorDescription());
  }

  @Test
  public void test009_SaveLegalTests_EmailInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_EMAIL_INVALID, error.getErrorDescription());
  }

  @Test
  public void test015_SaveLegalTests_NationalityInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_NATIONALITY_INVALID, error.getErrorDescription());
  }

  @Test
  public void test016_SaveLegalTests_CompanyRegistrationNumberInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_CREATELEGALUSER_COMPANY_REGISTRATION_NUMBER_INVALID,
        error.getErrorDescription());
  }

  @Test
  public void test017_SaveLegalTests_InexistentUser() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", "5e396cc5aa7c415826575aaa");

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_INEXISTENT_USER, error.getErrorDescription());
  }

  @Test
  public void test018_CreateNatural() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.UserController.createNatural().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get data from response as object
    userNatural = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userNatural);

    // fill some variables
    providerId = userNatural.getProviderId();
  }

  @Test
  public void test019_SaveLegalTests_UserTypeInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userNatural.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_USERTYPE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test020_SaveLegalTests_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_ID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test021_SaveLegalTests_UserIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", "asd@.as/d");

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVEUSER_ID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test022_SaveLegalTests_UserCompanyName() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_COMPANYNAME, error.getErrorDescription());
  }

  @Test
  public void test023_SaveLegalTests_FNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_FNAME_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test024_SaveLegalTests_LNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_LNAME_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test025_SaveLegalTests_BDateInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_BDATE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test026_SaveLegalTests_MobilePhoneMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_SAVELEGALUSER_MOBILEPHONE_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test027_SaveLegalTests_CompanyEmailMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(
        ErrorMessage.ERROR_SAVELEGALUSER_COMPANYEMAIL_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test028_SaveLegalTests_EmailMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_EMAIL_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test028_SaveLegalTests_TagMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_SAVELEGALUSER_TAG_MAXLENGTH, error.getErrorDescription());
  }

  /** Create natural user */
  @Test
  public void test029_SaveLegalTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();
    responseHttpStatus = BAD_REQUEST;

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userLegal.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(User.class).filter("id", userLegal.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(User.class).filter("id", userNatural.getId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
