package app.ro.iss.lolopay.controllers.BankAccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_DEACTIVATE;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_IBAN;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.Providers.MANGO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeactivateBankAccountTests extends WithCustomApplication {

  private static String userId;
  private static String providerId;
  private static String bankAccountId;
  private static String bankProviderId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.BankAccountController.deactivateBankAccount().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("BankAccount", "DeactivateBankAccountTests");
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
        .POST(String.format(MANGO_CREATE_BANKACCOUNT_IBAN.getUri(), providerId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(
            String.format(MANGO_CREATE_BANKACCOUNT_DEACTIVATE.getUri(), providerId, bankProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(
            String.format(MANGO_CREATE_BANKACCOUNT_DEACTIVATE.getUri(), providerId, bankProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }

  /** Create natural user needed in all the other tests of this class */
  @Test
  public void test001_CreateNaturalUser() throws Exception {

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
    userId = user.getId().toString();
    providerId = user.getProviderId();

    System.out.println("DeactivateBankAccountTests - userId=" + userId);
    System.out.println("DeactivateBankAccountTests - user providerId=" + providerId);
  }

  /** Create IBAN account needed in all the other tests of this class */
  @Test
  public void test002_CreateIbanAccount() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

    String url = routes.BankAccountController.createIban().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    BankAccount bankAccount =
        Json.fromJson(jsonResponse.at("/body/bankAccount"), BankAccount.class);

    // perform asserts
    assertNotNull(bankAccount);

    // fill some variables
    bankAccountId = bankAccount.getId().toString();
    bankProviderId = bankAccount.getProviderId().toString();

    System.out.println("DeactivateBankAccountTests - bankAccountId=" + bankAccountId);
    System.out.println("DeactivateBankAccountTests - bankProviderId=" + bankProviderId);
  }

  /** Create account when there is no user */
  @Test
  public void test003_DeactivateBankAccount_NoUser() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());
    bodyParameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_USER,
        responseError.getErrorDescription());
  }

  /** Create account when there is no user */
  @Test
  public void test004_DeactivateBankAccount_NoBankAccount() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);
    bodyParameters.put("bankAccountId", ObjectId.get().toString());

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INEXISTENT_BANKACCOUNT,
        responseError.getErrorDescription());
  }

  /** Deactivate account with missing userId field */
  @Test
  public void test005_DeactivateBankAccount_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_USERID_REQUIRED, error.getErrorDescription());
  }

  /** Deactivate account with invalid userId field */
  @Test
  public void test006_DeactivateBankAccount_UserIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_USERID, error.getErrorDescription());
  }

  /** Deactivate account with missing BankAccountId field */
  @Test
  public void test007_DeactivateBankAccount_BankAccountIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_BANKACCOUNTID_REQUIRED,
        error.getErrorDescription());
  }

  /** Deactivate account with invalid BankAccountId field */
  @Test
  public void test008_DeactivateBankAccount_BankAccountIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_BANKACCOUNTID,
        error.getErrorDescription());
  }

  /** Deactivate account with missing new status field */
  @Test
  public void test009_DeactivateBankAccount_NewStatusRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_NEWSTATUS_REQUIRED, error.getErrorDescription());
  }

  /** Deactivate account with invalid new status field */
  @Test
  public void test010_DeactivateBankAccount_NewStatusInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(ErrorMessage.ERROR_INVALID_TYPE_USED, error.getErrorDescription());
  }

  /** Deactivate IBAN account successfully */
  @Test
  public void test011_DeactivateBankAccount_SuccessCase() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/bankAccount");

    // get data from body to object
    BankAccount bankAccount = Json.fromJson(userNode, BankAccount.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(bankAccount);
    assertEquals(bankAccountId, bankAccount.getId().toString());
    assertEquals(userId, bankAccount.getUserId());
    assertEquals("IBAN", bankAccount.getType().toString());
    assertEquals("customTagIban", bankAccount.getCustomTag().toString());
  }

  /** Deactivate IBAN account with provider error response */
  @Test
  public void test012_DeactivateBankAccount_ProviderFailureCase() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String errorDescription =
        "One or several required parameters are missing or incorrect. An incorrect resource ID also raises this kind of error.";

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

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
    assertEquals(
        errorDescription, Json.parse(error.getErrorDescription()).findPath("Message").asText());
  }

  /** Deactivate IBAN account with missing all required fields */
  @Test
  public void test013_DeactivateBankAccount_AllRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_USERID_REQUIRED);
    errors.add(ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_BANKACCOUNTID_REQUIRED);
    errors.add(ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_NEWSTATUS_REQUIRED);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(3, responseErrors.length);
  }

  /** Deactivate IBAN account with invalid fields */
  @Test
  public void test014_DeactivateBankAccount_AllInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_USERID);
    errors.add(ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_BANKACCOUNTID);
    errors.add(ErrorMessage.ERROR_INVALID_TYPE_USED);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(3, responseErrors.length);
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

    // delete created bank accounts from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankAccount.class).filter("id", bankAccountId));

    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));

    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
