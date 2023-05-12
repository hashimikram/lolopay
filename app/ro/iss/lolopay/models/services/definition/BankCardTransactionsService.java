package ro.iss.lolopay.models.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.BankCardTransactionsImplementation;

@ImplementedBy(BankCardTransactionsImplementation.class)
public interface BankCardTransactionsService {
  public List<BankCardTransaction> getBankCardWalletTransaction(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      long startDate,
      long endDate,
      List<BankCardTransaction> returnBankCardTransactions);

  public TransactionDate getDatesForProvider(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      long startDate,
      long endDate);

  public void saveTransactionsWithOffset(
      String requestId,
      Account account,
      List<BankCardTransaction> bankCardTransactions,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      TransactionDate transactionDate);
}
