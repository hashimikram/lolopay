package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_OTHER;
import static shared.ProviderApis.MANGO_CREATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_PAYOUT;
import static shared.ProviderApis.MANGO_CREATE_TRANSACTION_DIRECTPAYIN;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_GET_PAYINS;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_UPDATE_CARDREGISTRATION;
import static shared.ProviderApis.MANGO_VIEW_CARD;
import static shared.ProviderApis.MANGO_VIEW_PAYOUT;
import static shared.Providers.MANGO;
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
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GetPayOutTests extends WithCustomApplication {

  private static String userId;
  private static String userProviderId;
  private static String walletId;
  private static String walletProviderId;
  private static String walletCurrency;
  private static String bankAccountId;
  private static String bankAccountAProviderId;

  private static String cardRegistrationId;
  private static String cardRegistrationUrl;
  private static String preRegistrationData;
  private static String registrationData;
  private static String accessKey;
  private static String cardUserProviderId;
  private static String cardProviderId;
  private static String cardId;
  private static String testCardId;
  private static String depositCardId;
  private static String transactionId;
  private static String transactionProviderId;

  private static String payOutSId;
  private static String payOutSProviderId;
  private static String payOutCId;
  private static String payOutCProviderId;
  private static String payOutCFeeId;
  private static String payOutFId;
  private static String payOutFProviderId;
  private static String payOutFFeeId;
  private static String testPayOutProviderId;

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";
  private static String errorDescription = "The ressource does not exist";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "GetPayOutTests");
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
                httpHelper.getProviderResponse("test004_GetDepositCard", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447455"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    "test005_ViewDirectPayIn", MANGO, responseHttpStatus))
        .POST(String.format(MANGO_CREATE_BANKACCOUNT_OTHER.getUri(), userProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_PAYOUT.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_PAYOUT.getUri(), testPayOutProviderId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.substring(0, 8) + "ViewPayOut", MANGO, responseHttpStatus))
        .build();
  }

  /**
   * Create natural user needed in all the other tests of this class. <br>
   * This user it will be used for a wallet that must be debited.
   */
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
    userProviderId = user.getProviderId();

    System.out.println("GetPayOutTests - userId=" + userId);
    System.out.println("GetPayOutTests - user providerId=" + userProviderId);
  }

  /**
   * Create user wallet needed in all the other tests of this class.<br>
   * This is the debited wallet.
   */
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

    System.out.println("GetPayOutTests - walletId=" + walletId);
    System.out.println("GetPayOutTests - walletProviderId=" + walletProviderId);
    System.out.println("GetPayOutTests - walletCurrency=" + walletCurrency);
  }

  /**
   * Request creation of a card (make a card registration) so that a DirectPayIn transaction to put
   * money into wallet and to be able to do payout in the next testcases
   */
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
    System.out.println("GetPayOutTests - createCardRegistrationUrl: " + url);

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
    // cardUserProviderId value it is same as userProviderId value
    cardUserProviderId = cardRegistration.getUserProviderId();

    // values needed for token authentication of creation of depositCard in next testcase
    cardRegistrationUrl = cardRegistration.getCardRegistrationUrl();
    preRegistrationData = cardRegistration.getPreRegistrationData();
    accessKey = cardRegistration.getAccessKey();

    System.out.println("GetPayOutTests - cardRegistrationId=" + cardRegistrationId);
    System.out.println("GetPayOutTests - cardUserProviderId=" + cardUserProviderId);
    System.out.println("GetPayOutTests - cardRegistrationUrl=" + cardRegistrationUrl);
    System.out.println("GetPayOutTests - preRegistrationData=" + preRegistrationData);
    System.out.println("GetPayOutTests - accessKey=" + accessKey);

    // set up the cardId for testing purposes
    // NOTE: This value must match the value of:
    //       CardId from test004_updateCardRegistration_mango_200.json
    //   and Id from test004_updateCardRegistrationDeposit_mango_200.json
    testCardId = "12639018";
  }

  /**
   * Finally create the card that is needed so that a DirectPayIn transaction to put money into
   * wallet and to be able to do payout in the next testcases
   */
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
    System.out.println("GetPayOutTests - updateCardRegistrationUrl: " + url);

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

    System.out.println("GetPayOutTests - cardId=" + cardId);
    System.out.println("GetPayOutTests - cardProviderId=" + cardProviderId);

    // get the deposit card Id
    depositCardId =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardProviderId)
            .get()
            .getId();
    System.out.println("GetPayOutTests - depositCardId=" + depositCardId);
  }

  /**
   * Create DirectPayIn to put money into wallet and to be able to do payout in the next testcases
   */
  @Test
  public void test005_CreateDirectPayIn() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCardId);

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("GetPayOutTests - createDirectPayInUrl: " + url);

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
    System.out.println("GetPayOutTests - transactionId = " + transactionId);
    System.out.println("GetPayOutTests - transactionProviderId = " + transactionProviderId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("Success", transactionPayIn.getResultMessage().toString());
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("DIRECT", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("40", transactionPayIn.getAmount().getValue().toString());
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
    System.out.println("GetPayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println("GetPayOutTests - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYIN TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that the wallet is updated accordingly
    Thread.sleep(2000);

    // read debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the balances
    assertEquals("40", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ===============
    // set up the Id of the json file that must be already loaded for test007
    // this is the value of Id field from
    // test007_CreatePayOutWithSucceededStatus.json
    testPayOutProviderId = "85093209";
  }

  /** Create OTHER account with valid Country, AccountNumber and valid BIC (SWIFT code) ) */
  @Test
  public void test006_CreateBankAccount() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.BankAccountController.createOther().url();
    System.out.println("GetPayOutTests - createOtherUrl: " + url);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

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
    assertEquals("OTHER", bankAccount.getType().toString());
    assertEquals("customTagOtherForPayOut", bankAccount.getCustomTag().toString());
    assertEquals(true, bankAccount.isActive());

    // fill some variables
    bankAccountId = bankAccount.getId().toString();
    bankAccountAProviderId = bankAccount.getProviderId().toString();
    System.out.println("GetPayOutTests - bankAccountId = " + bankAccountId);
    System.out.println("GetPayOutTests - bankAccountAProviderId=" + bankAccountAProviderId);
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test007_CreatePayOutWithSucceededStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountId);

    String url = routes.TransactionController.createPayOut().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    payOutSId = transactionPayIn.getId().toString();
    payOutSProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("GetPayOutTests - payOutSId = " + payOutSId);
    System.out.println("GetPayOutTests - payOutSProviderId = " + payOutSProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("30", walletDetails.getBalance().getValue().toString());
    assertEquals("10", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutSProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutSId).get();
      payOutSProviderId = transactionPayOut.getProviderId();
      System.out.println("GetPayOutTests - payOutSProviderId from DB = " + payOutSProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutSProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("GetPayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "GetPayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYOUT TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============
    // read the debit wallet details
    walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("30", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test008_CreatePayOutWithCreatedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.createPayOut().url();

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    payOutCId = transactionPayIn.getId().toString();
    payOutCProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("GetPayOutTests - payOutCId = " + payOutCId);
    System.out.println("GetPayOutTests - payOutCProviderId = " + payOutCProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // get FEE PAYOUT data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    payOutCFeeId = transactionPayInFee.getId().toString();
    System.out.println("GetPayOutTests - payOutCFeeId = " + payOutCFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("16", walletDetails.getBalance().getValue().toString());
    assertEquals("14", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutCProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutCId).get();
      payOutCProviderId = transactionPayOut.getProviderId();
      System.out.println("GetPayOutTests - payOutCProviderId from DB = " + payOutCProviderId);
    }
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test009_CreatePayOutWithFailedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String url = routes.TransactionController.createPayOut().url();

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // fill some variables
    payOutFId = transactionPayIn.getId().toString();
    payOutFProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("GetPayOutTests - payOutFId = " + payOutFId);
    System.out.println("GetPayOutTests - payOutFProviderId = " + payOutFProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("9", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // get FEE PAYOUT data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    PayIn transactionPayInFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertNotNull(userNodeFee);
    assertNotNull(transactionPayInFee);

    // fill some variables
    payOutFFeeId = transactionPayInFee.getId().toString();
    System.out.println("GetPayOutTests - payOutFFeeId = " + payOutFFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After Mango response below are the final values in DB ============

    // read the payout transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutFId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the payout transaction fee details from DB
    Transaction transactionFeeDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutFFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionFeeDB.getStatus().toString());
    assertNotEquals("", transactionFeeDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionFeeDB.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("16", walletDetails.getBalance().getValue().toString());
    assertEquals("14", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Retrieve a PayOut with SUCCEEDED status and below data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test010_GetPayOut_SuccessCase_GetPayOutWithSucceededStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut(payOutSId).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transaction");

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // do some other checks by performing asserts on the output result
    assertEquals(payOutSId, transactionPayIn.getId().toString());
    assertEquals(payOutSProviderId, transactionPayIn.getProviderId().toString());
    assertEquals("SUCCEEDED", transactionPayIn.getStatus().toString());
    assertEquals("Success", transactionPayIn.getResultMessage().toString());
    assertEquals("000000", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());
    assertEquals("", transactionPayIn.getRelatedTransactionId());
  }

  /**
   * Retrieve a PayOut with CREATED status and below data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test011_GetPayOut_SuccessCase_GetPayOutWithCreatedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut(payOutCId).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transaction");

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // do some other checks by performing asserts on the output result
    assertEquals(payOutCId, transactionPayIn.getId().toString());
    assertEquals(payOutCProviderId, transactionPayIn.getProviderId().toString());
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());
    assertEquals(payOutCFeeId, transactionPayIn.getRelatedTransactionId());
  }

  /**
   * Retrieve a PayOut with FAILED status and below data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test012_GetPayOut_SuccessCase_GetPayOutWithFailedStatus() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut(payOutFId).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get PAYOUT data from body
    JsonNode userNode = jsonResponse.at("/body/transaction");

    // get data from body to object
    PayIn transactionPayIn = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transactionPayIn);

    // do some other checks by performing asserts on the output result
    assertEquals(payOutFId, transactionPayIn.getId().toString());
    assertEquals(payOutFProviderId, transactionPayIn.getProviderId().toString());
    assertEquals("FAILED", transactionPayIn.getStatus().toString());
    assertNotEquals("", transactionPayIn.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionPayIn.getResultMessage()).findPath("Message").asText());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("9", transactionPayIn.getAmount().getValue().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());
    assertEquals(payOutFFeeId, transactionPayIn.getRelatedTransactionId());
  }

  /** Retrieve a PayOut using an invalid transactionId */
  @Test
  public void test013_GetPayOut_TransactionIdInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut("po212345aa").url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_GETTRANSACTION_INVALID_TRANSACTIONID,
        responseError.getErrorDescription());
  }

  /** Retrieve a PayOut using an inexistent transactionId */
  @Test
  public void test014_GetPayOut_NoTransaction() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut(ObjectId.get().toString()).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_GETTRANSACTION_INEXISTENT_PAYOUT, responseError.getErrorDescription());
  }

  /** Retrieve a PayOut using a transactionId of a direct PayIn */
  @Test
  public void test015_GetPayOut_InvalidType() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.getPayOut(transactionId).url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(
        ErrorMessage.ERROR_GETTRANSACTION_INVALID_TYPE, responseError.getErrorDescription());
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {

    super.afterEachTest();

    // delete all the callbacks from DB if any
    databaseService
        .getMainConnection()
        .delete(
            databaseService
                .getMainConnection()
                .createQuery(Callback.class)
                .filter("parameters.RessourceId", transactionProviderId));

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

    // delete all the failed callbacks from DB if any
    databaseService
        .getMainConnection()
        .delete(
            databaseService
                .getMainConnection()
                .createQuery(FailedCallback.class)
                .filter("parameters.RessourceId", payOutSProviderId));

    // delete all the processed callbacks from DB if any
    databaseService
        .getMainConnection()
        .delete(
            databaseService
                .getMainConnection()
                .createQuery(ProcessedCallback.class)
                .filter("parameters.RessourceId", payOutSProviderId));

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    // delete created user from database
    datastoreTestAccount.delete(datastoreTestAccount.createQuery(User.class).filter("id", userId));

    // delete created wallets from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletId));

    // delete created cardRegistration from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(CardRegistration.class).filter("id", cardRegistrationId));

    // delete created deposit card from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("providerId", cardProviderId));

    // delete created DirectPayIn transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transactionId));

    // delete created bank accounts from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankAccount.class).filter("id", bankAccountId));

    // delete created PayOut and fee transactions from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutSId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutCId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutCFeeId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutFId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutFFeeId));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
