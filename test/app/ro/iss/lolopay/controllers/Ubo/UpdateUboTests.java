package app.ro.iss.lolopay.controllers.Ubo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CREATE_LEGAL_USER;
import static shared.ProviderApis.MANGO_CREATE_UBO;
import static shared.ProviderApis.MANGO_CREATE_UBO_DECLARATION;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_SUBMIT_UBO_DECLARATION;
import static shared.ProviderApis.MANGO_UPDATE_UBO;
import static shared.Providers.MANGO;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.fasterxml.jackson.databind.JsonNode;
import com.mangopay.entities.Ubo;
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
import ro.iss.lolopay.jobs.ProcessCallbacksJob;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.UboDeclarationStatus;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateUboTests extends WithCustomApplication {

  private static User userLegal;
  private static Document document;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Ubo", "UpdateUboTests");
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
        .POST(MANGO_CREATE_LEGAL_USER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_CREATE_UBO_DECLARATION.getUri(), "74513987"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_CREATE_UBO.getUri(), "74513987", "74713704"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(String.format(MANGO_UPDATE_UBO.getUri(), "74513987", "74713704", "77640535"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_SUBMIT_UBO_DECLARATION.getUri(), "74513987", "74713704"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }

  /** Create natural user */
  @Test
  public void test001_CreateLegalUser() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.UserController.createLegal().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // get data from response as object
    userLegal = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userLegal);
  }

  @Test
  public void test002_CreateUboDeclaration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.createUboDeclaration(userLegal.getId()).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode body = jsonResponse.findPath("body");

    document = Json.fromJson(body.findPath("document"), Document.class);

    System.out.println("document id " + document.getProviderId());
    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals(DocumentStatus.CREATED, document.getStatus());
    assertEquals(userLegal.getId(), document.getUserId());
  }

  @Test
  public void test003_CreateUbo() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.createUbo(userLegal.getId(), document.getId()).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode body = jsonResponse.findPath("body");

    Ubo Ubo = Json.fromJson(body.findPath("ubo"), Ubo.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals("Cornel", Ubo.getFirstName());
    assertEquals("Zgardan", Ubo.getLastName());
    assertEquals("RO", Ubo.getNationality().toString());
    assertEquals("Bucharest", Ubo.getBirthplace().getCity());
  }

  @Test
  public void test004_UpdateUbo_UboIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri =
        routes.UboController.updateUbo(
                userLegal.getId(), document.getId(), (new ObjectId()).toString())
            .url();

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
    assertEquals(ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test005_UpdateUboTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode body = jsonResponse.findPath("body");

    Ubo Ubo = Json.fromJson(body.findPath("ubo"), Ubo.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals("Bob", Ubo.getFirstName());
    assertEquals("Snow", Ubo.getLastName());
    assertEquals("BE", Ubo.getNationality().toString());
    assertEquals("Namur", Ubo.getBirthplace().getCity());
  }

  @Test
  public void test006_UpdateUboTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();
    responseHttpStatus = BAD_REQUEST;

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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

  @Test
  public void test007_UpdateUboTests_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(" ", document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_USERID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test008_UpdateUboTests_UserIdRegexValid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo("ASDFASD3243.", document.getId(), "77640535").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_CREATEUBO_USERID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test009_UpdateUboTests_DocIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), " ", "77640535").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_CREATEUBO_DOCID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test010_UpdateUboTests_DocIdRegexValid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), "31232hdh12h.", "77640535").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_CREATEUBO_DOCID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test011_UpdateUboTests_FirstNameRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_FNAME_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test012_UpdateUboTests_LastNameRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_LNAME_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test013_UpdateUboTests_BDateRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_BDATE_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test014_UpdateUboTests_NationalityRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_NATIONALITY_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test015_UpdateUboTests_FirstNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_FNAME_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test016_UpdateUboTests_LastNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_LNAME_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test017_UpdateUboTests_BdateInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_BDATE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test018_UpdateUboTests_NationalityInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_NATIONALITY_INVALID, error.getErrorDescription());
  }

  @Test
  public void test019_UpdateUboTests_UserIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri =
        routes.UboController.updateUbo("5e3c36982195819a6873faaa", document.getId(), "77640535")
            .url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_USERID_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test020_UpdateUboTests_DocIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri =
        routes.UboController.updateUbo(userLegal.getId(), "5e3c36982195819a6873faaa", "77640535")
            .url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_DOCID_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test021_UpdateUbo_UboIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), " ").url();

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
    assertEquals(ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test022_ChangeStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(document.getProviderId())
            .concat("&EventType=UBO_DECLARATION_REFUSED&Date=1585008000");
    System.out.println("CreateUbo - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    UboDeclaration uboDeclaration =
        datastoreTestAccount.createQuery(UboDeclaration.class).filter("id", document.getId()).get();
    assertEquals(UboDeclarationStatus.REFUSED, uboDeclaration.getStatus());
  }

  @Test
  public void test023_UpdateUboTests_InvalidStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.updateUbo(userLegal.getId(), document.getId(), "77640535").url();

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
    assertEquals(ErrorMessage.ERROR_CREATEUBO_INVALID_STATUS, error.getErrorDescription());
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

    // delete ubo declarations
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(UboDeclaration.class).filter("userId", userLegal.getId()));

    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(ProcessedCallback.class)
            .filter("parameters.RessourceId", document.getProviderId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
