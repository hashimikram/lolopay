package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.database.CompanyBankCardTransaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.CompanyBankCardTransactionImplementation;

@ImplementedBy(CompanyBankCardTransactionImplementation.class)
public interface CompanyBankCardTransactionService {
  public CompanyBankCardTransaction getCompanyBankCardTransaction(
      String requestId, Account account, String companyBankCardTransactionId);

  public CompanyBankCardTransaction getCompanyBankCardTransactionByTransactionId(
      String requestId, Account account, String transactionId);

  public void saveCompanyBankCardTransaction(
      String requestId, Account account, CompanyBankCardTransaction companyBankCardTransaction);

  public PaginatedList getCompanyBankCardTransactionPerCompanyBankCardWallet(
      String requestId, Account account, String companyBankCardWalletId, int page, int pageSize);
}
