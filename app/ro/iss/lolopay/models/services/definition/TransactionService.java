package ro.iss.lolopay.models.services.definition;

import java.util.Map;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.TransactionImplementation;

@ImplementedBy(TransactionImplementation.class)
public interface TransactionService {
  public PaginatedList getTransactionsPerWallet(
      String requestId, Account account, String walletId, int page, int pageSize);

  public PaginatedList getTransactionsPerWalletNoFee(
      String requestId, Account account, String walletId, int page, int pageSize, String sort);

  public void updateTransaction(
      String requestId,
      Account account,
      Application application,
      Map<String, Object> transactionFilters,
      Map<String, Object> transactionFields);

  public Transaction getTransaction(String requestId, Account account, String transactionId);
}
