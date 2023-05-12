package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.WalletImplementation;

@ImplementedBy(WalletImplementation.class)
public interface WalletService {
  public Wallet getWallet(String requestId, Account account, String walletId);

  public Wallet getWalletByProviderId(String requestId, Account account, String walletProviderId);

  public Wallet getWallet(String requestId, Account account, String userId, CurrencyISO currency);

  public PaginatedList getWalletsPerUser(
      String requestId, Account account, String userId, int page, int pageSize);

  public void saveWallet(String requestId, Account account, Wallet wallet);

  public void updateWalletBalance(
      String requestId, Account account, Wallet wallet, Integer valueToUpdate);

  public void updateWalletBlockedBalance(
      String requestId, Account account, Wallet wallet, Integer valueToUpdate);

  public void deleteWallet(String requestId, Account account, String userId);
}
