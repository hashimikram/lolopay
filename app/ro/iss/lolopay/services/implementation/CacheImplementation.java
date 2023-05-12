package ro.iss.lolopay.services.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.typesafe.config.ConfigFactory;
import play.cache.AsyncCacheApi;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.services.definition.CacheService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.EmailService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class CacheImplementation implements CacheService {
  @Inject private AsyncCacheApi cache;

  @Inject private DatabaseService databaseService;

  @Inject private LogService logService;

  @Inject private EmailService emailService;

  @Inject UtilsService utilsService;

  @Override
  @SuppressWarnings("unchecked")
  public void addAccountToCache(String requestId, Account account) {

    List<Account> listAccounts;

    try {
      // get accounts from cache
      listAccounts =
          cache
              .getOrElseUpdate(
                  "db.accounts",
                  () -> getAccountsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbAccounts.cacheTime"))
              .toCompletableFuture()
              .get();

    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get accounts manually
      listAccounts = (List<Account>) getAccountsFromDatabase(requestId);
    }

    // if the account was not already updated in cache
    if (!listAccounts.contains(account)) {
      // add account in cache
      listAccounts.add(account);

      // update cache
      cache.set(
          "db.accounts",
          listAccounts,
          ConfigFactory.load().getInt("application.dbAccounts.cacheTime"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Account getAccountByEmail(String requestId, String accountEmail) {

    List<Account> listAccounts;

    try {
      // get accounts from cache
      listAccounts =
          cache
              .getOrElseUpdate(
                  "db.accounts",
                  () -> getAccountsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbAccounts.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get accounts manually
      listAccounts = (List<Account>) getAccountsFromDatabase(requestId);
    }

    // for each iterate account
    for (Account account : listAccounts) {
      // if account email matches
      if ((account.getAccountEmail() != null) && (account.getAccountEmail().equals(accountEmail))) {
        return account;
      }
    }

    // account not found or error
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Account getAccountByAccountId(String requestId, String accountId) {

    List<Account> listAccounts;

    try {
      // get accounts from cache
      listAccounts =
          cache
              .getOrElseUpdate(
                  "db.accounts",
                  () -> getAccountsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbAccounts.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get accounts manually
      listAccounts = (List<Account>) getAccountsFromDatabase(requestId);
    }

    // for each iterate account
    for (Account account : listAccounts) {
      // if account email matches
      if ((account.getAccountId() != null) && (account.getAccountId().equals(accountId))) {
        return account;
      }
    }

    // account not found or error
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Account getAccount(String requestId, String accountId) {

    List<Account> listAccounts;

    try {
      // get accounts from cache
      listAccounts =
          cache
              .getOrElseUpdate(
                  "db.accounts",
                  () -> getAccountsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbAccounts.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get accounts manually
      listAccounts = (List<Account>) getAccountsFromDatabase(requestId);
    }

    // for each iterate account
    for (Account account : listAccounts) {
      // if account id matches
      if (account.getId().toString().equals(accountId)) {
        return account;
      }
    }

    // account not found
    return null;
  }

  /**
   * Add new application to cache to avoid application cache reload
   *
   * @param accountId
   * @param application
   */
  @Override
  @SuppressWarnings("unchecked")
  public void addApplicationToCache(String requestId, Account account, Application application) {

    List<Application> listApplications;

    try {
      // get applications from cache
      listApplications =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".applications",
                  () -> getAccountApplicationsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbApplications.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listApplications = (List<Application>) getAccountApplicationsFromDatabase(requestId, account);
    }

    // if the application was not already updated in cache
    if (!listApplications.contains(application)) {
      // add application in cache
      listApplications.add(application);

      // update cache
      cache.set(
          "db." + account.getId().toString() + ".applications",
          listApplications,
          ConfigFactory.load().getInt("application.dbApplications.cacheTime"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Application getApplication(String requestId, Account account, String applicationId) {

    List<Application> listApplications;

    try {
      // get applications from cache
      listApplications =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".applications",
                  () -> getAccountApplicationsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbApplications.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listApplications = (List<Application>) getAccountApplicationsFromDatabase(requestId, account);
    }

    // for each iterated application
    for (Application application : listApplications) {
      // if application id matches
      if (application.getId().toString().equals(applicationId)) {
        return application;
      }
    }

    // application not found
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Application getApplicationByApplicationId(
      String requestId, Account account, String readableApplicationId) {

    List<Application> listApplications;

    try {
      // get applications from cache
      listApplications =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".applications",
                  () -> getAccountApplicationsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbApplications.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listApplications = (List<Application>) getAccountApplicationsFromDatabase(requestId, account);
    }

    // for each iterated application
    for (Application application : listApplications) {
      // if application id matches
      if (application.getApplicationId().toString().equals(readableApplicationId)) {
        return application;
      }
    }

    // application not found
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Application> getApplicationsForAccount(
      String requestId, Account account, int limitResults) {

    List<Application> listApplications;

    try {
      // get applications from cache
      listApplications =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".applications",
                  () -> getAccountApplicationsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbApplications.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listApplications = (List<Application>) getAccountApplicationsFromDatabase(requestId, account);
    }

    // check if it is possible to cut off this list
    if (limitResults < listApplications.size()) {
      // extract sublist from all account applications
      return listApplications.subList(0, limitResults);
    } else {
      // return entire list
      return listApplications;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addSessionToCache(String requestId, Session session) {

    List<Session> sessions;

    try {
      // get sessions from cache
      sessions =
          cache
              .getOrElseUpdate(
                  "db.sessions",
                  () -> getSessionsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbSessions.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      sessions = (List<Session>) getSessionsFromDatabase(requestId);
    }

    // if the session was not already updated in cache
    if (!sessions.contains(session)) {
      // add session in cache
      sessions.add(session);

      // update cache
      cache.set(
          "db.sessions", sessions, ConfigFactory.load().getInt("application.dbSessions.cacheTime"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void removeSessionFromCache(String requestId, Session session) {

    List<Session> sessions;

    try {
      // get sessions from cache
      sessions =
          cache
              .getOrElseUpdate(
                  "db.sessions",
                  () -> getSessionsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbSessions.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      sessions = (List<Session>) getSessionsFromDatabase(requestId);
    }

    // if the session exists in cache
    if (sessions.contains(session)) {
      // remove session from cache
      sessions.remove(session);

      // update cache
      cache.set(
          "db.sessions", sessions, ConfigFactory.load().getInt("application.dbSessions.cacheTime"));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Session getSession(String requestId, String sessionId) {

    List<Session> sessions;

    try {
      // get sessions from cache
      sessions =
          cache
              .getOrElseUpdate(
                  "db.sessions",
                  () -> getSessionsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbSessions.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      sessions = (List<Session>) getSessionsFromDatabase(requestId);
    }

    // for each iterated session
    for (Session session : sessions) {
      // if id matches
      if (session.getId().toString().equals(sessionId)) {
        return session;
      }
    }

    // session not found
    return null;
  }

  @Override
  public AccountSettings getAccountSettings(String requestId, Account account) {

    AccountSettings accountSettings;

    try {
      // get account settings record from cache
      accountSettings =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".settings",
                  () -> getAccountSettingsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbSettings.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      accountSettings = (AccountSettings) getAccountSettingsFromDatabase(requestId, account);
    }

    // return setting record
    return accountSettings;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Wallet getAccountWallet(String requestId, Account account, CurrencyISO currencyISO) {

    List<Wallet> accountWallets;

    try {
      // get account settings record from cache
      accountWallets =
          cache
              .getOrElseUpdate(
                  "db." + account.getId().toString() + ".wallets",
                  () -> getAccountWalletsFromDatabase(requestId, account),
                  ConfigFactory.load().getInt("application.dbAccountWallets.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      accountWallets = (List<Wallet>) getAccountWalletsFromDatabase(requestId, account);
    }

    // iterate each wallet
    for (Wallet wallet : accountWallets) {
      if (wallet.getCurrency().equals(currencyISO)) {
        return wallet;
      }
    }

    // wallet not found
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getErrorCode(String requestId, String errorKey) {

    List<ApplicationError> listErrors;

    try {
      // get errors from cache
      listErrors =
          cache
              .getOrElseUpdate(
                  "db.errors",
                  () -> getApplicationErrorsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbErrors.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listErrors = (List<ApplicationError>) getApplicationErrorsFromDatabase(requestId);
    }

    // for each iterate error
    for (ApplicationError error : listErrors) {
      // if error key matches
      if (error.getErrorKey().equals(errorKey)) {
        return error.getErrorCode();
      }
    }

    // error not found
    logService.error(requestId, "L", "errorKeyNotFound", errorKey);
    return "XXXXXX" + utilsService.getCurrentTimeMiliseconds();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ApplicationError> getErrorCodes(String requestId) {

    List<ApplicationError> listErrors;

    try {
      // get errors from cache
      listErrors =
          cache
              .getOrElseUpdate(
                  "db.errors",
                  () -> getApplicationErrorsFromDatabase(requestId),
                  ConfigFactory.load().getInt("application.dbErrors.cacheTime"))
              .toCompletableFuture()
              .get();
    } catch (InterruptedException | ExecutionException e) {
      // log errors and send email
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Request Id: ");
      sb.append(requestId);
      sb.append(System.lineSeparator());
      sb.append("Error: Cache unwanted error: " + e.getMessage());
      sb.append(System.lineSeparator());
      sb.append("Error stack:");
      sb.append(ExceptionUtils.getStackTrace(e));

      // send an email
      emailService.email(
          ConfigFactory.load().getString("application.noreplyemail"),
          ConfigFactory.load().getString("application.name"),
          ConfigFactory.load().getString("application.devemail"),
          "LoLo Developer",
          ConfigFactory.load().getString("application.environment") + " LoLo cacheError",
          sb.toString());

      // get records from db
      listErrors = (List<ApplicationError>) getApplicationErrorsFromDatabase(requestId);
    }

    // return all errors
    return listErrors;
  }

  /**
   * Retrieve all accounts from database
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<List<Account>> getAccountsFromDatabase(String requestId) {

    // create query for accounts
    List<Account> resultList =
        (List<Account>) databaseService.getMainAllRecords(requestId, Account.class);

    // execute and retrieve query accounts
    return CompletableFuture.completedFuture(resultList);
  }

  /**
   * Retrieve all application groups for one account
   *
   * @param accountId
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<List<Application>> getAccountApplicationsFromDatabase(
      String requestId, Account account) {

    // get all account applications
    List<Application> resultList =
        (List<Application>) databaseService.getAllRecords(requestId, account, Application.class);

    // execute and retrieve query accounts
    return CompletableFuture.completedFuture(resultList);
  }

  /**
   * Retrieve all database sessions
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<List<Session>> getSessionsFromDatabase(String requestId) {

    // get all account sessions
    List<Session> resultList =
        (List<Session>) databaseService.getMainAllRecords(requestId, Session.class);

    // execute and retrieve query accounts
    return CompletableFuture.completedFuture(resultList);
  }

  /**
   * Retrieve account settings
   *
   * @param account
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<AccountSettings> getAccountSettingsFromDatabase(
      String requestId, Account account) {

    // create query for account settings
    List<AccountSettings> resultList =
        (List<AccountSettings>)
            databaseService.getAllRecords(requestId, account, AccountSettings.class);

    // get first one only
    // execute and retrieve query account setting - we must assume that one setting is always
    // available
    return CompletableFuture.completedFuture(resultList.get(0));
  }

  /**
   * Retrieve account settings
   *
   * @param accountId
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<List<Wallet>> getAccountWalletsFromDatabase(
      String requestId, Account account) {

    // get account settings first
    AccountSettings accountSettings = this.getAccountSettings(requestId, account);

    // create a list of DB String Id's
    List<String> walletIds = new ArrayList<String>();

    // iterate account wallets and add them to String Id's list
    for (String stringWalletId : accountSettings.getAccountWalletIds()) {
      walletIds.add(stringWalletId);
    }

    // create DB query to search all wallets in specified list of id's
    List<Wallet> resultList =
        (List<Wallet>) databaseService.getAllRecords(requestId, account, walletIds, Wallet.class);

    // execute and extract wallet list
    return CompletableFuture.completedFuture(resultList);
  }

  /**
   * Retrieve all errors from database
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  private CompletionStage<List<ApplicationError>> getApplicationErrorsFromDatabase(
      String requestId) {

    // create query for all errors
    List<ApplicationError> resultList =
        (List<ApplicationError>)
            databaseService.getMainAllRecords(requestId, ApplicationError.class);

    // execute and retrieve query errors
    return CompletableFuture.completedFuture(resultList);
  }
}
