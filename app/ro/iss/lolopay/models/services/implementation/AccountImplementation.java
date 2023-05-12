package ro.iss.lolopay.models.services.implementation;

import javax.inject.Inject;
import javax.inject.Singleton;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.services.definition.CacheService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class AccountImplementation implements AccountService {

  @Inject private CacheService cacheService;

  @Inject private DatabaseService databaseService;

  /**
   * Find an account by e-mail address
   *
   * @param accountEmail
   * @return
   */
  @Override
  public Account getAccountByEmail(String requestId, String accountEmail) {

    return cacheService.getAccountByEmail(requestId, accountEmail);
  }

  @Override
  public Account getAccountByAccountId(String requestId, String accountId) {

    return cacheService.getAccountByAccountId(requestId, accountId);
  }

  /**
   * Find an account by id
   *
   * @param accountEmail
   * @return
   */
  @Override
  public Account getAccount(String requestId, String accountId) {

    return cacheService.getAccount(requestId, accountId);
  }

  @Override
  public void saveAccount(String requestId, Account account) {

    // save user
    databaseService.getMainConnection().save(account);

    // add new created account in cache
    cacheService.addAccountToCache(requestId, account);
  }
}
