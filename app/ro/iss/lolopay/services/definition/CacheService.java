package ro.iss.lolopay.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.services.implementation.CacheImplementation;

@ImplementedBy(CacheImplementation.class)
public interface CacheService {
  public void addAccountToCache(String requestId, Account account);

  public Account getAccountByEmail(String requestId, String accountEmail);

  public Account getAccountByAccountId(String requestId, String accountId);

  public Account getAccount(String requestId, String accountId);

  public void addSessionToCache(String requestId, Session session);

  public void removeSessionFromCache(String requestId, Session session);

  public Session getSession(String requestId, String sessionId);

  public void addApplicationToCache(String requestId, Account account, Application application);

  public Application getApplication(String requestId, Account account, String applicationId);

  public Application getApplicationByApplicationId(
      String requestId, Account account, String readableApplicationId);

  public List<Application> getApplicationsForAccount(
      String requestId, Account account, int limitResults);

  public AccountSettings getAccountSettings(String requestId, Account account);

  public Wallet getAccountWallet(String requestId, Account account, CurrencyISO currencyISO);

  public String getErrorCode(String requestId, String errorKey);

  public List<ApplicationError> getErrorCodes(String requestId);
}
