package app.ro.iss.lolopay.controllers.Callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.test.Helpers.GET;
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
import play.libs.Json;
import play.mvc.Http.MimeTypes;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.Session;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MangoTests extends WithCustomApplication {

  private static HttpHelper httpHelper;
  private static Callback callback;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("Callback", "MangoTests");
  }

  @Override
  @Before
  public void beforeEachTest() {
    super.beforeEachTest();
  }

  @Test
  public void test001_Mango_MissingRessourceId() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("moneymailme", "m3Service").url();
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);
  }

  @Test
  public void test002_Mango_MissingEventTypeId() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("moneymailme", "m3Service").url();
    uri = uri.concat("?");
    uri = uri.concat("RessourceId=111111");
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);
  }

  @Test
  public void test003_Mango_MissingDate() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("moneymailme", "m3Service").url();
    uri = uri.concat("?");
    uri = uri.concat("RessourceId=1");
    uri = uri.concat("&");
    uri = uri.concat("EventType=1");
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);
  }

  @Test
  public void test004_Mango_AccountNotFound() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("accountId", "m3Service").url();
    uri = uri.concat("?");
    uri = uri.concat("RessourceId=1");
    uri = uri.concat("&");
    uri = uri.concat("EventType=PFS_CALLBACK_TRANSACTION");
    uri = uri.concat("&");
    uri = uri.concat("Date=1");
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);
  }

  @Test
  public void test005_Mango_ApplicationNotFound() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("moneymailme", "applicationId").url();
    uri = uri.concat("?");
    uri = uri.concat("RessourceId=1");
    uri = uri.concat("&");
    uri = uri.concat("EventType=PFS_CALLBACK_TRANSACTION");
    uri = uri.concat("&");
    uri = uri.concat("Date=1");
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);
  }

  @Test
  public void test006_Mango_Success() throws Exception {

    // print test name
    System.out.println(httpHelper.getMethodName());

    String uri = routes.CallbackController.mango("moneymailme", "m3Service").url();
    uri = uri.concat("?");
    uri = uri.concat("RessourceId=1");
    uri = uri.concat("&");
    uri = uri.concat("EventType=TRANSFER_NORMAL_SUCCEEDED");
    uri = uri.concat("&");
    uri = uri.concat("Date=1");
    System.out.println("uri" + uri);
    JsonNode jsonResponse =
        httpHelper.executeRequest(
            GET, app, autheticationToken, uri, null, requestId, MimeTypes.JSON);

    assertEquals(Json.parse("{}"), jsonResponse);

    // get callback form database
    callback =
        datastoreTestApplication
            .createQuery(Callback.class)
            .filter("provider", "MANGO")
            .filter("accountId", "moneymailme")
            .filter("applicationId", "m3Service")
            .filter("parameters.EventType", "TRANSFER_NORMAL_SUCCEEDED")
            .filter("parameters.RessourceId", "1")
            .filter("parameters.Date", "1")
            .get();
    assertNotNull(callback);
    assertEquals("MANGO", callback.getProvider());
  }

  /** Gets called after every test */
  @Override
  @After
  public void afterEachTest() {
    super.afterEachTest();
  }

  /** Gets called after all tests */
  @AfterClass
  public static void cleanDatabaseAfterAllTests() {

    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    datastoreTestApplication.delete(Callback.class, callback.getId());

    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
