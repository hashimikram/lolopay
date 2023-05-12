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
import static shared.ProviderApis.MANGO_CREATE_TRANSFER;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
import static shared.ProviderApis.MANGO_GET_PAYINS;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.MANGO_REFUND_PAYIN;
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
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RefundPayinTests extends WithCustomApplication {

  private static String registrationData;
  private static String testCardId;

  private static String SECURE_MODE_RETURN_URL = "https://www.voxfinance.ro";
  private static Server server;

  private static List<String> callbackStrings = new ArrayList<String>();

  private static String testName;
  private static int responseHttpStatus;
  private static HttpHelper httpHelper;
  private static User userA;
  private static User userB;
  private static Wallet walletUserA;
  private static Wallet walletUserB;
  private static DepositCard depositCard;
  private static CardRegistration cardRegistration = new CardRegistration();
  private static PayIn payInWithZeroFee;
  private static Refund refundPayInWithZeroFee;
  private static PayIn payInFeeNotIncluded;
  private static Refund refundPayInFeeNotIncluded;
  private static PayIn payInFeeNotIncludedFee;
  private static Refund refundPayInFeeNotIncludedFee;
  private static PayIn payInFeeIncluded;
  private static Refund refundPayInFeeIncluded;
  private static PayIn payInFeeIncludedFee;
  private static Refund refundPayInFeeIncludedFee;
  private static Transfer transfer;
  private static PayIn payInFeeIncludedFailure;
  private static Refund refundPayInFeeIncludedFailure;
  private static PayIn payInFeeIncludedFailureFee;
  private static Refund refundPayInFeeIncludedFailureFee;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "RefundPayinTests");
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
        .PUT(String.format(MANGO_UPDATE_CARDREGISTRATION.getUri(), cardRegistration.getId()))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_CARD.getUri(), testCardId))
        .routingTo(
            req ->
                httpHelper.getProviderResponse("test006_GetDepositCard", MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_DIRECTPAYIN.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_REFUND_PAYIN.getUri(), "74447455"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_REFUND_PAYIN.getUri(), "74447466"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_REFUND_PAYIN.getUri(), "74447477"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(String.format(MANGO_REFUND_PAYIN.getUri(), "74447478"))
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447455"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447466"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSFER.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_TRANSFER.getUri(), "75093249"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447477"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_REFUND.getUri(), "76502826"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_REFUND.getUri(), "76502927"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_VIEW_REFUND.getUri(), "76502828"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
        .GET(String.format(MANGO_GET_PAYINS.getUri(), "74447478"))
        .routingTo(
            req ->
                httpHelper.getProviderResponse(
                    testName.concat("_status"), MANGO, responseHttpStatus))
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
    userA = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userA);

    System.out.println("userA: " + utilsService.prettyPrintObject(userA));
  }

  /** Create natural user needed in all the other tests of this class */
  /* i need a second user because i need to make a transfer
   * in order to have a different type of transaction so i can
   * make test number 14
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
    userB = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(userB);
    System.out.println("userB: " + userB);
  }

  /** Create user wallet needed in all the other tests of this class */
  @Test
  public void test003_CreateUserAWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userA.getId());
    parameters.put("currency", "EUR");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    walletUserA = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(walletUserA);

    System.out.println("walletUserA: " + walletUserA);
  }

  /** Create user wallet needed in all the other tests of this class */
  @Test
  public void test004_CreateUserBWallet() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userB.getId());
    parameters.put("currency", "EUR");

    String url = routes.WalletController.create().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    walletUserB = Json.fromJson(jsonResponse.at("/body/wallet"), Wallet.class);

    // perform asserts
    assertNotNull(walletUserB);

    System.out.println("walletUserB: " + walletUserB);
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
    parameters.put("userId", userA.getId());
    parameters.put("currency", "EUR");

    String url = routes.CardRegistrationsController.createCardRegistration().url();
    System.out.println("url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    cardRegistration =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("CREATED", cardRegistration.getStatus());

    System.out.println("cardRegistration: " + utilsService.prettyPrintObject(cardRegistration));

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
  public void test007_UpdateCardRegistration() throws Exception {

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
    WSRequest request = wsClient.url(cardRegistration.getCardRegistrationUrl());
    request.addHeader("Content-Type", "application/x-www-form-urlencoded");
    request.addHeader("Cache-Control", "no-cache");

    Map<String, String> bodyMap = new HashMap<String, String>();
    bodyMap.put("accessKeyRef", cardRegistration.getAccessKey());
    bodyMap.put("data", cardRegistration.getPreRegistrationData());
    bodyMap.put("cardNumber", "4706750000000025");
    bodyMap.put("cardExpirationDate", "1230");
    bodyMap.put("cardCvx", "148");

    WSResponse wsResponse = request.post(Json.toJson(bodyMap)).toCompletableFuture().get();
    registrationData = wsResponse.asJson().get(0).textValue();

    // ====== Update card registration with obtained registrationData value ======

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userA.getId());
    parameters.put("registrationData", registrationData);

    String url =
        routes.CardRegistrationsController.updateCardRegistration(cardRegistration.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(PUT, app, autheticationToken, url, parameters, requestId, JSON);

    // get data from response as object
    cardRegistration =
        Json.fromJson(jsonResponse.at("/body/cardRegistration"), CardRegistration.class);

    // perform asserts
    assertNotNull(cardRegistration);
    assertEquals("VALIDATED", cardRegistration.getStatus());
    System.out.println("cardRegistration: " + cardRegistration);

    // get the deposit card Id
    depositCard =
        datastoreTestAccount
            .find(DepositCard.class)
            .filter("providerId", cardRegistration.getCardProviderId())
            .get();

    System.out.println("depositCard: " + utilsService.prettyPrintObject(depositCard));
  }

  @Test
  public void test008_CreateDirectPayIn_withZeroFee() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", String.valueOf(walletUserA.getCurrency()));
    parameters.put("creditedWalletId", walletUserA.getId());
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCard.getId());

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    payInWithZeroFee = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(payInWithZeroFee);

    System.out.println("payInWithZeroFee: " + utilsService.prettyPrintObject(payInWithZeroFee));

    // do some other checks by performing asserts
    assertNotNull(payInWithZeroFee);
    assertEquals("CREATED", payInWithZeroFee.getStatus().toString());
    assertEquals("Success", payInWithZeroFee.getResultMessage().toString());

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(payInWithZeroFee.getProviderId())
            .concat("&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(payInWithZeroFee.getProviderId());
    PayIn payInDb =
        datastoreTestAccount.find(PayIn.class).filter("id", payInWithZeroFee.getId()).get();
    assertEquals(TransactionStatus.SUCCEEDED, payInDb.getStatus());
    System.out.println("payInDb: " + utilsService.prettyPrintObject(payInDb));

    // get wallet

    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(70, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test009_CreateDirectPayIn_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletUserA.getBalance().getCurrency().toString());
    parameters.put("creditedWalletId", walletUserA.getId());
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCard.getId());

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    payInFeeNotIncluded = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(payInFeeNotIncluded);

    // do some other checks by performing asserts
    assertNotNull(payInFeeNotIncluded);
    assertEquals(TransactionStatus.CREATED, payInFeeNotIncluded.getStatus());
    assertEquals("Success", payInFeeNotIncluded.getResultMessage());
    assertEquals("000000", payInFeeNotIncluded.getResultCode());
    assertEquals(TransactionType.PAYIN, payInFeeNotIncluded.getType());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    payInFeeNotIncludedFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(payInFeeNotIncludedFee);

    // do some other checks by performing asserts
    assertNotNull(payInFeeNotIncludedFee);
    assertEquals(TransactionStatus.CREATED, payInFeeNotIncludedFee.getStatus());
    assertEquals(TransactionType.PAYIN_FEE, payInFeeNotIncludedFee.getType());

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(payInFeeNotIncluded.getProviderId())
            .concat("&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(payInFeeNotIncluded.getProviderId());
    // gat payin from database
    payInFeeNotIncluded =
        datastoreTestAccount.find(PayIn.class).filter("id", payInFeeNotIncluded.getId()).get();
    System.out.println(
        "payInFeeNotIncluded: " + utilsService.prettyPrintObject(payInFeeNotIncluded));
    assertEquals(TransactionStatus.SUCCEEDED, payInFeeNotIncluded.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(80, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test010_CreateTransfer() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("debitedWalletId", walletUserA.getId());
    parameters.put("creditedWalletId", walletUserB.getId());
    parameters.put("currency", walletUserA.getBalance().getCurrency().toString());

    String url = routes.TransactionController.createTransfer().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // sleep the main thread in order to let the async call to provider finish
    Thread.sleep(2000);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get TRANSFER data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    transfer = Json.fromJson(userNode, Transfer.class);
    System.out.println("first transfer: " + utilsService.prettyPrintObject(transfer));

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(transfer);
    assertEquals(TransactionStatus.CREATED, transfer.getStatus());
    assertEquals(null, jsonResponse.at("/body/transactions").get(1));

    // get the transfer from database
    transfer = datastoreTestAccount.find(Transfer.class).filter("id", transfer.getId()).get();
    System.out.println("second transfer: " + utilsService.prettyPrintObject(transfer));

    // Simulate reply from mango in order to get the money into the wallet
    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();
    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(transfer.getProviderId())
            .concat("&EventType=TRANSFER_NORMAL_SUCCEEDED&Date=1581663625");

    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    // get response status
    boolean responseSuccessCallback = jsonResponseCallback.findPath("success").asBoolean();
    System.out.println(
        "CreateTransferTests - Transfer - responseSuccessCallback: " + responseSuccessCallback);

    // =============== END - SET TRANSFER TRANSACTION AS SUCCESSFULL - END ===============

    // process the Callback job
    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(transfer.getProviderId());
    // gat payin from database
    transfer = datastoreTestAccount.find(Transfer.class).filter("id", transfer.getId()).get();
    System.out.println("transfer: " + utilsService.prettyPrintObject(transfer));
    assertEquals(TransactionStatus.SUCCEEDED, transfer.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(72, walletUserA.getBalance().getValue().intValue());

    walletUserB = datastoreTestAccount.find(Wallet.class).filter("id", walletUserB.getId()).get();
    System.out.println("walletUserB: " + utilsService.prettyPrintObject(walletUserB));
    assertEquals(8, walletUserB.getBalance().getValue().intValue());
  }

  @Test
  public void test011_CreateDirectPayIn_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletUserA.getBalance().getCurrency().toString());
    parameters.put("creditedWalletId", walletUserA.getId());
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCard.getId());

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object payInFeeNotIncluded
    payInFeeIncluded = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(payInFeeIncluded);

    // do some other checks by performing asserts
    assertNotNull(payInFeeIncluded);
    assertEquals(TransactionStatus.CREATED, payInFeeIncluded.getStatus());
    assertEquals("Success", payInFeeIncluded.getResultMessage().toString());
    assertEquals("000000", payInFeeIncluded.getResultCode().toString());
    assertEquals(TransactionType.PAYIN, payInFeeIncluded.getType());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    payInFeeIncludedFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(payInFeeIncludedFee);

    // do some other checks by performing asserts
    assertNotNull(payInFeeIncludedFee);
    assertEquals(TransactionStatus.CREATED, payInFeeIncludedFee.getStatus());
    assertEquals(TransactionType.PAYIN_FEE, payInFeeIncludedFee.getType());

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(payInFeeIncluded.getProviderId())
            .concat("&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(payInFeeIncluded.getProviderId());

    // gat payin from database
    payInFeeIncluded =
        datastoreTestAccount.find(PayIn.class).filter("id", payInFeeIncluded.getId()).get();
    System.out.println("payInFeeIncluded: " + utilsService.prettyPrintObject(payInFeeIncluded));
    assertEquals(TransactionStatus.SUCCEEDED, payInFeeIncluded.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(117, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test012_RefundPayIn_InvalidPayinId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn("SADAS").url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(
        ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_PAYINID, responseError.getErrorDescription());
  }

  @Test
  public void test013_RefundPayIn_InexistentPayinId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn((new ObjectId()).toString()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(
        ErrorMessage.ERROR_CREATEPAYINREFUND_INEXISTENT_PAYIN, responseError.getErrorDescription());
  }

  @Test
  public void test014_RefundPayIn_InvalidType() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(transfer.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(
        ErrorMessage.ERROR_CREATEPAYINREFUND_INVALID_TYPE, responseError.getErrorDescription());
  }

  @Test
  public void test015_RefundPayIn_Success_ZeroFee() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();
    responseHttpStatus = Http.Status.OK;

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();

    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // we must wait so that the async call to the provider to finish
    Thread.sleep(2000);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    JsonNode jsonNode = jsonResponse.at("/body/transactions").get(0);

    refundPayInWithZeroFee = Json.fromJson(jsonNode, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertEquals(TransactionType.PAYOUT, refundPayInWithZeroFee.getType());
    assertEquals(TransactionNature.REFUND, refundPayInWithZeroFee.getNature());
    assertEquals(payInWithZeroFee.getId(), refundPayInWithZeroFee.getInitialTransactionId());
    assertNull(jsonErrors);

    // get refundPayInWithZeroFee from data base to acces the provider id
    refundPayInWithZeroFee =
        datastoreTestAccount.find(Refund.class).filter("id", refundPayInWithZeroFee.getId()).get();
    System.out.println(
        "refundPayInWithZeroFee: " + utilsService.prettyPrintObject(refundPayInWithZeroFee));

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(refundPayInWithZeroFee.getProviderId())
            .concat("&EventType=PAYIN_REFUND_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(refundPayInWithZeroFee.getProviderId());
    // gat payin from database
    refundPayInWithZeroFee =
        datastoreTestAccount.find(Refund.class).filter("id", refundPayInWithZeroFee.getId()).get();
    System.out.println(
        "refundPayInWithZeroFee: " + utilsService.prettyPrintObject(refundPayInWithZeroFee));
    assertEquals(TransactionStatus.SUCCEEDED, refundPayInWithZeroFee.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(47, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test016_RefundPayIn_Success_FeeNotIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInFeeNotIncluded.getId()).url();

    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // we must wait so that the async call to the provider to finish
    Thread.sleep(2000);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNull(jsonErrors);

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    refundPayInFeeNotIncluded = Json.fromJson(userNode, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(refundPayInFeeNotIncluded);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeNotIncluded);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeNotIncluded.getStatus());
    assertEquals(TransactionType.PAYOUT, refundPayInFeeNotIncluded.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeNotIncluded.getNature());
    assertEquals(12, refundPayInFeeNotIncluded.getAmount().getValue().intValue());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    refundPayInFeeNotIncludedFee = Json.fromJson(userNodeFee, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(refundPayInFeeNotIncludedFee);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeNotIncludedFee);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeNotIncludedFee.getStatus());
    assertEquals(TransactionType.TRANSFER_FEE, refundPayInFeeNotIncludedFee.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeNotIncludedFee.getNature());
    assertEquals(2, refundPayInFeeNotIncludedFee.getAmount().getValue().intValue());

    // get refundPayInFeeNotIncluded from data base to acces the provider id
    refundPayInFeeNotIncluded =
        datastoreTestAccount
            .find(Refund.class)
            .filter("id", refundPayInFeeNotIncluded.getId())
            .get();
    System.out.println(
        "refundPayInFeeNotIncluded: " + utilsService.prettyPrintObject(refundPayInFeeNotIncluded));

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(refundPayInFeeNotIncluded.getProviderId())
            .concat("&EventType=PAYIN_REFUND_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(refundPayInFeeNotIncluded.getProviderId());

    // gat payin from database
    refundPayInFeeNotIncluded =
        datastoreTestAccount
            .find(Refund.class)
            .filter("id", refundPayInFeeNotIncluded.getId())
            .get();
    System.out.println(
        "refundPayInFeeNotIncluded: " + utilsService.prettyPrintObject(refundPayInFeeNotIncluded));
    assertEquals(TransactionStatus.SUCCEEDED, refundPayInFeeNotIncluded.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(33, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test017_RefundPayIn_Success_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInFeeIncluded.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // we must wait so that the async call to the provider to finish
    Thread.sleep(2000);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNull(jsonErrors);

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    refundPayInFeeIncluded = Json.fromJson(userNode, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(refundPayInFeeIncluded);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeIncluded);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeIncluded.getStatus());
    assertEquals(TransactionType.PAYOUT, refundPayInFeeIncluded.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeIncluded.getNature());
    assertEquals(50, refundPayInFeeIncluded.getAmount().getValue().intValue());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    refundPayInFeeIncludedFee = Json.fromJson(userNodeFee, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(refundPayInFeeIncludedFee);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeIncludedFee);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeIncludedFee.getStatus());
    assertEquals(TransactionType.TRANSFER_FEE, refundPayInFeeIncludedFee.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeIncludedFee.getNature());
    assertEquals(5, refundPayInFeeIncludedFee.getAmount().getValue().intValue());

    // get refundPayInFeeNotIncluded from data base to acces the provider id
    refundPayInFeeIncluded =
        datastoreTestAccount.find(Refund.class).filter("id", refundPayInFeeIncluded.getId()).get();
    System.out.println(
        "refundPayInFeeIncluded: " + utilsService.prettyPrintObject(refundPayInFeeIncluded));

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(refundPayInFeeIncluded.getProviderId())
            .concat("&EventType=PAYIN_REFUND_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(refundPayInFeeIncluded.getProviderId());

    // gat payin from database
    refundPayInFeeIncluded =
        datastoreTestAccount.find(Refund.class).filter("id", refundPayInFeeIncluded.getId()).get();
    System.out.println(
        "refundPayInFeeNotIncluded: " + utilsService.prettyPrintObject(refundPayInFeeIncluded));
    assertEquals(TransactionStatus.SUCCEEDED, refundPayInFeeIncluded.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(-22, walletUserA.getBalance().getValue().intValue());
  }

  @Test
  public void test018_RefundPayIn_CustomTagMaxLength() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get errors
    ResponseError[] responseErrors = Json.fromJson(jsonErrors, ResponseError[].class);

    // construct expected list of errors
    List<String> errors = new ArrayList<>();
    errors.add(ErrorMessage.ERROR_CREATEPAYINREFUND_TAG_MAXLENGTH);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(1, responseErrors.length);
  }

  @Test
  public void test019_RefundPayIn_AmountCurrencyInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID, responseError.getErrorDescription());
  }

  @Test
  public void test020_RefundPayIn_AmountCurrencyRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(ErrorMessage.ERROR_AMOUNT_CURRENCY_REQUIRED, responseError.getErrorDescription());
  }

  @Test
  public void test021_RefundPayIn_AmountValueRequired() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(ErrorMessage.ERROR_AMOUNT_VALUE_REQUIRED, responseError.getErrorDescription());
  }

  @Test
  public void test022_RefundPayIn_AmountValueInvalid() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // test name
    System.out.println(testName);

    String url = routes.TransactionController.refundPayIn(payInWithZeroFee.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(ErrorMessage.ERROR_AMOUNT_INVALID, responseError.getErrorDescription());
  }

  @Test
  public void test023_CreateDirectPayIn_FeeIncluded_ForFailure() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletUserA.getBalance().getCurrency().toString());
    parameters.put("creditedWalletId", walletUserA.getId());
    parameters.put("secureModeReturnURL", SECURE_MODE_RETURN_URL);
    parameters.put("cardId", depositCard.getId());

    String url = routes.TransactionController.createDirectPayIn().url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, parameters, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    payInFeeIncludedFailure = Json.fromJson(userNode, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(payInFeeIncludedFailure);

    // do some other checks by performing asserts
    assertNotNull(payInFeeIncludedFailure);
    assertEquals(TransactionStatus.CREATED, payInFeeIncludedFailure.getStatus());
    assertEquals("Success", payInFeeIncludedFailure.getResultMessage());
    assertEquals("000000", payInFeeIncludedFailure.getResultCode());
    assertEquals(TransactionType.PAYIN, payInFeeIncludedFailure.getType());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    payInFeeIncludedFailureFee = Json.fromJson(userNodeFee, PayIn.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(payInFeeIncludedFailureFee);

    // do some other checks by performing asserts
    assertNotNull(payInFeeIncludedFailureFee);
    assertEquals(TransactionStatus.CREATED, payInFeeIncludedFailureFee.getStatus());

    String urlCallback = routes.CallbackController.mango("moneymailme", "m3Service").url();

    urlCallback =
        urlCallback
            .concat("?RessourceId=")
            .concat(payInFeeIncludedFailure.getProviderId())
            .concat("&EventType=PAYIN_NORMAL_SUCCEEDED&Date=1581663625");
    System.out.println("CreateTransferTests - urlCallback=" + urlCallback);

    JsonNode jsonResponseCallback =
        httpHelper.executeRequest(GET, app, autheticationToken, urlCallback, null, requestId, JSON);

    System.out.println(
        "jsonResponseCallback: " + utilsService.prettyPrintObject(jsonResponseCallback));

    appInjector.instanceOf(ProcessCallbacksJob.class).executeCallbacksJob();
    callbackStrings.add(payInFeeIncludedFailure.getProviderId());

    // gat payin from database
    payInFeeIncludedFailure =
        datastoreTestAccount.find(PayIn.class).filter("id", payInFeeIncludedFailure.getId()).get();
    System.out.println(
        "payInFeeNotIncluded: " + utilsService.prettyPrintObject(payInFeeIncludedFailure));
    assertEquals(TransactionStatus.SUCCEEDED, payInFeeIncludedFailure.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(23, walletUserA.getBalance().getValue().intValue());
  }

  /**
   * Refund a Transfer created with below valid data: <br>
   * feeModel as INCLUDED <br>
   * fee value different than zero <br>
   * Note: When the fee is different than zero we will have two transactions created in db
   */
  @Test
  public void test024_RefundPayIn_ProviderFailureCase_FeeIncluded() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String url = routes.TransactionController.refundPayIn(payInFeeIncludedFailure.getId()).url();
    System.out.println("CreateTransferTests - url: " + url);

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, url, null, requestId, JSON);

    // we must wait so that the async call to the provider to finish
    Thread.sleep(2000);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get data from body
    JsonNode userNode = jsonResponse.at("/body/transactions").get(0);

    // get data from body to object
    refundPayInFeeIncludedFailure = Json.fromJson(userNode, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNode);
    assertNotNull(refundPayInFeeIncludedFailure);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeIncludedFailure);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeIncludedFailure.getStatus());
    assertEquals(TransactionType.PAYOUT, refundPayInFeeIncludedFailure.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeIncludedFailure.getNature());
    assertEquals(50, refundPayInFeeIncludedFailure.getAmount().getValue().intValue());

    // get data from body
    JsonNode userNodeFee = jsonResponse.at("/body/transactions").get(1);

    // get data from body to object
    refundPayInFeeIncludedFailureFee = Json.fromJson(userNodeFee, Refund.class);

    // perform asserts
    assertEquals(true, responseSuccess);
    assertNotNull(userNodeFee);
    assertNotNull(refundPayInFeeIncludedFailureFee);

    // do some other checks by performing asserts
    assertNotNull(refundPayInFeeIncludedFailureFee);
    assertEquals(TransactionStatus.CREATED, refundPayInFeeIncludedFailureFee.getStatus());
    assertEquals(TransactionType.TRANSFER_FEE, refundPayInFeeIncludedFailureFee.getType());
    assertEquals(TransactionNature.REFUND, refundPayInFeeIncludedFailureFee.getNature());
    assertEquals(5, refundPayInFeeIncludedFailureFee.getAmount().getValue().intValue());

    // test the database statuses
    refundPayInFeeIncludedFailure =
        datastoreTestAccount
            .find(Refund.class)
            .filter("id", refundPayInFeeIncludedFailure.getId())
            .get();
    assertEquals(TransactionStatus.FAILED, refundPayInFeeIncludedFailure.getStatus());

    refundPayInFeeIncludedFailureFee =
        datastoreTestAccount
            .find(Refund.class)
            .filter("id", refundPayInFeeIncludedFailureFee.getId())
            .get();
    assertEquals(TransactionStatus.FAILED, refundPayInFeeIncludedFailure.getStatus());

    // get wallet
    walletUserA = datastoreTestAccount.find(Wallet.class).filter("id", walletUserA.getId()).get();
    System.out.println("walletUserA: " + utilsService.prettyPrintObject(walletUserA));
    assertEquals(23, walletUserA.getBalance().getValue().intValue());
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
        datastoreTestAccount.createQuery(User.class).filter("id", userA.getId()));

    // delete created user from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(User.class).filter("id", userB.getId()));

    // delete created wallet from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletUserA.getId()));

    // delete created wallet from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Wallet.class).filter("id", walletUserB.getId()));

    // delete deposit card
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(DepositCard.class).filter("id", depositCard.getId()));

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", transfer.getId()));

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(Transaction.class).filter("id", payInWithZeroFee.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Refund.class)
            .filter("initialTransactionId", payInWithZeroFee.getId()));

    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("providerId", payInFeeNotIncluded.getProviderId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("providerId", refundPayInFeeNotIncluded.getProviderId()));

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("providerId", payInFeeIncluded.getProviderId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("providerId", refundPayInFeeIncluded.getProviderId()));

    // delete created transaction from database
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("providerId", payInFeeIncludedFailure.getProviderId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("id", refundPayInFeeIncludedFailure.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount
            .createQuery(Transaction.class)
            .filter("id", refundPayInFeeIncludedFailureFee.getId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));

    callbackStrings
        .stream()
        .forEach(
            id -> {
              databaseService
                  .getMainConnection()
                  .delete(
                      databaseService
                          .getMainConnection()
                          .createQuery(ProcessedCallback.class)
                          .filter("parameters.RessourceId", id));
            });

    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
