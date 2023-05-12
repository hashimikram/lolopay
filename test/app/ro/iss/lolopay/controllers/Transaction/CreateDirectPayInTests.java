package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_TRANSACTION_DIRECTPAYIN;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
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
public class CreateDirectPayInTests extends WithCustomApplication {
  private static String userId;
  private static String providerId;
  private static String walletId;
  private static String walletProviderId;
  private static String walletCurrency;
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
  private static String transactionFeeId;
  private static String depositCardId;

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.TransactionController.createDirectPayIn().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "CreateDirectPayInTests");
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
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
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

    System.out.println("CreateDirectPayInTests - userId=" + userId);
    System.out.println("CreateDirectPayInTests - user providerId=" + providerId);
  }

  /** Create user wallet needed in all the other tests of this class */
  @Test
  public void test002_CreateUserWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("currency", "EUR");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    Wallet wallet = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(wallet);

    // fill some variables
    walletId = wallet.getId().toString();
    walletProviderId = wallet.getProviderId().toString();
    walletCurrency = wallet.getCurrency().toString();

    System.out.println("CreateDirectPayInTests - walletId=" + walletId);
    System.out.println("CreateDirectPayInTests - walletProviderId=" + walletProviderId);
    System.out.println("CreateDirectPayInTests - walletCurrency=" + walletCurrency);
  }

  /** Request creation of a card (make a card registration) */
  @Test
  public void test003_CreateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
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

    // values needed for token authentication of creation of depositCard in next testcase
    cardRegistrationUrl = cardRegistration.getCardRegistrationUrl();
    preRegistrationData = cardRegistration.getPreRegistrationData();
    accessKey = cardRegistration.getAccessKey();

    System.out.println("CreateDirectPayInTests - cardRegistrationId=" + cardRegistrationId);
    System.out.println("CreateDirectPayInTests - cardUserProviderId=" + cardUserProviderId);
    System.out.println("CreateDirectPayInTests - cardRegistrationUrl=" + cardRegistrationUrl);
    System.out.println("CreateDirectPayInTests - preRegistrationData=" + preRegistrationData);
    System.out.println("CreateDirectPayInTests - accessKey=" + accessKey);

    // set up the cardId for testing purposes
    // NOTE: This value must match the value from:
    //       CardId from test004_updateCardRegistration_mango_200.json
    //   and Id from test004_updateCardRegistrationDeposit_mango_200.json
    testCardId = "12639018";
  }

  /** Finally create the card that is needed in all the other tests of this class */
  @Test
  public void test004_UpdateCardRegistration() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // ====== SEND CARD DETAILS AND GET THE RegistrationData TO AN EXTERNAL SERVER======
    // We have to send the fields AccessKey, PreregistrationData and the user card details (card
    // number, expire date and CSC) to the tokenization server through a form posted on the
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
    parameters.put("userId", userId);
    // parameters.put("registrationData", registrationData);
    parameters.put("registrationData", registrationData);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistrationId).url();
    System.out.println("CreateDirectPayInTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    CardRegistration cardRegistrationUpdate =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistrationUpdate);
    System.out.println("ResultCode" + cardRegistrationUpdate.getResultCode());
    System.out.println("Resultstatus " + cardRegistrationUpdate.getStatus());
    assertEquals("VALIDATED", cardRegistrationUpdate.getStatus());

    // fill some variables
    cardId = cardRegistrationUpdate.getId().toString();
    cardProviderId = cardRegistrationUpdate.getCardProviderId().toString();

    System.out.println("CreateDirectPayInTests - cardId=" + cardId);
    System.out.println("CreateDirectPayInTests - cardProviderId=" + cardProviderId);

    // get the deposit card Id
    depositCardId =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardProviderId)
            .get()
            .getId();
    System.out.println("CreateDirectPayInTests - depositCardId=" + depositCardId);
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test005_CreateDirectPayIn_SuccessCase_SmallZeroIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as INCLUDED <br>
   */
  @Test
  public void test006_CreateDirectPayIn_SuccessCase_BigZeroIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("55", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getSecureModeReturnUrl().toString());
    assertEquals(null, transactionPayIn.getBilling());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as NOT_INCLUDED <br>
   * Note: When the fee is not included, we will have 2 transactions created in db
   */
  @Test
  public void test007_CreateDirectPayIn_SuccessCase_SmallNotIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as NOT_INCLUDED <br>
   */
  @Test
  public void test008_CreateDirectPayIn_SuccessCase_BigNotIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("58", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getSecureModeReturnUrl().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("3", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED
   */
  @Test
  public void test009_CreateDirectPayIn_SuccessCase_SmallIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor missing <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   */
  @Test
  public void test010_CreateDirectPayIn_SuccessCase_SmallIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as NOT_INCLUDED <br>
   */
  @Test
  public void test011_CreateDirectPayIn_SuccessCase_BigIncludedForce() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("FORCE", transactionPayIn.getSecureMode().toString());
    assertEquals("55", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getSecureModeReturnUrl().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("3", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   */
  @Test
  public void test012_CreateDirectPayIn_SuccessCase_SmallIncludedForce() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("FORCE", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * Note: the status of the transactions will be FAILED because the amount is too small
   */
  @Test
  public void test013_CreateDirectPayIn_SuccessCase_TooSmallIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals(
        "Transaction amount is lower than minimum permitted amount",
        transactionPayIn.getResultMessage().toString());
    assertEquals("001012", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("5", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transactionFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateDirectPayInTests - transactionFeeId = " + transactionFeeId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayInFee);
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYIN_FEE", transactionPayInFee.getType().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("CREDIT_EUR", transactionPayInFee.getCreditedWalletId().toString());
  }

  /**
   * Create DirectPayIn with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine.
   */
  @Test
  public void test014_CreateDirectPayIn_ProviderFailureCase() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String errorDescription =
        "One or several required parameters are missing or incorrect. An incorrect resource ID also raises this kind of error.";

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    assertEquals("ERROR_PROVIDER", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());
  }

  /** Create direct PayIn with long customTag field */
  @Test
  public void test015_CreateDirectPayIn_CustomTagMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_TAG_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing creditWalletId field */
  @Test
  public void test016_CreateDirectPayIn_WalletIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLETID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid creditWalletId field */
  @Test
  public void test017_CreateDirectPayIn_WalletIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLETID_INVALID,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with creditWalletId field inexistent in DB */
  @Test
  public void test018_CreateDirectPayIn_NoWallet() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", ObjectId.get().toString());
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLET_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing debit funds field */
  @Test
  public void test019_CreateDirectPayIn_DebitFundsRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEBITFUNDS_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing amount value field */
  @Test
  public void test020_CreateDirectPayIn_AmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_VALUE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid amount value field */
  @Test
  public void test021_CreateDirectPayIn_AmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    // assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
    assertEquals(ErrorMessage.ERROR_INVALID_TYPE_USED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid (negative) amount value field */
  @Test
  public void test022_CreateDirectPayIn_NegativeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing amount currency field */
  @Test
  public void test023_CreateDirectPayIn_AmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid amount currency field */
  @Test
  public void test024_CreateDirectPayIn_AmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with mismatch amount currency field and wallet currency */
  @Test
  public void test025_CreateDirectPayIn_AmountCurrencyWalletMismatchInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_INVALID,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing fees field */
  @Test
  public void test026_CreateDirectPayIn_FeesRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEES_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing fee amount value field */
  @Test
  public void test027_CreateDirectPayIn_FeeAmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_VALUE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid fee amount value field */
  @Test
  public void test028_CreateDirectPayIn_FeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    // assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
    assertEquals(ErrorMessage.ERROR_INVALID_TYPE_USED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid (negative) fee amount value field */
  @Test
  public void test029_CreateDirectPayIn_NegativeFeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing fee amount currency field */
  @Test
  public void test030_CreateDirectPayIn_FeeAmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid fee amount currency field */
  @Test
  public void test031_CreateDirectPayIn_FeeAmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with mismatch amount currency and fee amount currency field */
  @Test
  public void test032_CreateDirectPayIn_FeeAmountCurrencyMismatch() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_CURRENCY_MISSMATCH,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with amount value smaller than fee amount value field */
  @Test
  public void test033_CreateDirectPayIn_AmountSmallerThanFee() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();

    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with amount value field set as zero */
  @Test
  public void test034_CreateDirectPayIn_AmountIsZero() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", cardId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing feeModel field */
  @Test
  public void test035_CreateDirectPayIn_FeeModelRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEEMODEL_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with empty feeModel field */
  @Test
  public void test036_CreateDirectPayIn_FeeModelRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEEMODEL_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid fee model field */
  @Test
  public void test037_CreateDirectPayIn_FeeModelInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEEMODEL_INVALID, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing secureModeReturnUrl field */
  @Test
  public void test038_CreateDirectPayIn_SecureModeReturnUrlRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with long SecureModeReturnUrl field */
  @Test
  public void test039_CreateDirectPayIn_SecureModeReturnUrlMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_MAXLENGTH,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid secureModeReturnUrl field */
  @Test
  public void test040_CreateDirectPayIn_SecureModeReturnUrlInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_INVALID,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing cardId field */
  @Test
  public void test041_CreateDirectPayIn_CardIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_CARDID_REQUIRED, responseError.getErrorDescription());
  }

  /** Create direct PayIn with long cardId field */
  @Test
  public void test042_CreateDirectPayIn_CardIdMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_CARDID_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create direct PayIn with missing secureMode field */
  @Test
  public void test043_CreateDirectPayIn_SecureModeRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODE_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with empty secureMode field */
  @Test
  public void test044_CreateDirectPayIn_SecureModeRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODE_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid secureMode field */
  @Test
  public void test045_CreateDirectPayIn_SecureModeInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODE_INVALID,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with invalid statement description field */
  @Test
  public void test046_CreateDirectPayIn_StatementDescriptionInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_STATEMENTDESC_INVALID,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with long statement description field */
  @Test
  public void test047_CreateDirectPayIn_StatementDescriptionMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", depositCardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_STATEMENTDESC_MAXLENGTH,
        responseError.getErrorDescription());
  }

  /** Create direct PayIn with long statement description field */
  @Test
  public void test048_CreateDirectPayIn_DepositCardInexistent() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    bodyParameters.put("cardId", cardId);

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
        ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEPOSITCARD_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create DirectPayIn with missing all required fields */
  @Test
  public void test049_CreateDirectPayIn_AllRequired() throws Exception {

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

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEES_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLETID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODE_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_DEBITFUNDS_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_CARDID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEEMODEL_REQUIRED);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(7, responseErrors.length);
  }

  /** Create DirectPayIn with missing all required fields */
  @Test
  public void test050_CreateDirectPayIn_AllInvalidMaxLength() throws Exception {

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

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_TAG_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_CREDITWALLETID_INVALID);
    // below error is for invalid amount
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    // below error is for invalid fees
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_FEEMODEL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODERETURNURL_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_CARDID_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_SECUREMODE_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_STATEMENTDESC_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEDIRECTPAYIN_STATEMENTDESC_MAXLENGTH);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(13, responseErrors.length);
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {
    super.afterEachTest();

    // delete created fee transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionFeeId));

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionId));

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    // delete created user from database
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userId));

    // delete created wallet from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletId));

    // delete created cardRegistration from database ????
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(CardRegistration.class).filter("id", cardRegistrationId));

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
