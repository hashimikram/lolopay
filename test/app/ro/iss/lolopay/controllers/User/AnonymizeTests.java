package app.ro.iss.lolopay.controllers.User;

import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.MimeTypes.JSON;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static shared.ProviderApis.MANGO_CREATE_NATURAL_USER;
import static shared.ProviderApis.MANGO_OAUTH;
import static shared.Providers.MANGO;

import classes.HttpHelper;
import classes.WithCustomApplication;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.BuiltInComponents;
import play.libs.Json;
import play.mvc.Http;
import play.routing.Router;
import play.routing.RoutingDsl;
import play.server.Server;
import ro.iss.lolopay.controllers.routes;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Session;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AnonymizeTests extends WithCustomApplication {

  private static User naturalUser;
  private static Server server;

  private static String testName;
  private static int responseHttpStatus;

  private static HttpHelper httpHelper;
  private static String uri;

  @BeforeClass
  public static void init() {
    httpHelper = new HttpHelper("User", "AnonymizeTests");
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
        .build();
  }

  @Test
  public void test001_CreateNatural() throws Exception {

    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UserController.createNatural().url();

    JsonNode jsonResponse =
        httpHelper.executeRequest(POST, app, autheticationToken, uri, null, requestId, JSON);

    // get data from response as object
    naturalUser = Json.fromJson(jsonResponse.at("/body/user"), User.class);

    // perform asserts
    assertNotNull(naturalUser);

    uri = routes.UserController.get(naturalUser.getId()).url();
  }

  @Test
  public void test002_Anonymize() throws Exception {
    // set variables for mock server response
    testName = httpHelper.getMethodName();

    // print test name
    System.out.println(testName);

    uri = routes.UserController.anonymize(naturalUser.getId()).url();
    JsonNode jsonResponse =
        httpHelper.executeRequest(GET, app, autheticationToken, uri, null, requestId, JSON);

    System.out.println(utilsService.prettyPrintObject(jsonResponse));
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
        datastoreTestAccount.createQuery(User.class).filter("id", naturalUser.getId()));
    datastoreTestApplication.delete(
        datastoreTestApplication
            .createQuery(Session.class)
            .filter("accountId", testAccount.getId()));
    isNotAuthenticated = true;
    WithCustomApplication.countDatabaseRecordsAndAssert();
  }
}
