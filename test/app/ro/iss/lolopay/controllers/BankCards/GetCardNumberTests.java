package app.ro.iss.lolopay.controllers.BankCards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.MimeTypes.JSON;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.PFS_CREATE_CARD;
import static shared.ProviderApis.PFS_GET_CARD_NUMBER;
import static shared.ProviderApis.PFS_GET_CARD_WALLET_TRANSACTIONS;
import static shared.Providers.MANGO;
import static shared.Providers.PFS;
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
public class GetCardNumberTests extends WithCustomApplication {

  private static String userId;
  private static String bankCardId;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("BankCards", "GetCardNumberTests");
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
        .POST(PFS_GET_CARD_WALLET_TRANSACTIONS.getUri())
        .routingTo(req -> httpHelper.getProviderResponse(testName, PFS, responseHttpStatus))
        .POST(PFS_GET_CARD_NUMBER.getUri())
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

    uri = routes.BankCardsController.getCardNumber(bankCardId).url();

    assertEquals(true, responseSuccess);
  }

  @Test
  public void test003_GetCardNumberTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get body from response
    JsonNode body = jsonResponse.at("/body");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertTrue(body.has("cardNumber"));
    assertEquals("5919099501129531", body.at("/cardNumber").asText());
  }

  @Test
  public void test004_GetCardNumberTests_InvalidCardId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.BankCardsController.getCardNumber("asd@.").url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_GETCARDNO_INVALID_CARDID, error.getErrorDescription());
  }

  @Test
  public void test005_GetCardNumberTests_InexistentCard() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.BankCardsController.getCardNumber("5e3180549fcbe1c2d9f41daa").url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get response errors
    JsonNode jsonErrors = jsonResponse.findValue("errors");

    // get error
    ResponseError error = Json.fromJson(jsonErrors.get(0), ResponseError.class);

    // perform asserts
    assertEquals(false, responseSuccess);
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_GETCARDNO_INEXISTENT_CARD, error.getErrorDescription());
  }

  @Test
  public void test006_GetCardNumberTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.BankCardsController.getCardNumber(bankCardId).url();
    responseHttpStatus = BAD_REQUEST;

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

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
