package app.ro.iss.lolopay.controllers.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_CREATE_TRANSACTION_PAYIN;
import static shared.ProviderApis.MANGO_CREATE_WALLET;
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
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.responses.ResponseError;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreatePayInTests extends WithCustomApplication {
  private static String userId;
  private static String providerId;
  private static String walletId;
  private static String walletProviderId;
  private static String walletCurrency;
  private static String transactionId;
  private static String transactionFeeId;
  private static String RETURN_URL = "http://www.my-site.com/returnURL/";

  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri = routes.TransactionController.createPayIn().url();

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Transaction", "CreatePayInTests");
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
        .POST(MANGO_CREATE_WALLET.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, MANGO, responseHttpStatus))
        .POST(MANGO_CREATE_TRANSACTION_PAYIN.getUri())
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

    System.out.println("CreatePayInTests - userId=" + userId);
    System.out.println("CreatePayInTests - user providerId=" + providerId);
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

    System.out.println("CreatePayInTests - walletId=" + walletId);
    System.out.println("CreatePayInTests - walletProviderId=" + walletProviderId);
    System.out.println("CreatePayInTests - walletCurrency=" + walletCurrency);
  }

  /**
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * Note: When the fee is zero we will only have a single transaction created in db
   */
  @Test
  public void test003_CreatePayIn_SuccessCase_SmallZeroIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayIn - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());
  }

  /**
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as INCLUDED <br>
   */
  @Test
  public void test004_CreatePayIn_SuccessCase_BigZeroIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("55", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());
  }

  /**
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as NOT_INCLUDED <br>
   * Note: When the fee is not included, we will have 2 transactions created in db
   */
  @Test
  public void test005_CreatePayIn_SuccessCase_SmallNotIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as NOT_INCLUDED <br>
   */
  @Test
  public void test006_CreatePayIn_SuccessCase_BigNotIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("58", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED
   */
  @Test
  public void test007_CreatePayIn_SuccessCase_SmallIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());
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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor missing <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * TemplateOption missing
   */
  @Test
  public void test008_CreatePayIn_SuccessCase_Small() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * big amount <br>
   * feeModel as NOT_INCLUDED <br>
   */
  @Test
  public void test009_CreatePayIn_SuccessCase_BigIncludedForceNotSecured() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("FORCE", transactionPayIn.getSecureMode().toString());
    assertEquals("55", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as FORCE <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   */
  @Test
  public void test010_CreatePayIn_SuccessCase_SmallIncludedForceNotSecured() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("FORCE", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor not empty <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * Note: the status of the transactions will be FAILED because the amount is too small <br>
   * NOTE!!!: for this test i needed to change createProviderPayIn from BusinessImplementation.java
   */
  @Test
  public void test011_CreatePayIn_SuccessCase_TooSmallIncludedDefault() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);
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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals(
        "Transaction amount is lower than minimum permitted amount",
        transactionPayIn.getResultMessage().toString());
    assertEquals("001012", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("5", transactionPayIn.getAmount().getValue().toString());
    assertEquals("", transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertEquals(null, transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor missing <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel as INCLUDED <br>
   * TemplateOption added <br>
   * NOTE!!!: The secureMode is not mandatory in mango but for our API it is!!!
   */
  @Test
  public void test012_CreatePayIn_SuccessCase_SmallIncludedTemplate() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertNotNull(transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create PayIn with valid data: <br>
   * statementDescriptor missing <br>
   * secureMode as DEFAULT <br>
   * small amount <br>
   * feeModel missing <br>
   * TemplateOption added <br>
   * NOTE!!!: The secureMode is not mandatory in mango but for our API it is!!!
   */
  @Test
  public void test013_CreatePayIn_SuccessCase_SmallNotIncludedTemplate() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("currency", walletCurrency);
    parameters.put("creditedWalletId", walletId);
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("CREATED", transactionPayIn.getStatus().toString());
    assertEquals("", transactionPayIn.getResultMessage().toString());
    assertEquals("", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("12", transactionPayIn.getAmount().getValue().toString());
    assertNotNull(transactionPayIn.getRedirectURL());
    assertNotNull(transactionPayIn.getReturnURL());
    assertNotNull(transactionPayIn.getTemplateURL());

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
    System.out.println("CreatePayInTests - transactionFeeId = " + transactionFeeId);

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
   * Create DirectPayIn with provider error response (invalid wallet Id) <br>
   * NOTE: This test case will always fail on sandbox because we cannot actually reproduce a 400
   * response, but on local host is working fine.
   */
  @Test
  public void test014_CreatePayIn_ProviderFailureCase() throws Exception {

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
    parameters.put("returnURL", RETURN_URL);

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
    System.out.println("CreatePayInTests - transactionId = " + transactionId);

    // do some other checks by performing asserts
    assertNotNull(transactionPayIn);
    assertEquals("FAILED", transactionPayIn.getStatus().toString());
    assertEquals(
        errorDescription,
        Json.parse(transactionPayIn.getResultMessage()).findPath("Message").asText());
    assertEquals("ERROR_PROVIDER", transactionPayIn.getResultCode().toString());
    assertEquals("PAYIN", transactionPayIn.getType().toString());
    assertEquals("CARD", transactionPayIn.getPaymentType().toString());
    assertEquals("WEB", transactionPayIn.getExecutionType().toString());
    assertEquals("DEFAULT", transactionPayIn.getSecureMode().toString());
    assertEquals("10", transactionPayIn.getAmount().getValue().toString());
  }

  /** Create PayIn with long customTag field */
  @Test
  public void test015_CreatePayIn_CustomTagMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_CREATEPAYIN_TAG_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create PayIn with missing creditWalletId field */
  @Test
  public void test016_CreatePayIn_WalletIdRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_REQUIRED,
        responseError.getErrorDescription());
  }

  /** Create PayIn with invalid creditWalletId field */
  @Test
  public void test017_CreatePayIn_WalletIdInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with creditWalletId field inexistent in DB */
  @Test
  public void test018_CreatePayIn_NoWallet() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", ObjectId.get().toString());
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLET_INEXISTENT,
        responseError.getErrorDescription());
  }

  /** Create PayIn with missing debit funds field */
  @Test
  public void test019_CreatePayIn_DebitFundsRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_DEBITFUNDS_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with missing amount value field */
  @Test
  public void test020_CreatePayIn_AmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid amount value field */
  @Test
  public void test021_CreatePayIn_AmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid (negative) amount value field */
  @Test
  public void test022_CreatePayIn_NegativeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with missing amount currency field */
  @Test
  public void test023_CreatePayIn_AmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid amount currency field */
  @Test
  public void test024_CreatePayIn_AmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with mismatch amount currency field and wallet currency */
  @Test
  public void test025_CreatePayIn_AmountCurrencyWalletMismatchInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_INVALID,
        responseError.getErrorDescription());
  }

  /** Create PayIn with missing fees field */
  @Test
  public void test026_CreatePayIn_FeesRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, uri, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get responseError
    ResponseError responseError = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    assertEquals(false, responseSuccess);
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_CREATEPAYIN_FEES_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with missing fee amount value field */
  @Test
  public void test027_CreatePayIn_FeeAmountRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid fee amount value field */
  @Test
  public void test028_CreatePayIn_FeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid (negative) fee amount value field */
  @Test
  public void test029_CreatePayIn_NegativeFeeAmountInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with missing fee amount currency field */
  @Test
  public void test030_CreatePayIn_FeeAmountCurrencyRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with invalid fee amount currency field */
  @Test
  public void test031_CreatePayIn_FeeAmountCurrencyInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with mismatch amount currency and fee amount currency field */
  @Test
  public void test032_CreatePayIn_FeeAmountCurrencyMismatch() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_REQUEST_CURRENCY_MISSMATCH,
        responseError.getErrorDescription());
  }

  /** Create PayIn with amount value smaller than fee amount value field */
  @Test
  public void test033_CreatePayIn_AmountSmallerThanFee() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_REQUEST_AMOUNT_SMALLER_THAN_FEE,
        responseError.getErrorDescription());
  }

  /** Create PayIn with amount value field set as zero */
  @Test
  public void test034_CreatePayIn_AmountIsZero() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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

  /** Create PayIn with missing fee model field */
  @Test
  public void test035_CreatePayIn_FeeModelRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with fee model field set as empty */
  @Test
  public void test036_CreatePayIn_FeeModelRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid fee model field */
  @Test
  public void test037_CreatePayIn_FeeModelInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with missing ReturnUrl field */
  @Test
  public void test038_CreatePayIn_ReturnUrlRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with long ReturnUrl field */
  @Test
  public void test039_CreatePayIn_ReturnUrlMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid ReturnUrl field */
  @Test
  public void test040_CreatePayIn_ReturnUrlInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with missing cardType field */
  @Test
  public void test041_CreatePayIn_CardTypeRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid cardType field */
  @Test
  public void test042_CreatePayIn_CardTypeInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with missing secureMode field */
  @Test
  public void test043_CreatePayIn_SecureModeRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with empty secureMode field */
  @Test
  public void test044_CreatePayIn_SecureModeRequiredEmpty() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid secureMode field */
  @Test
  public void test045_CreatePayIn_SecureModeInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with long templateURL field */
  @Test
  public void test046_CreatePayIn_TemplateUrlMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_MAXLENGTH, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid templateURL field */
  @Test
  public void test047_CreatePayIn_TemplateUrlInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with missing creditWalletId field */
  @Test
  public void test048_CreatePayIn_CultureRequired() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CULTURE_REQUIRED, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid creditWalletId field */
  @Test
  public void test049_CreatePayIn_CultureInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_CULTURE_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with invalid statement description field */
  @Test
  public void test050_CreatePayIn_StatementDescriptionInvalid() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_INVALID, responseError.getErrorDescription());
  }

  /** Create PayIn with long statement description field */
  @Test
  public void test051_CreatePayIn_StatementDescriptionMaxLength() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("currency", walletCurrency);
    bodyParameters.put("creditedWalletId", walletId);
    bodyParameters.put("returnURL", RETURN_URL);

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
        ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_MAXLENGTH,
        responseError.getErrorDescription());
  }

  /** Create PayIn with missing all required fields */
  @Test
  public void test052_CreatePayIn_AllRequired() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_FEES_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_DEBITFUNDS_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CULTURE_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_REQUIRED);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_REQUIRED);
    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(8, responseErrors.length);
  }

  /** Create PayIn with missing all required fields */
  @Test
  public void test053_CreatePayIn_AllInvalidMaxLength() throws Exception {

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
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_TAG_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_INVALID);
    // below error is for invalid amount
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    // below error is for invalid fees
    errors.add(ErrorMessage.ERROR_AMOUNT_INVALID);
    errors.add(ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_CULTURE_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_MAXLENGTH);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_INVALID);
    errors.add(ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_MAXLENGTH);

    for (ResponseError responseError : responseErrors) {
      errors.remove(responseError.getErrorDescription());
    }

    // perform asserts
    assertEquals(false, responseSuccess);
    assertEquals(0, errors.size());
    assertEquals(16, responseErrors.length);
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
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
