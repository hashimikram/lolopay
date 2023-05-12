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
import static shared.ProviderApis.MANGO_REFUND_TRANSFER;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
import static shared.ProviderApis.MANGO_VIEW_REFUND;
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
public class RefundTransactionTests extends WithCustomApplication {

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

  // variables for a transfer with Zero Fee Included to be used for a Successful refund scenario
  private static String transferZFISId;
  private static String transferProviderZFISId;
  // variables for a transfer with Fee Included to be used for a Successful refund scenario
  private static String transferFISId;
  private static String transferProviderFISId;
  private static String transferFeeFISId;
  // variables for a transfer with Zero Fee Not Included to be used for a Successful refund scenario
  private static String transferZFNISId;
  private static String transferProviderZFNISId;
  // variables for a transfer with Fee Included to be used for a Successful refund scenario
  private static String transferFNISId;
  private static String transferProviderFNISId;
  private static String transferFeeFNISId;
  // variable for a failed transfer
  private static String transferFId;
  // variables for a transfer with Zero Fee Included to be used for a Failure refund scenario
  private static String transferZFIFId;
  private static String transferProviderZFIFId;
  // variables for a transfer with Fee Included to be used for a Failure refund scenario
  private static String transferFIFId;
  private static String transferProviderFIFId;
  private static String transferFeeFIFId;
  // variables for a transfer with Fee Included to be used for a Failure refund scenario
  private static String transferFNIFId;
  private static String transferProviderFNIFId;
  private static String transferFeeFNIFId;
  // variables for a transfer with Fee Included to be used for a Refund Balance Issue scenario
  private static String transferRBIId;
  private static String transferProviderRBIId;
  // variables for a transfer from credit side into debit side
  private static String transferCtDId;
  private static String transferProviderCtDId;

  private static String testTransferProviderId;
  private static String testRefundProviderId;
  private static String testRefundTransferId;

  private static String refundId;
  private static String refundIdWithoutTag;
  private static String refundProviderId;
  private static String refundFeeId;

