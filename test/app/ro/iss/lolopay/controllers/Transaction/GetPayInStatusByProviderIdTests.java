package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_TRANSACTION_DIRECTPAYIN;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_GET_PAYINS;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
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
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import ro.iss.lolopay.classes.CardRegistration;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GetPayInStatusByProviderIdTests extends WithCustomApplication {
  private static String userAId;
  private static String providerAId;
  private static String walletAId;
  private static String walletAProviderId;
  private static String walletACurrency;

  private static String cardRegistrationId;
  private static String cardRegistrationUrl;
  private static String preRegistrationData;
  private static String registrationData;
  private static String accessKey;
  private static String cardUserProviderId;
  private static String cardProviderId;
  private static String cardId;
  private static String testCardId;
  private static String transactionId;
  private static String transactionProviderId;
  private static String depositCardId;
  private static String transferId;

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "GetPayInStatusByProviderIdTests");
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
        .POST(MANGO_CARD_REGISTRATION_TOKEN.getUri())
        .routingTo(req -> HttpHelper.getProviderSharedResponse("card_token", MANGO, Http.Status.OK))
        .POST(MANGO_CREATE_NATURAL_USER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_WALLET.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_CARDREGISTRATION.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(String.format(MANGO_UPDATE_CARDREGISTRATION.getUri(), cardRegistrationId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_CARD.getUri(), testCardId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse("test004_GetDepositCard", MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), transactionProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447458"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .build();
  }
  /** Create natural user needed in all the other tests of this class */
  @Test
  public void test001_CreateNaturalUserA() throws Exception {

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
    userAId = user.getId().toString();
    providerAId = user.getProviderId();

    System.out.println("CreateTransferTests - userAId=" + userAId);
    System.out.println("CreateTransferTests - user providerAId=" + providerAId);
  }

  /** Create user wallet needed in all the other tests of this class */
  @Test
  public void test002_CreateUserAWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userAId);
    parameters.put("currency", "EUR");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    Wallet wallet = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(wallet);

    // fill some variables
    walletAId = wallet.getId().toString();
    walletAProviderId = wallet.getProviderId().toString();
    walletACurrency = wallet.getCurrency().toString();

    System.out.println("CreateTransferTests - walletAId=" + walletAId);
    System.out.println("CreateTransferTests - walletAProviderId=" + walletAProviderId);
    System.out.println("CreateTransferTests - walletCurrency=" + walletACurrency);
  }

  /**
   * Request creation of a card (make a card registration) so that a DirectPayIn transaction to put
   * money into walletA and to be able to do transfers in the next testcases
   */
  @Test
  public void test003_CreateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userAId);
    parameters.put("currency", "EUR");

    String url = routes.CardRegistrationsController.createCardRegistration().url();
    System.out.println("url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    CardRegistration cardRegistration =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("CREATED", cardRegistration.getStatus());

    // fill some variables
    cardRegistrationId = cardRegistration.getId().toString();
    // cardUserProviderId value it is same as providerId value
    cardUserProviderId = cardRegistration.getUserProviderId();

    // values needed for token authentication of creation of depositCard in next
    // testcase
    cardRegistrationUrl = cardRegistration.getCardRegistrationUrl();
    preRegistrationData = cardRegistration.getPreRegistrationData();
    accessKey = cardRegistration.getAccessKey();

    System.out.println("CreateTransferTests - cardRegistrationId=" + cardRegistrationId);
    System.out.println("CreateTransferTests - cardUserProviderId=" + cardUserProviderId);
    System.out.println("CreateTransferTests - cardRegistrationUrl=" + cardRegistrationUrl);
    System.out.println("CreateTransferTests - preRegistrationData=" + preRegistrationData);
    System.out.println("CreateTransferTests - accessKey=" + accessKey);

    // set up the cardId for testing purposes
    // NOTE: This value must match the value of:
    // CardId from test007_updateCardRegistration_mango_200.json
    // and Id from test007_updateCardRegistrationDeposit_mango_200.json
    testCardId = "12639018";
  }

  /**
   * Finally create the card that is needed so that a DirectPayIn transaction to put money into
   * walletA and to be able to do transfers in the next testcases
   */
  @Test
  public void test005_UpdateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // ====== SEND CARD DETAILS AND GET THE RegistrationData TO AN EXTERNAL
    // SERVER======
    // We have to send the fields AccessKey, PreregistrationData and the user card
    // details (card
    // number, expire date and CSC) to the tokenization server through a form posted
    // on the
    // CardRegistrationURL
    WSClient wsClient = appInjector.instanceOf(WSClient.class);
    WSRequest request = wsClient.url(cardRegistrationUrl);
    request.addHeader("Content-Type", "application/x-www-form-urlencoded");
    request.addHeader("Cache-Control", "no-cache");

    Map<String, String> bodyMap = new HashMap<String, String>();
    bodyMap.put("accessKeyRef", accessKey);
    bodyMap.put("data", preRegistrationData);
    bodyMap.put("cardNumber", "4706750000000025");
    bodyMap.put("cardExpirationDate", "1230");
    bodyMap.put("cardCvx", "148");

    WSResponse wsResponse = request.post(Json.toJson(bodyMap)).toCompletableFuture().get();
    registrationData = wsResponse.asJson().get(0).textValue();
    // ====== Update card registration with obtained registrationData value ======

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userAId);
    // parameters.put("registrationData", registrationData);
    parameters.put("registrationData", registrationData);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistrationId).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    CardRegistration cardRegistrationUpdate =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistrationUpdate);
    System.out.println("ResultCode" + cardRegistrationUpdate.getResultCode());
    System.out.println("ResultStatus " + cardRegistrationUpdate.getStatus());
    assertEquals("VALIDATED", cardRegistrationUpdate.getStatus());

    // fill some variables
    cardId = cardRegistrationUpdate.getId().toString();
    cardProviderId = cardRegistrationUpdate.getCardProviderId().toString();

    System.out.println("CreateTransferTests - cardId=" + cardId);
    System.out.println("CreateTransferTests - cardProviderId=" + cardProviderId);

    // get the deposit card Id
    depositCardId =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardProviderId)
            .get()
            .getId();
    System.out.println("CreateTransferTests - depositCardId=" + depositCardId);

    // sleep for 1 second
    Thread.sleep(1000);
  }

  @Test
  public void test006_CreateDirectPayIn_SucceededStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletACurrency);
    parameters.put("creditedWalletId", walletAId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transactionId = transactionPayIn.getId().toString();
    transactionProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transactionId = " + transactionId);
    System.out.println("CreateTransferTests - transactionProviderId = " + transactionProviderId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("Success", transactionPayIn.getResultMessage().toString());
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("70", transactionPayIn.getAmount().getValue().toString());
  }

  @Test
  public void test007_GetPayInStatusByProviderId_InvalidTransactionId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    System.out.println(transactionId + "  - id-ul tranzactiei");

    String url = routes.TransactionController.getPayInStatusByProviderId("sada543da").url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(
        ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INVALID_TRANSACTIONID,
        responseError.getErrorDescription());
  }

  @Test
  public void test008_GetPayInStatusByProviderId_InexistentPayIn() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.NOT_FOUND;

    String url = routes.TransactionController.getPayInStatusByProviderId("74447458").url();
    System.out.println("CreateTransferTests - url: " + url);

    System.out.println(transactionProviderId + " - providerID");

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(
        ErrorMessage.ERROR_GETPAYINBYPROVIDERID_INEXISTENT_PAYIN,
        responseError.getErrorDescription());
  }

  @Test
  public void test009_GetPayInStatusByProviderId_Success_StatusSucceeded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url =
        routes.TransactionController.getPayInStatusByProviderId(transactionProviderId).url();

    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNull(jsonErrors);
  }

  // needed because it's gives a transaction with Created status
  @Test
  public void test010_CreateDirectPayIn_CreatedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletACurrency);
    parameters.put("creditedWalletId", walletAId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transactionId = transactionPayIn.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("Success", transactionPayIn.getResultMessage().toString());
  }

  @Test
  public void test011_GetPayInStatusByProviderId_Success_StatusCreated() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url =
        routes.TransactionController.getPayInStatusByProviderId(transactionProviderId).url();

    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNull(jsonErrors);
  }

  /**
   * Create DirectPayIn with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine. //needed because it's gives a transaction with
   * Failed status
   */
  @Test
  public void test012_CreateDirectPayIn_FailedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String errorDescription =
        "One or several required parameters are missing or incorrect. An incorrect resource ID also raises this kind of error.";

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletACurrency);
    parameters.put("creditedWalletId", walletAId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transactionId = transactionPayIn.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("FAILED", transactionPayIn.getStatus().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionPayIn.getResultMessage()).findPath("Message").asText());
  }

  @Test
  public void test013_GetPayInStatusByProviderId_Success_StatusFailed() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url =
        routes.TransactionController.getPayInStatusByProviderId(transactionProviderId).url();

    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNull(jsonErrors);
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {
    super.afterEachTest();
    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionId));

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferId));

    // delete created user from database
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userAId));

    // delete created wallet from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletAId));

    // delete created deposit card from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("providerId", cardProviderId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
