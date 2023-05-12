package app.ro.iss.lolopay.controllers.Ubo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_LEGAL_USER;
import static shared.ProviderApis.MANGO_CREATE_UBO_DECLARATION;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_SUBMIT_UBO_DECLARATION;
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
public class SubmitUboDeclarationTests extends WithCustomApplication {

  private static User userLegal;
  private static Server server;
  private static Document document;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Ubo", "SubmitUboDeclarationTests");
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
        .GET(String.format(MANGO_SUBMIT_UBO_DECLARATION.getUri(), "74513987", "74713704"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(String.format(MANGO_SUBMIT_UBO_DECLARATION.getUri(), "74513987", "74713704"))
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
  public void test002_CreateUboDeclarationTests() throws Exception {

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

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals(DocumentStatus.CREATED, document.getStatus());
    assertEquals(userLegal.getId(), document.getUserId());
  }

  @Test
  public void test003_SubmitUboDeclarationTests_UserIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(" ", document.getId()).url();

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
        ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test004_SubmitUboDeclarationTests_UserIdRegexValid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration("423432xsadsad.sad", document.getId()).url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test005_SubmitUboDeclarationTests_DocIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(userLegal.getId(), " ").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test006_SubmitUboDeclarationTests_DocIdRegexValid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(userLegal.getId(), "423432xsadsad.sad").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test007_SubmitUboDeclarationTests_UserIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri =
        routes.UboController.submitUboDeclaration(ObjectId.get().toString(), document.getId())
            .url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test008_SubmitUboDeclarationTests_DocIdInexistent() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri =
        routes.UboController.submitUboDeclaration(userLegal.getId(), ObjectId.get().toString())
            .url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INEXISTENT, error.getErrorDescription());
  }

  @Test
  public void test009_SubmitUboDeclarationTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(userLegal.getId(), document.getId()).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode body = jsonResponse.findPath("body");

    Document documentAfterSubmit = Json.fromJson(body.findPath("document"), Document.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals("74713704", documentAfterSubmit.getProviderId().toString());
    assertEquals("1581064069", documentAfterSubmit.getCreatedAt().toString());
  }

  @Test
  public void test010_SubmitUboDeclarationTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();
    responseHttpStatus = BAD_REQUEST;

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(userLegal.getId(), document.getId()).url();

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
    assertEquals(ErrorMessage.ERROR_PROVIDER, error.getErrorCode());
  }

  @Test
  public void test011_SubmitUboDeclarationTests_ChangeStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(document.getProviderId())
            .concat("&EventType=UBO_DECLARATION_REFUSED&Date=1584698400");
    System.out.println("SubmitUboDeclaration - urlCallback=" + urlCallback);

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
  public void test012_SubmitUboDeclarationTests_InvalidStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UboController.submitUboDeclaration(userLegal.getId(), document.getId()).url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_SUBMITUBODECLARATION_INVALID_STATUS, error.getErrorDescription());
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