  private static List<String> providerIdList = new ArrayList<String>();

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
  private static String errorMessage = "Transaction has already been successfully refunded";
  private static String errorCode = "001401";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.TransactionController.refundTransaction().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "RefundTransactionTests");
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
                httpHelper.getProviderResponse("test006_GetDepositCard", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447477"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    "test007_ViewDirectPayIn", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSFER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_TRANSFER.getUri(), testTransferProviderId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.substring(0, 8) + "ViewTransfer", MANGO, responseHttpStatus))
        .POST(String.format(MANGO_REFUND_TRANSFER.getUri(), testRefundTransferId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_REFUND.getUri(), testRefundProviderId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.substring(0, 8) + "ViewRefundTransaction", MANGO, responseHttpStatus))
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

    System.out.println("RefundTransactionTests - userAId=" + userAId);
    System.out.println("RefundTransactionTests - user providerAId=" + providerAId);
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

    System.out.println("RefundTransactionTests - userBId=" + userBId);
    System.out.println("RefundTransactionTests - user providerBId=" + providerBId);
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

    System.out.println("RefundTransactionTests - walletAId=" + walletAId);
    System.out.println("RefundTransactionTests - walletAProviderId=" + walletAProviderId);
    System.out.println("RefundTransactionTests - walletCurrency=" + walletACurrency);
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

    System.out.println("RefundTransactionTests - walletBId=" + walletBId);
    System.out.println("RefundTransactionTests - walletBProviderId=" + walletBProviderId);
    System.out.println("RefundTransactionTests - walletBCurrency=" + walletBCurrency);
  }

  /**
   * Request creation of a card (make a card registration) so that a DirectPayIn transaction to put
   * money into walletA and to be able to do transfers in the next testcases
   */
  @Test
  public void test005_CreateCardRegistration() throws Exception {

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

    System.out.println("RefundTransactionTests - cardRegistrationId=" + cardRegistrationId);
    System.out.println("RefundTransactionTests - cardUserProviderId=" + cardUserProviderId);
    System.out.println("RefundTransactionTests - cardRegistrationUrl=" + cardRegistrationUrl);
    System.out.println("RefundTransactionTests - preRegistrationData=" + preRegistrationData);
    System.out.println("RefundTransactionTests - accessKey=" + accessKey);

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
  public void test006_UpdateCardRegistration() throws Exception {

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
    parameters.put("registrationData", registrationData);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistrationId).url();
    System.out.println("RefundTransactionTests - url: " + url);

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

    System.out.println("RefundTransactionTests - cardId=" + cardId);
    System.out.println("RefundTransactionTests - cardProviderId=" + cardProviderId);

    // get the deposit card Id
    depositCardId =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardProviderId)
            .get()
            .getId();
    System.out.println("RefundTransactionTests - depositCardId=" + depositCardId);

    // sleep for 1 second
    Thread.sleep(1000);
  }

  /**
   * Create DirectPayIn to put money into walletA and to be able to do transfers in the next
   * testcases
   */
  @Test
  public void test007_CreateDirectPayIn() throws Exception {

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
    System.out.println("RefundTransactionTests - url: " + url);

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
    System.out.println("RefundTransactionTests - transactionId = " + transactionId);
    System.out.println("RefundTransactionTests - transactionProviderId = " + transactionProviderId);
    providerIdList.add(transactionPayIn.getProviderId());

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("Success", transactionPayIn.getResultMessage().toString());
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("65", transactionPayIn.getAmount().getValue().toString());
    assertEquals(null, transactionPayIn.getBilling());

    // =============== START - SET PAYIN TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transactionProviderId
            + "&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYIN TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // read debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the balances
    assertEquals("65", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test009
    // this is the value of Id field from
    // test008_CreateTransferZeroFeeIncludedForS_mango_200.json.json
    testTransferProviderId = "75193248";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test008_CreateTransferZeroFeeIncludedForS() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferZFISId = transactionPayIn.getId().toString();
    transferProviderZFISId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferZFISId = " + transferZFISId);
    System.out.println(
        "RefundTransactionTests - transferProviderZFISId = " + transferProviderZFISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("57", walletDetailsA.getBalance().getValue().toString());
    assertEquals("8", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("0", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderZFISId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferZFISId).get();
      transferProviderZFISId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderZFISId from DB = " + transferProviderZFISId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderZFISId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("57", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test009
    // this is the value of Id field from
    // test009_CreateTransferFeeIncludedForS_mango_200.json
    testTransferProviderId = "75193249";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test009_CreateTransferFeeIncludedForS() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferFISId = transactionPayIn.getId().toString();
    transferProviderFISId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferFISId = " + transferFISId);
    System.out.println("RefundTransactionTests - transferProviderFISId = " + transferProviderFISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("6", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeFISId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - transferFeeFISId = " + transferFeeFISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("50", walletDetailsA.getBalance().getValue().toString());
    assertEquals("7", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderFISId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferFISId).get();
      transferProviderFISId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderId from DB = " + transferProviderFISId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderFISId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("50", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("14", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test010
    // this is the value of Id field from
    // test010_CreateTransferZeroFeeNotIncludedForS_mango_200.json
    testTransferProviderId = "75193250";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test010_CreateTransferZeroFeeNotIncludedForS() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferZFNISId = transactionPayIn.getId().toString();
    transferProviderZFNISId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferZFNISId = " + transferZFNISId);
    System.out.println(
        "RefundTransactionTests - transferProviderZFNISId = " + transferProviderZFNISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ======== When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("43", walletDetailsA.getBalance().getValue().toString());
    assertEquals("7", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("14", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderZFNISId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferZFNISId).get();
      transferProviderZFNISId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderZFNISId from DB = " + transferProviderZFNISId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderZFNISId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581928816";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("43", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("21", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test011
    // this is the value of Id field from
    // test011_CreateTransferFeeNotIncludedForS_mango_200.json
    testTransferProviderId = "75193251";
  }

  /**
   * Create Transfer with valid data: <br>
   * customTag is not present <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test011_CreateTransferFeeNotIncludedForS() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferFNISId = transactionPayIn.getId().toString();
    transferProviderFNISId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferFNISId = " + transferFNISId);
    System.out.println(
        "RefundTransactionTests - transferProviderFNISId = " + transferProviderFNISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeFNISId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - transferFeeFNISId = " + transferFeeFNISId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("34", walletDetailsA.getBalance().getValue().toString());
    assertEquals("9", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("21", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderFNISId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferFNISId).get();
      transferProviderFNISId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderFNISId from DB = " + transferProviderFNISId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderFNISId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1582100572";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("34", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("28", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test013
    // this is the value of Id field from
    // test013_CreateTransferZeroFeeIncludedForF_MANGO_200.json
    testTransferProviderId = "75193253";
  }

  /** Create a Failed Transfer transaction (amount value smaller than fee amount value field) */
  @Test
  public void test012_CreateTransferFailedTransaction() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletAId);
    bodyParameters.put("creditedWalletId", walletBId);
    bodyParameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, url, bodyParameters, requestId, JSON);

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
    transferFId = transactionPayIn.getId().toString();
    System.out.println("CreateTransferTests - transferId = " + transferFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", transferFId).get();
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
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test013_CreateTransferZeroFeeIncludedForF() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferZFIFId = transactionPayIn.getId().toString();
    transferProviderZFIFId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferZFIFId = " + transferZFIFId);
    System.out.println(
        "RefundTransactionTests - transferProviderZFIFId = " + transferProviderZFIFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("26", walletDetailsA.getBalance().getValue().toString());
    assertEquals("8", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("28", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderZFIFId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferZFIFId).get();
      transferProviderZFIFId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderZFIFId from DB = " + transferProviderZFIFId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderZFIFId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("26", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test014
    // this is the value of Id field from
    // test014_CreateTransferFeeIncludedForF_mango_200.json
    testTransferProviderId = "75193254";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test014_CreateTransferFeeIncludedForF() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferFIFId = transactionPayIn.getId().toString();
    transferProviderFIFId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferFIFId = " + transferFIFId);
    System.out.println("RefundTransactionTests - transferProviderFIFId = " + transferProviderFIFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("6", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeFIFId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - transferFeeFIFId = " + transferFeeFIFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("19", walletDetailsA.getBalance().getValue().toString());
    assertEquals("7", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderFIFId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferFIFId).get();
      transferProviderFIFId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderFIFId from DB = " + transferProviderFIFId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderFIFId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("19", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("42", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test015
    // this is the value of Id field from
    // test015_CreateTransferFeeNotIncludedForF_mango_200.json
    testTransferProviderId = "75193255";
  }

  /**
   * Create Transfer with valid data: <br>
   * customTag is not present <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test015_CreateTransferFeeNotIncludedForF() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferFNIFId = transactionPayIn.getId().toString();
    transferProviderFNIFId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferFNIFId = " + transferFNIFId);
    System.out.println(
        "RefundTransactionTests - transferProviderFNIFId = " + transferProviderFNIFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    transferFeeFNIFId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - transferFeeFNIFId = " + transferFeeFNIFId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("10", walletDetailsA.getBalance().getValue().toString());
    assertEquals("9", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("42", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderFNIFId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferFNIFId).get();
      transferProviderFNIFId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderFNIFId from DB = " + transferProviderFNIFId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderFNIFId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1582100572";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("10", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("49", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test016
    // this is the value of Id field from
    // test016_CreateTransferForRefundBalanceIssue_mango_200.json
    testTransferProviderId = "75193256";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test016_CreateTransferForRefundBalanceIssue() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletAId);
    parameters.put("creditedWalletId", walletBId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferRBIId = transactionPayIn.getId().toString();
    transferProviderRBIId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferRBIId = " + transferRBIId);
    System.out.println("RefundTransactionTests - transferProviderRBIId = " + transferProviderRBIId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("0", walletDetailsA.getBalance().getValue().toString());
    assertEquals("10", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("49", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderRBIId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferRBIId).get();
      transferProviderRBIId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderRBIId from DB = " + transferProviderRBIId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderRBIId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("0", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("59", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ======================================================================= //
    // set up the Id of the json file that must be already loaded for test017
    // this is the value of Id field from
    // test017_CreateTransferFromCreditToDebit_mango_200.json
    testTransferProviderId = "75193257";
  }

  /**
   * Create Transfer with valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test017_CreateTransferFromCreditToDebit() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletBId);
    parameters.put("creditedWalletId", walletAId);
    parameters.put("currency", walletACurrency);

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    transferCtDId = transactionPayIn.getId().toString();
    transferProviderCtDId = transactionPayIn.getProviderId().toString();
    providerIdList.add(transactionPayIn.getProviderId());
    System.out.println("RefundTransactionTests - transferCtDId = " + transferCtDId);
    System.out.println("RefundTransactionTests - transferProviderCtDId = " + transferProviderCtDId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("23", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("0", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("23", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (transferProviderCtDId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", transferCtDId).get();
      transferProviderCtDId = transactionTransfer.getProviderId();
      providerIdList.add(transactionTransfer.getProviderId());
      System.out.println(
          "RefundTransactionTests - transferProviderCtDId from DB = " + transferProviderCtDId);
    }

    // =============== START - SET TRANSFER TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + transferProviderCtDId
            + "&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("23", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("36", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ================================================================================ //
    // set up the Id of the transfer that will be refunded as mentioned into the
    // InitialTransactionId field value of
    // test018_RefundTransaction_SuccessCase_ZeroFeeIncluded_mango_200.json
    testRefundTransferId = transferProviderZFISId;
    // set up the Id of the refund transaction as mentioned into the Id field value of
    // test018_RefundTransaction_SuccessCase_ZeroFeeIncluded_mango_200.json and
    // test018_ViewRefundTransaction_mango_200.json
    testRefundProviderId = "76193248";
  }

  /**
   * Refund a Transfer created with below data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test018_RefundTransaction_SuccessCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferZFISId);

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
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreateRefundTransfer", transactionPayIn.getCustomTag().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("23", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("28", walletDetailsB.getBalance().getValue().toString());
    assertEquals("8", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (refundProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
      refundProviderId = transactionTransfer.getProviderId();
      System.out.println("RefundTransactionTests - refundProviderId from DB = " + refundProviderId);
    }

    // =============== START - SET REFUND TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the appropriate wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + refundProviderId
            + "&EventType=TRANSFER_REFUND_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Refund - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET REFUND TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("31", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("28", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ================================================================================== //
    // set up the Id of the transfer that will be refunded as mentioned into the
    // InitialTransactionId field value of
    // test019_RefundTransaction_SuccessCase_FeeIncluded_mango_200.json
    testRefundTransferId = transferProviderFISId;
    // set up the Id of the refund transaction as mentioned into the Id field value of
    // test019_RefundTransaction_SuccessCase_FeeIncluded_mango_200.json and
    // test019_ViewRefundTransaction_mango_200.json
    testRefundProviderId = "76193249";
  }

  /**
   * Refund a Transfer created with below data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test019_RefundTransaction_SuccessCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferFISId);

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
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("6", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreateRefundTransfer", transactionPayIn.getCustomTag().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    refundFeeId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - refundFeeId = " + refundFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REFUND", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("31", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("22", walletDetailsB.getBalance().getValue().toString());
    assertEquals("6", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (refundProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
      refundProviderId = transactionTransfer.getProviderId();
      System.out.println("RefundTransactionTests - refundProviderId from DB = " + refundProviderId);
    }

    // =============== START - SET REFUND TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the appropriate wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + refundProviderId
            + "&EventType=TRANSFER_REFUND_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Refund - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET REFUND TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("38", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("22", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ================================================================================== //
    // set up the Id of the transfer that will be refunded as mentioned into the
    // InitialTransactionId field value of
    // test020_RefundTransaction_SuccessCase_ZeroFeeNotIncluded_mango_200.json
    testRefundTransferId = transferProviderZFNISId;
    // set up the Id of the refund transaction as mentioned into the Id field value of
    // test020_RefundTransaction_SuccessCase_ZeroFeeNotIncluded_mango_200.json and
    // test020_ViewRefundTransaction_mango_200.json
    testRefundProviderId = "76193250";
  }

  /**
   * Refund a Transfer created with below data: <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test020_RefundTransaction_SuccessCase_ZeroFeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferZFNISId);

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
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreateRefundTransfer", transactionPayIn.getCustomTag().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("38", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("15", walletDetailsB.getBalance().getValue().toString());
    assertEquals("7", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (refundProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
      refundProviderId = transactionTransfer.getProviderId();
      System.out.println("RefundTransactionTests - refundProviderId from DB = " + refundProviderId);
    }

    // =============== START - SET REFUND TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the appropriate wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + refundProviderId
            + "&EventType=TRANSFER_REFUND_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Refund - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET REFUND TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("45", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("15", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ================================================================================== //
    // set up the Id of the transfer that will be refunded as mentioned into the
    // InitialTransactionId field value of
    // test019_RefundTransaction_SuccessCase_FeeIncluded_mango_200.json
    testRefundTransferId = transferProviderFNISId;
    // set up the Id of the refund transaction as mentioned into the Id field value of
    // test019_RefundTransaction_SuccessCase_FeeIncluded_mango_200.json and
    // test019_ViewRefundTransaction_mango_200.json
    testRefundProviderId = "76193251";
  }

  /**
   * Refund a Transfer created with below data: <br>
   * customTag is not present <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test021_RefundTransaction_SuccessCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferFNISId);

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
    refundId = transactionPayIn.getId().toString();
    refundIdWithoutTag = refundId;
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertEquals("", transactionPayIn.getCustomTag().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    refundFeeId = transactionPayInFee.getId().toString();
    System.out.println("RefundTransactionTests - refundFeeId = " + refundFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REFUND", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("45", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("7", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (refundProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
      refundProviderId = transactionTransfer.getProviderId();
      System.out.println("RefundTransactionTests - refundProviderId from DB = " + refundProviderId);
    }

    // =============== START - SET REFUND TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the appropriate wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + refundProviderId
            + "&EventType=TRANSFER_REFUND_SUCCEEDED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Refund - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET REFUND TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // =============================================================================== //
    // set up the Id of the transfer that will be refunded in next test
    // test022_RefundTransaction_TransactionAlreadyRefunded.json
    testRefundTransferId = transferProviderZFNISId;
    // set up the Id of the refund transaction as mentioned into the Id field value of
    // test022_RefundTransaction_TransactionAlreadyRefunded_mango_200.json and
    // test022_ViewRefundTransaction_mango_200.json
    testRefundProviderId = "76193258";
    // =============================================================================== //
  }

  /** Refund a Transfer Transaction that was already refunded successfully one in test020 <br> */
  @Test
  public void test022_RefundTransaction_SuccessCase_TransactionAlreadyRefunded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferZFNISId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("1", walletDetailsB.getBalance().getValue().toString());
    assertEquals("7", walletDetailsB.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds
    Thread.sleep(2000);

    if (refundProviderId == "") {
      Transaction transactionTransfer =
          datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
      refundProviderId = transactionTransfer.getProviderId();
      System.out.println("RefundTransactionTests - refundProviderId from DB = " + refundProviderId);
    }

    // =============== START - SET REFUND TRANSACTION AS FAILED ===============
    // Simulate reply from mango in order to get the money into the appropriate wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + refundProviderId
            + "&EventType=TRANSFER_REFUND_FAILED&Date=1581663625";
    System.out.println("RefundTransactionTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "RefundTransactionTests - Refund - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET REFUND TRANSACTION AS FAILED - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that transaction is updated in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(errorMessage, transactionDB.getResultMessage().toString());
    assertEquals(errorCode, transactionDB.getResultCode().toString());

    // read the debit wallet details
    walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());

    // ================================================================== //
    // set up the Id of the transfer that will be refunded in
    // test023_RefundTransaction_ProviderFailureCase_ZeroFeeIncluded.json
    testRefundTransferId = transferProviderZFIFId;
  }

  /**
   * Refund a Transfer created with below valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test023_RefundTransaction_ProviderFailureCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // ================================================================== //
    // set up the Id of the transfer that will be refunded in next test
    // test023_RefundTransaction_ProviderFailureCase_FeeIncluded.json
    testRefundTransferId = transferProviderFIFId;
    // ================================================================== //

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferZFIFId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // read the transaction REFUND details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // sleep for 2 seconds
    Thread.sleep(2000);

    // ============ The wallets should have no change in their balances in DB ============
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());
  }

  /**
   * Refund a Transfer created with below valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test024_RefundTransaction_ProviderFailureCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    // ================================================================== //
    // set up the Id of the transfer that will be refunded in next test
    // test024_RefundTransaction_ProviderFailureCase_FeeNotIncluded.json
    testRefundTransferId = transferProviderFNIFId;
    // ================================================================== //

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferFIFId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("6", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    refundFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - refundFeeId = " + refundFeeId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REFUND", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("", transactionPayInFee.getProviderId().toString());

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // read the transaction REFUND details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // sleep for 2 seconds
    Thread.sleep(2000);

    // ============ The wallets should have no change in their balances in DB ============
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());
  }

  /**
   * Refund a Transfer created with below valid data: <br>
   * customTag is not present <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test025_RefundTransaction_ProviderFailureCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferFNIFId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // get FEE TRANSFER data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    refundFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreateTransferTests - refundFeeId = " + refundFeeId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("TRANSFER_FEE", transactionPayInFee.getType().toString());
    assertEquals("REFUND", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertEquals("", transactionPayInFee.getProviderId().toString());

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 1 seconds so that DB to be updated accordingly
    Thread.sleep(1000);

    // read the transaction REFUND details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // sleep for 2 seconds
    Thread.sleep(2000);

    // ============ The wallets should have no change in their balances in DB ============
    // read the debit wallet details
    Wallet walletDetailsA = datastoreTestAccount.find(Wallet.class).filter("id", walletAId).get();
    // and check the debit balances
    assertEquals("54", walletDetailsA.getBalance().getValue().toString());
    assertEquals("0", walletDetailsA.getBlockedBalance().getValue().toString());

    // read the credit wallet details
    Wallet walletDetailsB = datastoreTestAccount.find(Wallet.class).filter("id", walletBId).get();
    // and check the credit balances
    assertEquals("8", walletDetailsB.getBalance().getValue().toString());
    assertEquals("0", walletDetailsB.getBlockedBalance().getValue().toString());
  }

  /** Refund a Failed Transaction <br> */
  @Test
  public void test026_RefundTransaction_TransactionStatusNotSucceeded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferFId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // read the transaction REFUND details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        ErrorMessage.ERROR_REFUNDTRANSACTION_ORIGINALTRANSACTION_NOT_SUCCEEDED,
        transactionDB.getResultMessage().toString());
  }

  /** Refund a Failed Transaction <br> */
  @Test
  public void test027_RefundTransaction_InsufficientBalanceFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("transactionId", transferRBIId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get REFUND transfer data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    refundId = transactionPayIn.getId().toString();
    refundProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("RefundTransactionTests - refundId = " + refundId);
    System.out.println("RefundTransactionTests - refundProviderId = " + refundProviderId);

    // do some other checks by performing asserts on the output result
    // the output is with success, because this is how the transaction is created
    // only in DB the status is updated as FAILED
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("TRANSFER", transactionPayIn.getType().toString());
    assertEquals("REFUND", transactionPayIn.getNature().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());

    // there is no fee transfer created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // read the transaction REFUND details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", refundId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        ErrorMessage.ERROR_REFUNDTRANSACTION_INSUFFICIENT_BALANCE,
        transactionDB.getResultMessage().toString());
  }

  /** Refund a Transaction with its original type different than TRANSFER */
  @Test
  public void test028_RefundTransaction_TransactionTypeNotTransfer() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("transactionId", transactionId);

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
        ErrorMessage.ERROR_REFUNDTRANSACTION_INVALID_TYPE, responseError.getErrorDescription());
  }

  /** Refund a Transaction with transactionId field inexistent in DB */
  @Test
  public void test029_RefundTransaction_NoTransaction() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("transactionId", ObjectId.get().toString());

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
        ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTION_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Refund Transaction with missing all required fields */
  @Test
  public void test030_RefundTransaction_CustomTagMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("transactionId", transferCtDId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_REFUNDTRANSACTION_TAG_MAXLENGTH);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(1, responseErrors.length);
  }

  /** Refund Transaction with missing transactionId field */
  @Test
  public void test031_RefundTransaction_TransactionIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Refund Transaction with invalid transactionId field */
  @Test
  public void test032_RefundTransaction_TransactionIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_INVALID,
        responseError.getErrorDescription());
  }

  /** Refund Transaction with missing all required fields */
  @Test
  public void test033_RefundTransaction_AllRequired() throws Exception {

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
    errors.add(ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_REQUIRED);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(1, responseErrors.length);
  }

  /** Refund Transaction with invalid fields */
  @Test
  public void test034_RefundTransaction_AllInvalidMaxLength() throws Exception {

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
    errors.add(ErrorMessage.ERROR_REFUNDTRANSACTION_TAG_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_INVALID);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(2, responseErrors.length);
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {

    super.afterEachTest();

    // delete created refund transaction from DB
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", refundId));
    // delete created refund fee transaction from DB
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", refundFeeId));

    if ((refundProviderId != null) && (!refundProviderId.equals(""))) {
      // delete all the failed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(FailedCallback.class)
                  .filter("parameters.RessourceId", refundProviderId));

      // delete all the processed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(ProcessedCallback.class)
                  .filter("parameters.RessourceId", refundProviderId));
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

    // delete created cardRegistration from database ????
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(CardRegistration.class).filter("id", cardRegistrationId));

    // delete created deposit card from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("providerId", cardProviderId));

    // delete created DirectPayIn transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionId));

    // delete created transfers and fees transactions from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferZFISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFeeFISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferZFNISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFNISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFeeFNISId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferZFIFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFIFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFeeFIFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFNIFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferFeeFNIFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferRBIId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transferCtDId));

    // in case some of the tests failed then the refund transactions will not be deleted from DB and
    // this is why we will do an extra check here
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("customTag", "customTagCreateRefundTransfer"));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", refundIdWithoutTag));

    // delete all the CALLBACKS
    providerIdList
        .stream()
        .forEach(
            Id -> {
              databaseService
                  .getMainConnection()
                  .delete(
                      databaseService
                          .getMainConnection()
                          .createQuery(FailedCallback.class)
                          .filter("parameters.RessourceId", Id));

              databaseService
                  .getMainConnection()
                  .delete(
                      databaseService
                          .getMainConnection()
                          .createQuery(ProcessedCallback.class)
                          .filter("parameters.RessourceId", Id));
            });
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
