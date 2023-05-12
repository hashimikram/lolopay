package app.ro.iss.lolopay.controllers.Callback;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.MimeTypes.FORM;
import static play.test.Helpers.POST;
import java.util.List;
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
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.Session;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PfsRtfTests extends WithCustomApplication {

  private static Server server;

  private static HttpHelper httpHelper;
  private static String uri = routes.CallbackController.pfsRtf("moneymailme", "m3Service").url();

  private static String callbackId;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Callback", "PfsRtfTests");
  }

  @Override
  @Before
  public void beforeEachTest() {
    super.beforeEachTest();
    server = new Server.Builder().http(80).build(components -> getRoutingDsl(components));
  }

  private Router getRoutingDsl(BuiltInComponents components) {
    return RoutingDsl.fromComponents(components).build();
  }

  @Test
  public void test001_pfsrtfPurchase() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);

    for (Callback callback : callbacks) {
      callbackId = callback.getId();
      assertEquals("0000000060.00", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("GBP", String.valueOf(callback.getParameters().get("Currency")));
    }
  }

  /**
   * Fee transaction 15LR. don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test002_pfsrtf() throws Exception {

    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);
    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * Fee transaction 27LR. don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test003_pfsrtf27LRCardholderFee() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * Preauthorized purchase
   *
   * @throws Exception
   */
  @Test
  public void test004_pfsrtfPreauthorizedPurchase() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();

      assertEquals("0000000006.67", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("RON", String.valueOf(callback.getParameters().get("Currency")));
    }
  }

  /**
   * FX transaction, save amount
   *
   * @throws Exception
   */
  @Test
  public void test005_pfsrtf0110PreauthorizedPurchaseWithAmount2() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();

      assertEquals("0000000001.00", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("EUR", String.valueOf(callback.getParameters().get("Currency")));
    }
  }

  /**
   * No FX, no Fee, save amount1
   *
   * @throws Exception
   */
  @Test
  public void test006_pfsrtfPreauthorizedPurchaseNoFX() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();

      assertEquals("0000000060.00", String.valueOf(callback.getParameters().get("Amount")));
    }
  }

  /**
   * Deposit to card
   *
   * @throws Exception
   */
  @Test
  public void test007_pfsrtf27JRDepositToCard() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();

      assertEquals("0000000025.00", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("200005156369", String.valueOf(callback.getParameters().get("CardholderId")));
      assertEquals("EUR", String.valueOf(callback.getParameters().get("Currency")));
      assertEquals("20181122150524", String.valueOf(callback.getParameters().get("Date")));
    }
  }

  /**
   * Wallet sweep transaction 27IR, don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test008_pfsrtf15IRWalletSweep() throws Exception {

    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);
    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * Wallet sweep transaction 27IR, don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test009_pfsrtf27IRWalletSweep() throws Exception {

    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);
    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * Deposit to card
   *
   * @throws Exception
   */
  @Test
  public void test010_pfsrtf0110PendingPOSTransaction() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();
      assertEquals("0000000022.58", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("USD", String.valueOf(callback.getParameters().get("Currency")));
    }
  }

  /**
   * Wallet sweep transaction 27IR, don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test011_pfsrtf0230Settlement() throws Exception {

    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);
    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * Balance inquiry, don't save callback
   *
   * @throws Exception
   */
  @Test
  public void test012_pfsrtfBalanceInquiry() throws Exception {

    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);
    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(0, callbacks.size());
  }

  /**
   * ATM cash disbursment
   *
   * @throws Exception
   */
  @Test
  public void test013_pfsrtf0410CashReversal() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, FORM);

    assertEquals(Json.parse("{}"), jsonResponse);

    List<Callback> callbacks = callbackService.getOldestCallbacks(requestId);
    assertEquals(1, callbacks.size());

    for (Callback callback : callbacks) {
      callbackId = callback.getId();
      assertEquals("0000220000.00", String.valueOf(callback.getParameters().get("Amount")));
      assertEquals("XOF", String.valueOf(callback.getParameters().get("Currency")));
    }
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {
    super.afterEachTest();

    // delete processed callback from iss_lolopay_main.processedCallbacks
    datastoreTestApplication.delete(
        datastoreTestApplication.createQuery(Callback.class).filter("id", callbackId));

    // stop mock server
    server.stop();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
