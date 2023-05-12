package app.ro.iss.lolopay.controllers.BankCards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.PFS_BANK_PAYMENT;
import static shared.ProviderApis.PFS_CHANGE_STATUS;
import static shared.ProviderApis.PFS_CREATE_CARD;
import static shared.ProviderApis.PFS_TRANSFER_TO;
import static shared.Providers.MANGO;
import static shared.Providers.PFS;
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
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecuteBankPaymentTests extends WithCustomApplication {

  private static String userId;
  private static String bankCardId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.BankCardsController.executeBankPayment().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("BankCards", "ExecuteBankPaymentTests");
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
        .POST(PFS_CREATE_CARD.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, PFS, responseHttpStatus))
        .POST(PFS_CHANGE_STATUS.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, PFS, responseHttpStatus))
        .POST(PFS_TRANSFER_TO.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, PFS, responseHttpStatus))
        .POST(PFS_BANK_PAYMENT.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, PFS, responseHttpStatus))
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
    userId = user.getId();
  }

  /** Create card */
  @Test
  public void test002_CreateCard() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    testName = httpHelper.getMethodName();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", userId);

    String url = routes.BankCardsController.createCard().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, url, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    bankCardId = jsonResponse.findPath("body").findPath("id").asText();

    assertEquals(true, responseSuccess);
  }

  @Test
  public void test003_ExecuteBankPayment_CardNotFound() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", ObjectId.get().toString());

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_INEXISTENT_CARD, error.getErrorDescription());
  }

  @Test
  public void test003_TransferToWallet() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    testName = httpHelper.getMethodName();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("cardId", bankCardId);

    String url = routes.BankCardsController.transferTo().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, url, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    bankCardId = jsonResponse.findPath("body").findPath("id").asText();

    assertEquals(true, responseSuccess);
  }

  @Test
  public void test004_ExecuteBankPayment_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // perform asserts
    assertEquals(true, responseSuccess);
  }

  @Test
  public void test005_ExecuteBankPayment_CardRequired() throws Exception {

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_CARDID_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test006_ExecuteBankPayment_CardInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", "asd@.md");

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_CARDID_INVALID, error.getErrorDescription());
  }

  @Test
  public void test007_ExecuteBankPayment_BeneficiaryNameRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(
        ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test008_ExecuteBankPayment_BeneficiaryNameInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(
        ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_INVALID, error.getErrorDescription());
  }

  @Test
  public void test009_ExecuteBankPayment_IbanInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_CREDITORIBAN_INVALID, error.getErrorDescription());
  }

  @Test
  public void test010_ExecuteBankPayment_BicInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_INVALID, error.getErrorDescription());
  }

  @Test
  public void test011_ExecuteBankPayment_AmountRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(
        ErrorMessage.ERROR_BANKPAYMENT_PAYMENTAMOUNT_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test012_ExecuteBankPayment_AmountInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_PAYMENTAMOUNT_INVALID, error.getErrorDescription());
  }

  @Test
  public void test013_ExecuteBankPayment_ReferenceRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_REQUIRED, error.getErrorDescription());
  }

  @Test
  public void test014_ExecuteBankPayment_ReferenceMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_MAXLENGTH, error.getErrorDescription());
  }

  @Test
  public void test015_ExecuteBankPayment_ReferenceInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_INVALID, error.getErrorDescription());
  }

  @Test
  public void test016_ChangeStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    String url = routes.BankCardsController.changeStatus().url();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // perform asserts
    assertEquals(true, responseSuccess);
  }

  @Test
  public void test017_ExecuteBankPayment_InvalidCardStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

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
    assertEquals(ErrorMessage.ERROR_BANKPAYMENT_CARD_INVALID_STATUS, error.getErrorDescription());
  }

  @Test
  public void test018_ExecuteBankPayment_BeneficiaryNameMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    boolean errorFound = false;
    for (ResponseError responseError : responseErrors) {
      if (responseError
          .getErrorDescription()
          .equals(ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_MAXLENGTH)) {
        errorFound = true;
        break;
      }
    }

    // perform asserts
    assertFalse(responseSuccess);
    assertNotNull(jsonErrors);
    assertTrue(errorFound);
  }

  @Test
  public void test019_ExecuteBankPayment_BicMinLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    boolean errorFound = false;
    for (ResponseError responseError : responseErrors) {
      if (responseError
          .getErrorDescription()
          .equals(ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_MINLENGTH)) {
        errorFound = true;
        break;
      }
    }

    // perform asserts
    assertFalse(responseSuccess);
    assertNotNull(jsonErrors);
    assertTrue(errorFound);
  }

  @Test
  public void test020_ExecuteBankPayment_BicMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("cardId", bankCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    boolean errorFound = false;
    for (ResponseError responseError : responseErrors) {
      if (responseError
          .getErrorDescription()
          .equals(ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_MAXLENGTH)) {
        errorFound = true;
        break;
      }
    }

    // perform asserts
    assertFalse(responseSuccess);
    assertNotNull(jsonErrors);
    assertTrue(errorFound);
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
        datastoreTestAccount.createQuery(BankCard.class).filter("id", bankCardId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankCardWallet.class).filter("cardId", bankCardId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
