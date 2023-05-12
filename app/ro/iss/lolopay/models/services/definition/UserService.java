package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;

import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.UserImplementation;

@ImplementedBy(UserImplementation.class)
public interface UserService {
  public User getUser(String requestId, Account account, String userId);

  public User getUserByProviderId(String requestId, Account account, String providerId);

  public void saveUser(String requestId, Account account, User user);

  public void deleteUser(String requestId, Account account, String userId);
}
