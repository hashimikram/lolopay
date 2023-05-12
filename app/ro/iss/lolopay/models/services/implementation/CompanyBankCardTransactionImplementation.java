package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.database.CompanyBankCardTransaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.CompanyBankCardTransactionService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class CompanyBankCardTransactionImplementation implements CompanyBankCardTransactionService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public CompanyBankCardTransactionImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public CompanyBankCardTransaction getCompanyBankCardTransaction(
      String requestId, Account account, String companyBankCardTransactionId) {

    // create query for request history
    return (CompanyBankCardTransaction)
        databaseService.getRecord(
            requestId, account, companyBankCardTransactionId, CompanyBankCardTransaction.class);
  }

  @Override
  public void saveCompanyBankCardTransaction(
      String requestId, Account account, CompanyBankCardTransaction companyBankCardTransaction) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(companyBankCardTransaction);
  }

  @Override
  public PaginatedList getCompanyBankCardTransactionPerCompanyBankCardWallet(
      String requestId, Account account, String companyBankCardWalletId, int page, int pageSize) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("companyBankCardWalletId", companyBankCardWalletId);

    // return result
    return databaseService.getRecords(
        requestId,
        account,
        filters,
        QueryFieldOperator.AND,
        page,
        pageSize,
        CompanyBankCardTransaction.class);
  }

  @Override
  public CompanyBankCardTransaction getCompanyBankCardTransactionByTransactionId(
      String requestId, Account account, String transactionId) {

    // create filters for query
    Map<String, String> filters = new HashMap<String, String>();
    filters.put("transactionId", transactionId);

    // create query for CompanyBankCardTransaction and return object
    return (CompanyBankCardTransaction)
        databaseService.getRecord(requestId, account, filters, CompanyBankCardTransaction.class);
  }
}
