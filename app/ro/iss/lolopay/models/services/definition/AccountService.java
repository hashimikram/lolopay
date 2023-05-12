package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.AccountImplementation;

@ImplementedBy(AccountImplementation.class)
public interface AccountService {
  public Account getAccountByEmail(String requestId, String accountEmail);

  public Account getAccountByAccountId(String requestId, String accountId);

  public Account getAccount(String requestId, String accountId);

  public void saveAccount(String requestId, Account account);
}
