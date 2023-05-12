package app.ro.iss.lolopay.controllers.BankAccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_GB;
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
public class CreateGbTests extends WithCustomApplication {

  private static String userId;
  private static String providerId;
  private static String bankAccountId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.BankAccountController.createGb().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("BankAccount", "CreateGbTests");
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
        .POST(String.format(MANGO_CREATE_BANKACCOUNT_GB.getUri(), providerId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }

  /** Create natural user */
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
  }

  /** Create account when there is no user */
  @Test
  public void test002_CreateGb_NoUser() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", ObjectId.get().toString());

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create account with valid data */
  @Test
  public void test003_CreateGb_SuccessCase() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertNotEquals("", bankAccount.getId().toString());
    assertEquals(userId, bankAccount.getUserId());

    // fill some variables
    bankAccountId = bankAccount.getId().toString();
  }

  /** Create account with provider error */
  @Test
  public void test004_CreateGb_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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

  /** Create account with required accountNumber field */
  @Test
  public void test012_CreateGb_AccountNumberRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_REQUIRED,
        error.getErrorDescription());
  }

  /** Create account with long accountNumber field */
  @Test
  public void test013_CreateGb_AccountNumberMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_MAXLENGTH,
        error.getErrorDescription());
  }

  /** Create account with invalid accountNumber field */
  @Test
  public void test014_CreateGb_AccountNumberInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_INVALID, error.getErrorDescription());
  }

  /** Create account with long customTag field */
  @Test
  public void test018_CreateGb_CustomTagMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_CREATEBANKACCOUNT_TAG_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with required userId field */
  @Test
  public void test019_CreateGb_UserIdRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_REQUIRED, error.getErrorDescription());
  }

  /** Create account with invalid userId field */
  @Test
  public void test020_CreateGb_UserIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INVALID, error.getErrorDescription());
  }

  /** Create account with required ownerName field */
  @Test
  public void test021_CreateGb_OwnerNameRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERNAME_REQUIRED, error.getErrorDescription());
  }

  /** Create account with long ownerName field */
  @Test
  public void test022_CreateGb_OwnerNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERNAME_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with required ownerAddress field */
  @Test
  public void test023_CreateGb_OwnerAddressRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
        ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERADDRESS_REQUIRED, error.getErrorDescription());
  }

  /** Create account with required ownerAddress.addressLine1 field */
  @Test
  public void test024_CreateGb_OwnerAddressAddressLine1Required() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_LINE1_REQUIRED, error.getErrorDescription());
  }

  /** Create account with long ownerAddress.addressLine1 field */
  @Test
  public void test025_CreateGb_OwnerAddressAddressLine1MaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_LINE1_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with long ownerAddress.addressLine2 field */
  @Test
  public void test026_CreateGb_OwnerAddressAddressLine2MaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_LINE2_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with required ownerAddress.city field */
  @Test
  public void test027_CreateGb_OwnerAddressCityRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_CITY_REQUIRED, error.getErrorDescription());
  }

  /** Create account with long ownerAddress.city field */
  @Test
  public void test028_CreateGb_OwnerAddressCityMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_CITY_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with long ownerAddress.county field */
  @Test
  public void test029_CreateGb_OwnerAddressCountyMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_COUNTY_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with required ownerAddress.country field */
  @Test
  public void test030_CreateGb_OwnerAddressCountryRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_COUNTRY_REQUIRED, error.getErrorDescription());
  }

  /** Create account with invalid ownerAddress.country field */
  @Test
  public void test031_CreateGb_OwnerAddressCountryInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_COUNTRY_INVALID, error.getErrorDescription());
  }

  /** Create account with long ownerAddress.postalCode field */
  @Test
  public void test032_CreateGb_OwnerAddressPostalCodeMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_POSTALCODE_MAXLENGTH, error.getErrorDescription());
  }

  /** Create account with required ownerAddress.county field */
  @Test
  public void test033_CreateGb_OwnerAddressCountyRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_COUNTY_REQUIRED, error.getErrorDescription());
  }

  /** Create account with required ownerAddress.postalCode field */
  @Test
  public void test034_CreateGb_OwnerAddressPostalCodeRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    assertEquals(ErrorMessage.ERROR_ADDRESS_POSTALCODE_REQUIRED, error.getErrorDescription());
  }

  /** Create account with missing all required fields */
  @Test
  public void test035_CreateGb_AllRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

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
    errors.add(ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERNAME_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERADDRESS_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_SORTCODE_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_REQUIRED);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(5, responseErrors.length);
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

    // delete created bank account from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankAccount.class).filter("id", bankAccountId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
