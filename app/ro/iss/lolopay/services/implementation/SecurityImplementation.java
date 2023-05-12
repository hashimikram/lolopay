package ro.iss.lolopay.services.implementation;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;
import play.Logger;
import ro.iss.lolopay.classes.AuthenticationResponse;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.classes.ApplicationStatus;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.models.services.definition.SessionService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.SecurityService;
import ro.iss.lolopay.services.definition.TokenService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class SecurityImplementation implements SecurityService {
  @Inject private UtilsService utilsService;

  @Inject private AccountService accountService;

  @Inject private ApplicationService applicationService;

  @Inject private TokenService tokenService;

  @Inject private SessionService sessionService;

  @Inject private CoreService coreService;

  /**
   * Create default Account with an application associated with it
   *
   * @param accountName
   * @param accountEmail
   * @param applicationName
   * @param applicationUsername
   * @param applicationEmail
   * @param applicationPassword
   * @param applicationStatus
   * @param whoIsOperating
   * @return
   */
  @Override
  public AuthenticationResponse createAccount(
      String requestId,
      String accountName,
      String accountEmail,
      String applicationName,
      String applicationUsername,
      String applicationEmail,
      String applicationPassword,
      ApplicationStatus applicationStatus,
      ApplicationStamp whoIsOperating) {

    // generate database details
    String dbName = utilsService.generateDbName(accountName);
    String dbUser = "dbUser".concat(String.valueOf(utilsService.getCurrentTimeMiliseconds()));
    String dbPass = utilsService.generateRandomString(10);

    Logger.of(this.getClass()).debug("register:generated:dbName: " + dbName);
    Logger.of(this.getClass()).debug("register:generated:dbUser: " + dbUser);

    // create account in the system with provided details and allowed
    // application modules
    Account newAccount = new Account();
    newAccount.setAccountName(accountName);
    newAccount.setAccountEmail(accountEmail);
    newAccount.setDatabaseName(dbName);
    newAccount.setDatabaseUsername(dbUser);
    newAccount.setDatabasePassword(dbPass);
    newAccount.updateAudit(whoIsOperating);
    accountService.saveAccount(requestId, newAccount);

    // create new application in new database
    Application newApplication = new Application();
    newApplication.setApplicationName(applicationName);
    newApplication.setApplicationEmail(applicationEmail);
    newApplication.setApplicationPassword(applicationPassword);
    newApplication.setApplicationStatus(applicationStatus);
    applicationService.saveApplication(requestId, newAccount, newApplication);

    // register application session
    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setAuthenticatedAccount(newAccount);
    authenticationResponse.setAuthenticatedApplication(newApplication);

    return authenticationResponse;
  }

  /**
   * Try to authenticate an application. Return Account / Application record if properly
   * authenticated or null otherwise
   *
   * @param accountId
   * @param applicationId
   * @param password
   * @return
   */
  @Override
  public AuthenticationResponse authenticateApplication(
      String requestId, String accountId, String applicationId, String password) {

    Logger.of(this.getClass()).debug("authenticateApplication:accountId:" + accountId);
    Logger.of(this.getClass()).debug("authenticateApplication:applicationId:" + applicationId);

    // retrieve application account
    Account applicationAccount = accountService.getAccount(requestId, accountId);

    if (applicationAccount == null) {
      // account not found in the database
      Logger.of(this.getClass()).error("authenticateApplication: account not found in the system");
      return null;
    } else {
      // retrieve application
      Application application =
          applicationService.getApplication(requestId, applicationAccount, applicationId);

      if (application == null) {
        // application not found in the database
        Logger.of(this.getClass())
            .error("authenticateApplication: application not found in the system");
        return null;
      } else {
        // application found
        if (application.getApplicationPassword().equals(password)) {
          Logger.of(this.getClass())
              .debug("authenticateApplication:success:" + application.getId());

          AuthenticationResponse response = new AuthenticationResponse();
          response.setAuthenticatedAccount(applicationAccount);
          response.setAuthenticatedApplication(application);
          return response;
        } else {
          Logger.of(this.getClass()).error("authenticateApplication: invalid password");
          return null;
        }
      }
    }
  }

  /**
   * Register session within application
   *
   * @param authenticationResponse
   * @param remoteIP
   * @return
   */
  @Override
  public TokenSet registerSession(
      String requestId, AuthenticationResponse authenticationResponse, String remoteIP) {

    Logger.of(this.getClass())
        .debug(
            "registerSession:applicationId: "
                + authenticationResponse.getAuthenticatedApplication().getId());
    Logger.of(this.getClass())
        .debug(
            "registerSession:accountId: "
                + authenticationResponse.getAuthenticatedAccount().getId());

    // delete all services
    sessionService.deleteAllExpired(
        requestId, authenticationResponse.getAuthenticatedApplication().getId().toString());

    // generate a session id
    String generatedSessionId = new ObjectId().toString();

    // generate JWT token
    TokenSet jwtTokenSet =
        tokenService.generateTokenSet(
            authenticationResponse.getAuthenticatedAccount().getId().toString(),
            authenticationResponse.getAuthenticatedApplication().getId().toString(),
            generatedSessionId,
            remoteIP);

    // create session record as well
    Session session = new Session();
    session.setId(generatedSessionId);
    session.setApplicationId(
        authenticationResponse.getAuthenticatedApplication().getId().toString());
    session.setAccountId(authenticationResponse.getAuthenticatedAccount().getId().toString());
    session.setCreatedAt(utilsService.getTimeStamp());
    session.setExpiryDate(jwtTokenSet.getAutheticationExpiresAt());
    session.updateAudit(coreService.getSystemApplicationStamp());
    sessionService.saveSession(requestId, session);

    return jwtTokenSet;
  }

  /**
   * Unregister session, log application out, delete session record from DB and cache
   *
   * @param session
   */
  @Override
  public void unregisterSession(String requestId, Session session) {

    sessionService.deleteSession(requestId, session);
  }
}
