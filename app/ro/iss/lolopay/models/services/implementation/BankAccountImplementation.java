/** */
package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankAccountService;
import ro.iss.lolopay.services.definition.DatabaseService;

public class BankAccountImplementation implements BankAccountService {

  @Inject private DatabaseService databaseService;

  /*
   * get bank account based on the user id
   */
  @Override
  public BankAccount getBankAccount(
      String requestId, Account account, String userId, String bankAccountId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("id", bankAccountId);
    filters.put("userId", userId);

    // return result
    return (BankAccount) databaseService.getRecord(requestId, account, filters, BankAccount.class);
  }

  /*
   * set bank account
   */
  @Override
  public void saveBankAccount(String requestId, Account account, BankAccount bankAccount) {

    // save bankAccount in database
    databaseService.getConnection(account.getId().toString()).save(bankAccount);
  }

  @Override
  public PaginatedList getBankAccountsPerUser(
      String requestId, Account account, String userId, int page, int pageSize) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("userId", userId);

    // return result
    return databaseService.getRecords(
        requestId, account, filters, QueryFieldOperator.AND, page, pageSize, BankAccount.class);
  }
}
