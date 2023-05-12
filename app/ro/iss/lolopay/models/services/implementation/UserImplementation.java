package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class UserImplementation implements UserService {
  @Inject private DatabaseService databaseService;

  /** Deletes user record for gdpr reasons */
  @Override
  public void deleteUser(String requestId, Account account, String userId) {
    databaseService.getConnection(account.getId()).delete(User.class, userId);
  }

  /** Retrieve one user based on user Id */
  @Override
  public User getUser(String requestId, Account account, String userId) {

    return (User) databaseService.getRecord(requestId, account, userId, User.class);
  }

  @Override
  public void saveUser(String requestId, Account account, User user) {

    // save user in database
    databaseService.getConnection(account.getId().toString()).save(user);
  }

  @Override
  public User getUserByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (User) databaseService.getRecord(requestId, account, filters, User.class);
  }
}
