package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_TRANSACTION_DIRECTPAYIN;
import static shared.ProviderApis.MANGO_CREATE_TRANSFER;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_GET_PAYINS;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
import static shared.ProviderApis.MANGO_VIEW_TRANSFER;
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
import ro.iss.lolopay.jobs.ProcessCallbacksJob;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateTransferTests extends WithCustomApplication {
  private static String userAId;
  private static String providerAId;
  private static String userBId;
  private static String providerBId;
  private static String walletAId;
  private static String walletAProviderId;
  private static String walletACurrency;
  private static String walletBId;
  private static String walletBProviderId;
  private static String walletBCurrency;
  private static String USDWalletBId;
  private static String USDWalletBProviderId;
  private static String USDWalletBCurrency;

  private static String transferId;
  private static String transferWithoutTagId;
  private static String transferProviderId;
  private static String transferFeeId;
  private static String testTransferProviderId;

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

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";
  private static String errorDescription =
      "One or several required parameters are missing or incorrect. An incorrect resource ID also raises this kind of error.";
  private static String errorMessage = "Fraud suspected by the bank";
  private static String errorCode = "008006";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.TransactionController.createTransfer().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "CreateTransferTests");
  }

  @Override
  @Before
  public void beforeEachTest() {
    super.beforeEachTest();

    responseHttpStatus = Http.Status.OK;
    server = new Server.Builder().http(80).build(components -> getRoutingDsl(components));
  }

  private Router getRoutingDsl(BuiltInComponents components) {

    // set up the routes for the localhost server
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
                httpHelper.getProviderResponse("test007_GetDepositCard", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447455"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    "test008_ViewDirectPayIn", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSFER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_TRANSFER.getUri(), testTransferProviderId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.substring(0, 8) + "ViewTransfer", MANGO, responseHttpStatus))
        .build();
  }

  /**
   * Create natural user needed in all the other tests of this class. <br>
   * This user it will be used for a wallet that must be debited.
   */
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

  /**
   * Create natural user needed in all the other tests of this class. <br>
   * This user it will be used for a wallet that must be credited.
   */
  @Test
  public void test002_CreateNaturalUserB() throws Exception {

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
    userBId = user.getId().toString();
    providerBId = user.getProviderId();

    System.out.println("CreateTransferTests - userBId=" + userBId);
    System.out.println("CreateTransferTests - user providerBId=" + providerBId);
  }

  /**
   * Create userA wallet needed in all the other tests of this class.<br>
   * This is the debited wallet.
   */
  @Test
  public void test003_CreateUserAWallet() throws Exception {

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
   * Create userB wallet needed in all the other tests of this class.<br>
   * This is the credited wallet.
   */
  @Test
  public void test004_CreateUserBWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userBId);
    parameters.put("currency", "EUR");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    Wallet wallet = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(wallet);

    // fill some variables
    walletBId = wallet.getId().toString();
    walletBProviderId = wallet.getProviderId().toString();
    walletBCurrency = wallet.getCurrency().toString();

    System.out.println("CreateTransferTests - walletBId=" + walletBId);
    System.out.println("CreateTransferTests - walletBProviderId=" + walletBProviderId);
    System.out.println("CreateTransferTests - walletBCurrency=" + walletBCurrency);
  }

  /**
   * Create userB wallet needed in all the other tests of this class.<br>
   * This is the credited wallet.
   */
  @Test
  public void test005_CreateUserBWalletUSD() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userBId);
    parameters.put("currency", "USD");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    Wallet wallet = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(wallet);

    // fill some variables
    USDWalletBId = wallet.getId().toString();
    USDWalletBProviderId = wallet.getProviderId().toString();
    USDWalletBCurrency = wallet.getCurrency().toString();

    System.out.println("CreateTransferTests - USDWalletBId=" + USDWalletBId);
    System.out.println("CreateTransferTests - USDWalletBProviderId=" + USDWalletBProviderId);
    System.out.println("CreateTransferTests - USDWalletBCurrency=" + USDWalletBCurrency);
  }

  /**
   * Request creation of a card (make a card registration) so that a DirectPayIn transaction to put
   * money into walletA and to be able to do transfers in the next testcases
   */
  @Test
  public void test006_CreateCardRegistration() throws Exception {

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

    // values needed for token authentication of creation of depositCard in next testcase
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
    //       CardId from test007_updateCardRegistration_mango_200.json
    //   and Id from test007_updateCardRegistrationDeposit_mango_200.json
    testCardId = "12639018";
  }

  /**
   * Finally create the card that is needed so that a DirectPayIn transaction to put money into
   * walletA and to be able to do transfers in the next testcases
   */
  @Test
  public void test007_UpdateCardRegistration() throws Exception {

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
  }

  /**
   * Create DirectPayIn to put money into walletA and to be able to do transfers in the next
   * testcases
   */
  @Test
  public void test008_CreateDirectPayIn() throws Exception {

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
    assertEquals("105", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreateDirectPayIn", transactionPayIn.getCustomTag().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // =============== START - SET PAYIN TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transactionProviderId
            + "&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println("CreateTransferTests - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYIN TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that the wallet is updated accordingly
    Thread.sleep(2000);

    // read debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the balances
    assertEquals("105", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ===============
    // set up the Id of the json file that must be already loaded for test009
    // this is the value of Id field from
    // test009_CreateTransfer_SuccessCase_ZeroFeeIncluded_mango_200.json
    testTransferProviderId = "75093249";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test009_CreateTransfer_SuccessCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    transferProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);
    System.out.println("CreateTransferTests - transferProviderId = " + transferProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreateTransfer", transactionPayIn.getCustomTag().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("97", walletDetailsA.getBalance().getValue().toString());
    assertEquals("8", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("0", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (transferProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
      transferProviderId = transactionTransfer.getProviderId();
      System.out.println(
          "CreateTransferTests - transferProviderId from DB = " + transferProviderId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("97", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================
    // set up the Id of the json file that must be already loaded for test010
    // this is the value of Id field from
    // test010_CreateTransfer_SuccessCase_FeeIncluded_mango_200.json
    testTransferProviderId = "75093251";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test010_CreateTransfer_SuccessCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    transferProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);
    System.out.println("CreateTransferTests - transferProviderId = " + transferProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreateTransfer", transactionPayIn.getCustomTag().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - transferFeeId = " + transferFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("83", walletDetailsA.getBalance().getValue().toString());
    assertEquals("14", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (transferProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
      transferProviderId = transactionTransfer.getProviderId();
      System.out.println(
          "CreateTransferTests - transferProviderId from DB = " + transferProviderId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("83", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("20", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ==========================
    // set up the Id of the json file that must be already loaded for test010
    // this is the value of Id field from
    // test010_CreateTransfer_SuccessCase_FeeIncluded_mango_200.json
    testTransferProviderId = "75177972";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test011_CreateTransfer_SuccessCase_ZeroFeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    transferProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);
    System.out.println("CreateTransferTests - transferProviderId = " + transferProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreateTransfer", transactionPayIn.getCustomTag().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("76", walletDetailsA.getBalance().getValue().toString());
    assertEquals("7", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("20", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (transferProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
      transferProviderId = transactionTransfer.getProviderId();
      System.out.println(
          "CreateTransferTests - transferProviderId from DB = " + transferProviderId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581928816";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("76", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("27", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());
    // ======================

    // set up the Id of the json file that must be already loaded for test010
    // this is the value of Id field from
    // test010_CreateTransfer_SuccessCase_FeeIncluded_mango_200.json
    testTransferProviderId = "75177974";
  }

  /**
   * Create Transfer with valid data: <br>
   * customTag is not present <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test012_CreateTransfer_SuccessCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    transferWithoutTagId = transferId;
    transferProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);
    System.out.println("CreateTransferTests - transferProviderId = " + transferProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("9", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getCustomTag());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - transferFeeId = " + transferFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("66", walletDetailsA.getBalance().getValue().toString());
    assertEquals("10", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("27", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (transferProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
      transferProviderId = transactionTransfer.getProviderId();
      System.out.println(
          "CreateTransferTests - transferProviderId from DB = " + transferProviderId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581928816";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that the wallet to get the chance to be updated
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("66", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ===============
    // set up the Id of the json file that must be already loaded for test01a
    // this is the value of Id field from
    // test01a_CreateTransfer_SuccessCase_FailedStatus_mango_200.json
    testTransferProviderId = "75177975";
  }

  /**
   * Create Transfer with valid data: <br>
   * customTag is present <br>
   * feeModel is set as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note1: When the fee is different than zero we will have two transactions created in db <br>
   * Note2: This test will always fail while running on sandbox, but the purspose is to check that
   * when provider responds with 200 code and failed status then also in our DB we will update
   * accordingly the transaction status and wallet content
   */
  @Test
  public void test013_CreateTransfer_SuccessCase_FailedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    transferProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);
    System.out.println("CreateTransferTests - transferProviderId = " + transferProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("34", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreateTransfer", transactionPayIn.getCustomTag().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - transferFeeId = " + transferFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("31", walletDetailsA.getBalance().getValue().toString());
    assertEquals("35", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (transferProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
      transferProviderId = transactionTransfer.getProviderId();
      System.out.println(
          "CreateTransferTests - transferProviderId from DB = " + transferProviderId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS FAILED ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderId
            + "&EventType=TRANSFER_NORMAL_FAILED&Date=1581928816";
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS FAILED - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that transaction is updated in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(errorMessage, transactionDB.getResultMessage().toString());
    assertEquals(errorCode, transactionDB.getResultCode().toString());

    // read the transaction fee details from DB
    Transaction transactionFeeDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionFeeDB.getStatus().toString());
    assertEquals(errorMessage, transactionFeeDB.getResultMessage().toString());
    assertEquals(errorCode, transactionFeeDB.getResultCode().toString());

    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("66", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());
  }

  /**
   * Transfer with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine.
   */
  @Test
  public void test014_CreateTransfer_ProviderFailureCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertEquals("", transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // sleep for 2 seconds so that DB to be updated accordingly
    Thread.sleep(2000);

    // read the transaction TRANSFER details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the balances
    assertEquals("66", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Transfer with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine.
   */
  @Test
  public void test015_CreateTransfer_ProviderFailureCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("9", transactionPayIn.getAmount().getValue().toString());
    assertEquals("", transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - transferFeeId = " + transferFeeId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("", transactionPayInFee.getProviderId().toString());

    // sleep for 2 seconds so that DB to be updated accordingly
    Thread.sleep(2000);

    // read the transaction TRANSFER details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the transaction FEE TRANSFER details from DB
    Transaction transactionDBFee =
        datastoreTestAccount.find(Transaction.class).filter("id", transferFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDBFee.getStatus().toString());
    assertNotEquals("", transactionDBFee.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDBFee.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the balances
    assertEquals("66", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Transfer with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine.
   */
  @Test
  public void test016_CreateTransfer_ProviderFailureCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals("", transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - transferFeeId = " + transferFeeId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("", transactionPayInFee.getProviderId().toString());

    // sleep for 2 seconds so that DB to be updated accordingly
    Thread.sleep(2000);

    // read the transaction TRANSFER details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the transaction FEE TRANSFER details from DB
    Transaction transactionDBFee =
        datastoreTestAccount.find(Transaction.class).filter("id", transferFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDBFee.getStatus().toString());
    assertNotEquals("", transactionDBFee.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDBFee.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the balances
    assertEquals("66", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /** Create Transfer with amount value field set as zero */
  @Test
  public void test017_CreateTransfer_AmountIsZero() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("0", transactionPayIn.getAmount().getValue().toString());

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, transactionDB.getResultMessage().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals("TRANSFER", transactionDB.getType().toString());
    assertEquals("REGULAR", transactionDB.getNature().toString());
    assertEquals("0", transactionDB.getAmount().getValue().toString());
  }

  /** Create Transfer with amount value smaller than fee amount value field */
  @Test
  public void test018_CreateTransfer_AmountSmallerThanFee() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(
        ErrorMessage.ERROR_CREATETRANSFER_REQUEST_AMOUNT_SMALLER_THAN_FEE,
        transactionDB.getResultMessage().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals("TRANSFER", transactionDB.getType().toString());
    assertEquals("REGULAR", transactionDB.getNature().toString());
    assertEquals("10", transactionDB.getAmount().getValue().toString());
  }

  /**
   * Create Transfer with insufficient balance in debit wallet and transaction fees included <br>
   * NOTE: The transaction created is a failed one so the fee transaction will not be created, it
   * will be just one transaction with the amount value as received in input without taking into
   * account the fees
   */
  @Test
  public void test019_CreateTransfer_InsufficientBalanceFeeIncluded() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("50000", transactionPayIn.getAmount().getValue().toString());

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(
        ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE,
        transactionDB.getResultMessage().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals("TRANSFER", transactionDB.getType().toString());
    assertEquals("REGULAR", transactionDB.getNature().toString());
    assertEquals("50000", transactionDB.getAmount().getValue().toString());
  }

  /** Create Transfer with insufficient balance in debit wallet and transaction fees not included */
  @Test
  public void test020_CreateTransfer_InsufficientBalanceFeeNotIncluded() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

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
    transferId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("40000", transactionPayIn.getAmount().getValue().toString());

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(
        ErrorMessage.ERROR_CREATETRANSFER_INSUFFICIENT_BALANCE,
        transactionDB.getResultMessage().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals("TRANSFER", transactionDB.getType().toString());
    assertEquals("REGULAR", transactionDB.getNature().toString());
    assertEquals("40000", transactionDB.getAmount().getValue().toString());
  }

  /** Create Transfer with long customTag field */
  @Test
  public void test021_CreateTransfer_CustomTagMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_TAG_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create Transfer with missing debitedWalletId field */
  @Test
  public void test022_CreateTransfer_WalletAIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);
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
        ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create Transfer with invalid debitedWalletId field */
  @Test
  public void test023_CreateTransfer_WalletAIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_INVALID,
        responseError.getErrorDescription());
  }

  /** Create Transfer with debitedWalletId field inexistent in DB */
  @Test
  public void test024_CreateTransfer_NoWalletA() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", ObjectId.get().toString());
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLET_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create Transfer with missing creditedWalletId field */
  @Test
  public void test025_CreateTransfer_WalletBIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);
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
        ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create Transfer with invalid creditedWalletId field */
  @Test
  public void test026_CreateTransfer_WalletBIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_INVALID,
        responseError.getErrorDescription());
  }

  /** Create Transfer with creditedWalletId field inexistent in DB */
  @Test
  public void test027_CreateTransfer_NoWalletB() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", ObjectId.get().toString());
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLET_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create Transfer with missing debit funds field */
  @Test
  public void test028_CreateTransfer_DebitFundsRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_DEBITFUNDS_REQUIRED, responseError.getErrorDescription());
  }

  /** Create Transfer with missing amount value field */
  @Test
  public void test029_CreateTransfer_AmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);
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

  /** Create Transfer with invalid amount value field */
  @Test
  public void test030_CreateTransfer_AmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with invalid (negative) amount value field */
  @Test
  public void test031_CreateTransfer_NegativeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with missing amount currency field */
  @Test
  public void test032_CreateTransfer_AmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with invalid amount currency field */
  @Test
  public void test033_CreateTransfer_AmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with mismatch amount currency field and wallet currency */
  @Test
  public void test034_CreateTransfer_AmountCurrencyWalletMismatchInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_INVALID,
        responseError.getErrorDescription());
  }

  /** Create Transfer with missing fees field */
  @Test
  public void test035_CreateTransfer_FeesRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_FEES_REQUIRED, responseError.getErrorDescription());
  }

  /** Create Transfer with missing fee amount value field */
  @Test
  public void test036_CreateTransfer_FeeAmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with invalid fee amount value field */
  @Test
  public void test037_CreateTransfer_FeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with invalid (negative) fee amount value field */
  @Test
  public void test038_CreateTransfer_NegativeFeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with missing fee amount currency field */
  @Test
  public void test039_CreateTransfer_FeeAmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with invalid fee amount currency field */
  @Test
  public void test040_CreateTransfer_FeeAmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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

  /** Create Transfer with mismatch amount currency and fee amount currency field */
  @Test
  public void test041_CreateTransfer_FeeAmountCurrencyMismatch() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_REQUEST_CURRENCY_MISSMATCH,
        responseError.getErrorDescription());
  }

  /**
   * Create Transfer with mismatch wallets currency fields <br>
   * (credit and debit wallets have the same currency)
   */
  @Test
  public void test042_CreateTransfer_WalletsCurrencyMismatch() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", USDWalletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_WALLET_CURRENCY_MISSMATCH,
        responseError.getErrorDescription());
  }

  /** Create Transfer with same debit and credit wallet <br> */
  @Test
  public void test043_CreateTransfer_SameWallets() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletAId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_CREDIT_DEBIT_EQUAL, responseError.getErrorDescription());
  }

  /** Create Transfer with missing fee model field */
  @Test
  public void test044_CreateTransfer_FeeModelRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create Transfer with fee model field set as empty */
  @Test
  public void test045_CreateTransfer_FeeModelRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create Transfer with invalid fee model field */
  @Test
  public void test046_CreateTransfer_FeeModelInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

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
        ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_INVALID, responseError.getErrorDescription());
  }

  /** Create Transfer with missing all required fields */
  @Test
  public void test047_CreateTransfer_AllRequired() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_DEBITFUNDS_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_FEES_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_REQUIRED);
    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(5, responseErrors.length);
  }

  /** Create Transfer with missing all required fields */
  @Test
  public void test048_CreateTransfer_AllInvalidMaxLength() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_TAG_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_INVALID);
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_INVALID);
    // below error is for invalid amount
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    // below error is for invalid fees
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    //
    errors.add(ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_INVALID);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(8, responseErrors.length);
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {

    super.afterEachTest();

    // delete created transfer and fee transactions from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFeeId));

    if ((transferProviderId != null) && (!transferProviderId.equals(""))) {
      // delete all the failed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(FailedCallback.class)
                  .filter("parameters.RessourceId", transferProviderId));

      // delete all the processed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(ProcessedCallback.class)
                  .filter("parameters.RessourceId", transferProviderId));
    }

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    // delete created user from database
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userAId));
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userBId));

    // delete created wallets from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletAId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletBId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", USDWalletBId));

    // delete created cardRegistration from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(CardRegistration.class).filter("id", cardRegistrationId));

    // delete created deposit card from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("providerId", cardProviderId));

    // delete created DirectPayIn transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionId));

    // in case some of the tests failed then the transfer transactions will not be deleted from DB
    // and this is why we will do an extra check here to ensure they are all deleted
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("customTag", "customTagCreateTransfer"));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferWithoutTagId));

    // delete all the failed callbacks from DB if any
    databaseService
        .getMainConnection()
        .delete(
            databaseService
                .getMainConnection()
                .createQuery(FailedCallback.class)
                .filter("parameters.RessourceId", transactionProviderId));

    // delete all the processed callbacks from DB if any
    databaseService
        .getMainConnection()
        .delete(
            databaseService
                .getMainConnection()
                .createQuery(ProcessedCallback.class)
                .filter("parameters.RessourceId", transactionProviderId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
