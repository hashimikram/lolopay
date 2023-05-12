package app.ro.iss.lolopay.controllers.BankCards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.ProviderApis.PFS_CREATE_CARD;
import static shared.ProviderApis.PFS_GET_CARD_CVV;
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
public class GetCardCvvTests extends WithCustomApplication {

  private static User user;
  private static BankCard bankCard;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("BankCards", "GetCardCvvTests");
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
        .POST(PFS_GET_CARD_CVV.getUri())
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
    user = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(user);
  }

  /** Create card */
  @Test
  public void test002_CreateCard() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    testName = httpHelper.getMethodName();

    Map<String, String> bodyParameters = new HashMap<String, String>();
    bodyParameters.put("userId", user.getId());

    String url = routes.BankCardsController.createCard().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(
            POST, app, autheticationToken, url, bodyParameters, requestId, JSON);

    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    assertTrue(responseSuccess);

    JsonNode responseBody = jsonResponse.findPath("body");
    // add assert field exists of body/id field
    assertTrue(responseBody.has("id"));

    bankCard =
        datastoreTestAccount
            .createQuery(BankCard.class)
            .filter("id", responseBody.at("/id").asText())
            .get();

    // get card from database and save it to banckCard attribute
    assertNotNull(bankCard);

    uri = routes.BankCardsController.getCardCVV(bankCard.getId()).url();
    System.out.println(bankCard.getId());

    assertEquals(true, responseSuccess);
  }

  @Test
  public void test003_GetCardCvvTests_Success() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);
    System.out.println(bankCard.getId());

    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();

    // get body from response
    JsonNode body = jsonResponse.at("/body");

    // perform asserts
    assertEquals(true, responseSuccess);
    assertTrue(body.has("cvv"));
    assertEquals("237", body.at("/cvv").asText());
  }

  @Test
  public void test004_GetCardCvvTests_InvalidCardId() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.BankCardsController.getCardCVV("asd@.").url();

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
    assertEquals(1, jsonErrors.size());
    assertEquals(ErrorMessage.ERROR_GETCARDCVV_INVALID_CARDID, error.getErrorDescription());
  }

  @Test
  public void test005_GetCardCvvTests_InexistentCard() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    String uri = routes.BankCardsController.getCardCVV("5e3180549fcbe1c2d9f41daa").url();

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
    assertEquals(1, jsonErrors.size());
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_GETCARDCVV_INEXISTENT_CARD, error.getErrorDescription());
  }

  @Test
  public void test006_GetCardCvvTests_ProviderError() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    responseHttpStatus = Http.Status.BAD_REQUEST;

    String uri = routes.BankCardsController.getCardCVV(bankCard.getId()).url();

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
    assertEquals(1, jsonErrors.size());
    assertNotNull(jsonErrors);
    assertEquals(ErrorMessage.ERROR_PROVIDER, error.getErrorCode());
    assertEquals("Invalid request", error.getErrorDescription());
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
        datastoreTestAccount.createQuery(User.class).filter("id", user.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankCard.class).filter("id", bankCard.getId()));
    datastoreTestAccount.delete(
        datastoreTestAccount.createQuery(BankCardWallet.class).filter("cardId", bankCard.getId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
