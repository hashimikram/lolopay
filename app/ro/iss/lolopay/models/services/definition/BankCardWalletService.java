package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.BankCardWalletImplementation;

@ImplementedBy(BankCardWalletImplementation.class)
public interface BankCardWalletService {
  public PaginatedList getBankCardWallets(String requestId, Account account, String bankCardId);

  public BankCardWallet getBankCardWallet(
      String requestId, Account account, String bankCardId, CurrencyISO currency);

  public void saveBankCardWallet(String requestId, Account account, BankCardWallet bankCardWallet);
}
