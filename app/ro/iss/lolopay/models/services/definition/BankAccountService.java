/** */
package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.BankAccountImplementation;

@ImplementedBy(BankAccountImplementation.class)
public interface BankAccountService {
  public BankAccount getBankAccount(
      String requestId, Account account, String userId, String bankAccountId);

  public void saveBankAccount(String requestId, Account account, BankAccount bankAccount);

  public PaginatedList getBankAccountsPerUser(
      String requestId, Account account, String userId, int page, int pageSize);
}
