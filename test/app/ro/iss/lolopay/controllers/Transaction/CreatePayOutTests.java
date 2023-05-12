package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static shared.ProviderApis.MANGO_CARD_REGISTRATION_TOKEN;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_DEACTIVATE;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_OTHER;
import static shared.ProviderApis.MANGO_CREATE_BANKACCOUNT_US;
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
import ro.iss.lolopay.models.classes.PaymentType;
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.database.BankAccount;
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
public class CreatePayOutTests extends WithCustomApplication {

  private static String userId;
  private static String userProviderId;
  private static String walletId;
  private static String walletProviderId;
  private static String walletCurrency;
  private static String bankAccountAId;
  private static String bankAccountAProviderId;
  private static String bankAccountBId;
  private static String bankAccountBProviderId;

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

  private static String payOutId;
  private static String payOutWithoutTagId;
  private static String payOutProviderId;
  private static String payOutFeeId;
  private static String testPayOutProviderId;

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";
  private static String errorDescription = "The ressource does not exist";
  private static String errorMessage = "The associated bank account is not active";
  private static String errorCode = "121006";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.TransactionController.createPayOut().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "CreatePayOutTests");
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
        .POST(String.format(MANGO_CREATE_BANKACCOUNT_US.getUri(), userProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(
            String.format(
                MANGO_CREATE_BANKACCOUNT_DEACTIVATE.getUri(),
                userProviderId,
                bankAccountBProviderId))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .PUT(
            String.format(
                MANGO_CREATE_BANKACCOUNT_DEACTIVATE.getUri(),
                userProviderId,
                bankAccountBProviderId))
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

    System.out.println("CreatePayOutTests - userId=" + userId);
    System.out.println("CreatePayOutTests - user providerId=" + userProviderId);
  }

  /**
   * Create userA wallet needed in all the other tests of this class.<br>
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

    System.out.println("CreatePayOutTests - walletId=" + walletId);
    System.out.println("CreatePayOutTests - walletProviderId=" + walletProviderId);
    System.out.println("CreatePayOutTests - walletCurrency=" + walletCurrency);
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
    System.out.println("CreatePayOutTests - createCardRegistrationUrl: " + url);

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

    System.out.println("CreatePayOutTests - cardRegistrationId=" + cardRegistrationId);
    System.out.println("CreatePayOutTests - cardUserProviderId=" + cardUserProviderId);
    System.out.println("CreatePayOutTests - cardRegistrationUrl=" + cardRegistrationUrl);
    System.out.println("CreatePayOutTests - preRegistrationData=" + preRegistrationData);
    System.out.println("CreatePayOutTests - accessKey=" + accessKey);

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
    System.out.println("CreatePayOutTests - updateCardRegistrationUrl: " + url);

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

    System.out.println("CreatePayOutTests - cardId=" + cardId);
    System.out.println("CreatePayOutTests - cardProviderId=" + cardProviderId);

    // get the deposit card Id
    depositCardId =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardProviderId)
            .get()
            .getId();
    System.out.println("CreatePayOutTests - depositCardId=" + depositCardId);
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
    System.out.println("CreatePayOutTests - createDirectPayInUrl: " + url);

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
    System.out.println("CreatePayOutTests - transactionId = " + transactionId);
    System.out.println("CreatePayOutTests - transactionProviderId = " + transactionProviderId);

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
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println("CreatePayOutTests - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYIN TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that the wallet is updated accordingly
    Thread.sleep(2000);

    // read debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the balances
    assertEquals("105", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ===============
    // set up the Id of the json file that must be already loaded for test009
    // this is the value of Id field from
    // test009_CreatePayOut_SuccessCase_ZeroFeeIncluded_mango_200.json
    testPayOutProviderId = "85093209";
  }

  /** Create OTHER account with valid Country, AccountNumber and valid BIC (SWIFT code) ) */
  @Test
  public void test006_CreateBankAccountA() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.BankAccountController.createOther().url();
    System.out.println("CreatePayOutTests - createOtherUrl: " + url);

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
    bankAccountAId = bankAccount.getId().toString();
    bankAccountAProviderId = bankAccount.getProviderId().toString();
    System.out.println("CreatePayOutTests - bankAccountAId = " + bankAccountAId);
    System.out.println("CreatePayOutTests - bankAccountAProviderId=" + bankAccountAProviderId);
  }

  /** Create US account with valid AccountNumber, ABA and DepositAccountType */
  @Test
  public void test007_CreateBankAccountB() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.BankAccountController.createUs().url();
    System.out.println("CreatePayOutTests - createUsUrl: " + url);

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
    assertEquals("US", bankAccount.getType().toString());
    assertEquals("customTagUsForPayOut", bankAccount.getCustomTag().toString());
    assertEquals(true, bankAccount.isActive());

    // fill some variables
    bankAccountBId = bankAccount.getId().toString();
    bankAccountBProviderId = bankAccount.getProviderId().toString();
    System.out.println("CreatePayOutTests - bankAccountBId = " + bankAccountBId);
    System.out.println("CreatePayOutTests - bankAccountBProviderId=" + bankAccountBProviderId);
  }

  /** Deactivate IBAN account successfully */
  @Test
  public void test008_DeactivateBankAccountB() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.BankAccountController.deactivateBankAccount().url();
    System.out.println("CreatePayOutTests - deactivateBankAccountUrl: " + url);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("bankAccountId", bankAccountBId);

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
    assertEquals(bankAccountBId, bankAccount.getId().toString());
    assertEquals("US", bankAccount.getType().toString());
    assertEquals("customTagUsForPayOut", bankAccount.getCustomTag().toString());
    assertEquals(false, bankAccount.isActive());
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test009_CreatePayOut_SuccessCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("97", walletDetails.getBalance().getValue().toString());
    assertEquals("8", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
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
    assertEquals("97", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ======================
    // set up the Id of the json file that must be already loaded for test010
    // this is the value of Id field from
    // test010_CreatePayOut_SuccessCase_FeeIncluded_mango_200.json
    testPayOutProviderId = "85093210";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty feeModel as INCLUDED <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test010_CreatePayOut_SuccessCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

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
    payOutFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreatePayOutTests - payOutFeeId = " + payOutFeeId);

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
    assertEquals("83", walletDetails.getBalance().getValue().toString());
    assertEquals("14", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
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
    assertEquals("83", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ==========================
    // set up the Id of the json file that must be already loaded for test011
    // this is the value of Id field from
    // test011_CreatePayOut_SuccessCase_ZeroFeeNotIncluded_mango_200.json
    testPayOutProviderId = "85093211";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test011_CreatePayOut_SuccessCase_ZeroFeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("7", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("76", walletDetails.getBalance().getValue().toString());
    assertEquals("7", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
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
    assertEquals("76", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ======================
    // set up the Id of the json file that must be already loaded for test012
    // this is the value of Id field from
    // test012_CreatePayOut_SuccessCase_FeeNotIncluded_mango_200.json
    testPayOutProviderId = "85093212";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty feeModel as INCLUDED <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test012_CreatePayOut_SuccessCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("9", transactionPayIn.getAmount().getValue().toString());
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
    payOutFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreatePayOutTests - payOutFeeId = " + payOutFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // ============ When the transaction is first created we have below values in DB =====
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("66", walletDetails.getBalance().getValue().toString());
    assertEquals("10", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money into the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
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
    assertEquals("66", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ==========================
    // set up the Id of the json file that must be already loaded for test013
    // this is the value of Id field from
    // test013_CreatePayOut_SuccessCase_NoBankWireNoCustomTag_mango_200.json
    testPayOutProviderId = "85093213";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are missing <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test013_CreatePayOut_SuccessCase_NoBankWireNoCustomTag() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutWithoutTagId = payOutId;
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals(null, transactionPayIn.getCustomTag());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("8", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS SUCCESSFULL ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_SUCCEEDED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
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
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ======================
    // set up the Id of the json file that must be already loaded for test014
    // this is the value of Id field from
    // test014_CreatePayOut_SuccessCase_ZeroFeeIncludedInactiveAccount_mango_200.json
    testPayOutProviderId = "85093214";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test014_CreatePayOut_SuccessCase_ZeroFeeIncludedInactiveAccount() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("50", walletDetails.getBalance().getValue().toString());
    assertEquals("8", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS FAILED ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_FAILED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYOUT TRANSACTION AS FAILED - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(errorMessage, transactionDB.getResultMessage().toString());
    assertEquals(errorCode, transactionDB.getResultCode().toString());

    // read the debit wallet details
    walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());

    // ======================
    // set up the Id of the json file that must be already loaded for test015
    // this is the value of Id field from
    // test015_CreatePayOut_SuccessCase_FeeNotIncludedInactiveAccount_mango_200.json
    testPayOutProviderId = "85093215";
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test015_CreatePayOut_SuccessCase_FeeNotIncludedInactiveAccount() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

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
    payOutFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreatePayOutTests - payOutFeeId = " + payOutFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // ========= When the transaction is first created we have below values in DB =========
    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("48", walletDetails.getBalance().getValue().toString());
    assertEquals("10", walletDetails.getBlockedBalance().getValue().toString());

    // sleep for 2 seconds so that providerId gets the chance to be saved in DB
    Thread.sleep(2000);

    if (payOutProviderId == "") {
      Transaction transactionPayOut =
          datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
      payOutProviderId = transactionPayOut.getProviderId();
      System.out.println("CreatePayOutTests - payOutProviderId from DB = " + payOutProviderId);
    }

    // =============== START - SET PAYOUT TRANSACTION AS FAILED ===============
    // Simulate reply from mango in order to get the money out from the wallet

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            + "?RessourceId="
            + payOutProviderId
            + "&EventType=PAYOUT_NORMAL_FAILED&Date=1581663625";
    System.out.println("CreatePayOutTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreatePayOutTests - PayOut - responseSuccessCallback: " + responseSuccessCallback);
    // assertEquals(true, responseSuccessCallback);

    // =============== END - SET PAYOUT TRANSACTION AS FAILED - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();

    // sleep for 2 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(2000);

    // ============ After callback below are the final values in DB ============

    // read the payout transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertEquals(errorMessage, transactionDB.getResultMessage().toString());
    assertEquals(errorCode, transactionDB.getResultCode().toString());

    // read the payout transaction fee details from DB
    Transaction transactionFeeDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionFeeDB.getStatus().toString());
    assertEquals(errorMessage, transactionFeeDB.getResultMessage().toString());
    assertEquals(errorCode, transactionFeeDB.getResultCode().toString());

    // read the debit wallet details
    walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as INCLUDED <br>
   * fee value set as zero <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test016_CreatePayOut_ProviderFailureCase_ZeroFeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals(TransactionStatus.CREATED, transactionPayIn.getStatus());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("8", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayIn.getCustomTag().toString());

    // there is no fee payout created
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // sleep for 5 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(5000);

    // ============ After Mango response below are the final values in DB ============

    // read the transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
    // do some checks by performing asserts on the database output result
    assertEquals(TransactionStatus.FAILED, transactionDB.getStatus());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
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
  public void test017_CreatePayOut_ProviderFailureCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYOUT", transactionPayIn.getType().toString());
    assertEquals("REGULAR", transactionPayIn.getNature().toString());
    assertEquals("BANK_WIRE", transactionPayIn.getPaymentType().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
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
    payOutFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreatePayOutTests - payOutFeeId = " + payOutFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("2", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // sleep for 5 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(5000);

    // ============ After Mango response below are the final values in DB ============

    // read the payout transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the payout transaction fee details from DB
    Transaction transactionFeeDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionFeeDB.getStatus().toString());
    assertNotEquals("", transactionFeeDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionFeeDB.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /**
   * Create PayOut with valid data: <br>
   * customTag and bankWireRef are not empty <br>
   * feeModel as NOT_INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test018_CreatePayOut_ProviderFailureCase_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletId);
    parameters.put("currency", walletCurrency);
    parameters.put("bankAccountId", bankAccountAId);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, parameters, requestId, JSON);

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
    payOutId = transactionPayIn.getId().toString();
    payOutProviderId = transactionPayIn.getProviderId().toString();
    System.out.println("CreatePayOutTests - payOutId = " + payOutId);
    System.out.println("CreatePayOutTests - payOutProviderId = " + payOutProviderId);

    // do some other checks by performing asserts on the output result
    assertEquals(TransactionStatus.CREATED, transactionPayIn.getStatus());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals(TransactionType.PAYOUT, transactionPayIn.getType());
    assertEquals(TransactionNature.REGULAR, transactionPayIn.getNature());
    assertEquals(PaymentType.BANK_WIRE, transactionPayIn.getPaymentType());
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
    payOutFeeId = transactionPayInFee.getId().toString();
    System.out.println("CreatePayOutTests - payOutFeeId = " + payOutFeeId);

    // do some other checks by performing asserts on the output result
    assertEquals("CREATED", transactionPayInFee.getStatus().toString());
    assertEquals("", transactionPayInFee.getResultMessage().toString());
    assertEquals("", transactionPayInFee.getResultCode().toString());
    assertEquals("PAYOUT_FEE", transactionPayInFee.getType().toString());
    assertEquals("REGULAR", transactionPayInFee.getNature().toString());
    assertEquals("1", transactionPayInFee.getAmount().getValue().toString());
    assertNotNull(transactionPayInFee.getProviderId().toString());
    assertEquals("customTagCreatePayOut", transactionPayInFee.getCustomTag().toString());

    // sleep for 5 seconds so that wallet gets the chance to be saved in DB
    Thread.sleep(5000);

    // ============ After Mango response below are the final values in DB ============

    // read the payout transaction details from DB
    Transaction transactionDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionDB.getStatus().toString());
    assertNotEquals("", transactionDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionDB.getResultMessage()).findPath("Message").asText());

    // read the payout transaction fee details from DB
    Transaction transactionFeeDB =
        datastoreTestAccount.find(Transaction.class).filter("id", payOutFeeId).get();
    // do some checks by performing asserts on the database output result
    assertEquals("FAILED", transactionFeeDB.getStatus().toString());
    assertNotEquals("", transactionFeeDB.getResultCode().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionFeeDB.getResultMessage()).findPath("Message").asText());

    // read the debit wallet details
    Wallet walletDetails = datastoreTestAccount.find(Wallet.class).filter("id", walletId).get();
    // and check the debit balances
    assertEquals("58", walletDetails.getBalance().getValue().toString());
    assertEquals("0", walletDetails.getBlockedBalance().getValue().toString());
  }

  /** Create PayOut with long customTag field */
  @Test
  public void test019_CreatePayOut_CustomTagMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_TAG_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create PayOut with missing debitedWalletId field */
  @Test
  public void test020_CreatePayOut_WalletIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);
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
        ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLETID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create PayOut with invalid debitedWalletId field */
  @Test
  public void test021_CreatePayOut_WalletIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLETID_INVALID, responseError.getErrorDescription());
  }

  /** Create PayOut with debitedWalletId field inexistent in DB */
  @Test
  public void test022_CreatePayOut_NoWallet() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", ObjectId.get().toString());
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLET_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create PayOut with missing debit funds field */
  @Test
  public void test023_CreatePayOut_DebitFundsRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_DEBITFUNDS_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayOut with missing amount value field */
  @Test
  public void test024_CreatePayOut_AmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);
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

  /** Create PayOut with invalid amount value field */
  @Test
  public void test025_CreatePayOut_AmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);
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

  /** Create PayOut with invalid (negative) amount value field */
  @Test
  public void test026_CreatePayOut_NegativeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with missing amount currency field */
  @Test
  public void test027_CreatePayOut_AmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with invalid amount currency field */
  @Test
  public void test028_CreatePayOut_AmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with mismatch amount currency field and wallet currency field */
  @Test
  public void test029_CreatePayOut_RequestCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_INVALID,
        responseError.getErrorDescription());
  }

  /** Create PayOut with missing fees field */
  @Test
  public void test030_CreatePayOut_FeesRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_FEES_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayOut with missing fee amount value field */
  @Test
  public void test031_CreatePayOut_FeeAmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with invalid fee amount value field */
  @Test
  public void test032_CreatePayOut_FeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with invalid (negative) fee amount value field */
  @Test
  public void test033_CreatePayOut_NegativeFeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with missing fee amount currency field */
  @Test
  public void test034_CreatePayOut_FeeAmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with invalid fee amount currency field */
  @Test
  public void test035_CreatePayOut_FeeAmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with mismatch amount currency and fee amount currency field */
  @Test
  public void test036_CreatePayOut_FeeAmountCurrencyMismatch() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_CURRENCY_MISSMATCH,
        responseError.getErrorDescription());
  }

  /** Create PayOut with amount value smaller than fee amount value field */
  @Test
  public void test037_CreatePayOut_AmountSmallerThanFee() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_REQUEST_AMOUNT_SMALLER_THAN_FEE,
        responseError.getErrorDescription());
  }

  /** Create PayOut with amount value field set as zero */
  @Test
  public void test038_CreatePayOut_AmountIsZero() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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

  /** Create PayOut with insufficient balance in debit wallet and transaction fees included */
  @Test
  public void test039_CreatePayOut_InsufficientBalanceFeeIncluded() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE, responseError.getErrorDescription());
  }

  /** Create PayOut with insufficient balance in debit wallet and transaction fees not included */
  @Test
  public void test040_CreatePayOut_InsufficientBalanceFeeNotIncluded() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_INSUFFICIENT_BALANCE, responseError.getErrorDescription());
  }

  /** Create PayOut with missing fee model field */
  @Test
  public void test041_CreatePayOut_FeeModelRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayOut with fee model field set as empty */
  @Test
  public void test042_CreatePayOut_FeeModelRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayOut with invalid fee model field */
  @Test
  public void test043_CreatePayOut_FeeModelInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_FEEMODEL_INVALID, responseError.getErrorDescription());
  }

  /** Create PayOut with missing bankAccountId field */
  @Test
  public void test044_CreatePayOut_BankAccountIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);
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
        ErrorMessage.ERROR_CREATEPAYOUT_BANKACCOUNTID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create PayOut with invalid bankAccountId field */
  @Test
  public void test045_CreatePayOut_BankAccountIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_BANKACCOUNTID_INVALID, responseError.getErrorDescription());
  }

  /** Create PayOut with bankAccountId field inexistent in DB */
  @Test
  public void test046_CreatePayOut_NoBankAccount() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
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
        ErrorMessage.ERROR_CREATEPAYOUT_INEXISTENT_BANKACCOUNT,
        responseError.getErrorDescription());
  }

  /** Create PayOut with long bankWireRef field */
  @Test
  public void test047_CreatePayOut_BankWireRefMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("debitedWalletId", walletId);
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("bankAccountId", bankAccountAId);

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
        ErrorMessage.ERROR_CREATEPAYOUT_WIREREF_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create PayOut with missing all required fields */
  @Test
  public void test048_CreatePayOut_AllRequired() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLETID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_DEBITFUNDS_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_FEES_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_FEEMODEL_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_BANKACCOUNTID_REQUIRED);
    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(5, responseErrors.length);
  }

  /** Create PayOut with missing all required fields */
  @Test
  public void test049_CreatePayOut_AllInvalidMaxLength() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_TAG_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_WIREREF_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_DEBITWALLETID_INVALID);
    // below error is for invalid amount
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    // below error is for invalid fees
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_FEEMODEL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYOUT_BANKACCOUNTID_INVALID);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(9, responseErrors.length);
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {

    super.afterEachTest();

    // delete created PayOut and fee transactions from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutFeeId));

    if ((payOutProviderId != null) && (!payOutProviderId.equals(""))) {
      // delete all the failed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(FailedCallback.class)
                  .filter("parameters.RessourceId", payOutProviderId));

      // delete all the processed callbacks from DB if any
      databaseService
          .getMainConnection()
          .delete(
              databaseService
                  .getMainConnection()
                  .createQuery(ProcessedCallback.class)
                  .filter("parameters.RessourceId", payOutProviderId));
    }

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
        datastoreTestAccount.createQuery(BankAccount.class).filter("id", bankAccountAId));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankAccount.class).filter("id", bankAccountBId));

    // in case some of the tests failed then the payout transactions will not be deleted from DB
    // and this is why we will do an extra check here to ensure they are all deleted
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("customTag", "customTagCreatePayOut"));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payOutWithoutTagId));

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
