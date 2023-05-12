package classes;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.mongodb.morphia.Datastore;
import com.mangopay.entities.User;
import play.Application;
import play.Mode;
import play.inject.Injector;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import ro.iss.lolopay.classes.AuthenticationResponse;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.jobs.JobsModule;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.ApplicationActivity;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.models.services.definition.CallbackService;
import ro.iss.lolopay.models.services.implementation.CallbackImplementation;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.SecurityService;
import ro.iss.lolopay.services.definition.UtilsService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.SecurityImplementation;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

public class WithCustomApplication extends WithApplication {

  // test environment main services - declare below other services required
  protected static DatabaseService databaseService;
  protected static SecurityService securityService;
  protected static Datastore datastoreTestAccount;
  protected static Datastore datastoreTestApplication;
  protected static Injector appInjector;
  protected static UtilsService utilsService;
  protected static CallbackService callbackService;

  // test environment main variables
  protected static ro.iss.lolopay.models.main.Account testAccount;
  protected static ro.iss.lolopay.models.database.Application testApplication;

  // test environment main file
  protected static String autheticationToken = "";

  protected static boolean isNotAuthenticated = true;

  protected static String requestId;

  @Override
  protected Application provideApplication() {
    setEnvVar("LOLOPAY_MONGODB_SERVER1_ADDRESS", "127.0.0.1");
    setEnvVar("LOLOPAY_MONGODB_SERVER2_ADDRESS", "127.0.0.1");
    setEnvVar("LOLOPAY_MONGODB_SERVER3_ADDRESS", "127.0.0.1");

    setEnvVar("LOLOPAY_MANGOPAY_BASEURL", "http://localhost");
    setEnvVar("LOLOPAY_PFS_BASEURL", "http://localhost");
    setEnvVar(
        "LOLOPAY_MANGOPAY_RETURNURL",
        "http://localhost/callbacks/mango/moneymailme/m3Service/return");
    setEnvVar(
        "LOLOPAY_MANGOPAY_RETURNURL",
        "http://localhost/service/%s/deposit/updateRegistrationData/%s");
    setEnvVar("LOLOPAY_PFS_BASEURL", "http://localhost");

    return new GuiceApplicationBuilder().in(Mode.TEST).disable(JobsModule.class).build();
  }

  /** Gets called before every test */
  @Before
  public void beforeEachTest() {

    // get application injector
    appInjector = app.injector();

    // init test environment services
    initServices();

    // authenticate
    if (isNotAuthenticated) {
      authenticate();
    }

    requestId = UUID.randomUUID().toString();
  }

  /** Gets called after every test */
  @After
  public void afterEachTest() {

    // delete requestId
    datastoreTestApplication.delete(
        datastoreTestApplication.createQuery(RequestHistory.class).filter("requestId", requestId));
  }

  public static void countDatabaseRecordsAndAssert() {
    long users = datastoreTestAccount.createQuery(User.class).count();
    assertEquals(0l, users);
    long wallets = datastoreTestAccount.createQuery(Wallet.class).count();
    assertEquals(6l, wallets);
    long transactions = datastoreTestAccount.createQuery(Transaction.class).count();
    assertEquals(0l, transactions);
    long settings = datastoreTestAccount.createQuery(AccountSettings.class).count();
    assertEquals(1l, settings);
    long documents = datastoreTestAccount.createQuery(Document.class).count();
    assertEquals(0l, documents);
    long depositCards = datastoreTestAccount.createQuery(DepositCard.class).count();
    assertEquals(0l, depositCards);
    long companyBankCardWallets =
        datastoreTestAccount.createQuery(CompanyBankCardWallet.class).count();
    assertEquals(3l, companyBankCardWallets);
    long bankCards = datastoreTestAccount.createQuery(BankCard.class).count();
    assertEquals(0l, bankCards);
    long bankCardWallets = datastoreTestAccount.createQuery(BankCardWallet.class).count();
    assertEquals(0l, bankCardWallets);
    long bankAccounts = datastoreTestAccount.createQuery(BankAccount.class).count();
    assertEquals(0l, bankAccounts);
    long applicationsActivities =
        datastoreTestAccount.createQuery(ApplicationActivity.class).count();
    assertEquals(1l, applicationsActivities);
    long applications =
        datastoreTestAccount.createQuery(ro.iss.lolopay.models.database.Application.class).count();
    assertEquals(1l, applications);
    //
    long accounts = datastoreTestApplication.createQuery(Account.class).count();
    assertEquals(1l, accounts);
    long callbacks = datastoreTestApplication.createQuery(Callback.class).count();
    assertEquals(0l, callbacks);
    long failedCallbacks = datastoreTestApplication.createQuery(FailedCallback.class).count();
    assertEquals(0l, failedCallbacks);
    long processedCallbacks = datastoreTestApplication.createQuery(ProcessedCallback.class).count();
    assertEquals(0l, processedCallbacks);
    long requestHistory = datastoreTestApplication.createQuery(RequestHistory.class).count();
    assertEquals(0l, requestHistory);
    long sessions = datastoreTestApplication.createQuery(Session.class).count();
    assertEquals(0l, sessions);
  }

  private void initServices() {

    // get injected database implementation from app
    databaseService = appInjector.instanceOf(DatabaseImplementation.class);

    // get injected security service implementation from app
    securityService = appInjector.instanceOf(SecurityImplementation.class);

    // get first database account found
    testAccount = databaseService.getMainConnection().createQuery(Account.class).asList().get(0);

    // Initialise Datastore for test account
    datastoreTestAccount = databaseService.getConnection(testAccount.getId());

    // get first account application found
    testApplication =
        databaseService
            .getConnection(testAccount.getId())
            .createQuery(ro.iss.lolopay.models.database.Application.class)
            .asList()
            .get(0);

    // Initialise Datastore for test application
    datastoreTestApplication = databaseService.getMainConnection();

    // init utils service for pretty printing
    utilsService = appInjector.instanceOf(UtilsImplementation.class);

    callbackService = appInjector.instanceOf(CallbackImplementation.class);
  }

  /**
   * Retrieve main account and main account application. Register within the app and get login token
   */
  protected static void authenticate() {

    // try to authenticate the application
    AuthenticationResponse authenticationResponse =
        securityService.authenticateApplication(
            "",
            testAccount.getId(),
            testApplication.getId(),
            testApplication.getApplicationPassword());

    // register application session
    TokenSet sessionToken =
        securityService.registerSession("", authenticationResponse, "127.0.0.1");

    autheticationToken = sessionToken.getAutheticationToken();
    isNotAuthenticated = false;
  }

  /**
   * Sets an environment variable
   *
   * @param key
   * @param value
   */
  @SuppressWarnings("unchecked")
  private static void setEnvVar(String key, String value) {

    try {
      Map<String, String> env = System.getenv();

      Field field = env.getClass().getDeclaredField("m");
      field.setAccessible(true);

      Map<String, String> writableEnv = (Map<String, String>) field.get(env);

      writableEnv.put(key, value);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to set environment variable", e);
    }
  }
}
