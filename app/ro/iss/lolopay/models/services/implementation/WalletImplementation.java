package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class WalletImplementation implements WalletService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public WalletImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public void deleteWallet(String requestId, Account account, String userId) {
    Datastore datastore = databaseService.getConnection(account.getId());

    Query<Wallet> query = datastore.createQuery(Wallet.class);
    query.criteria("userId").equal(userId);

    datastore.delete(query);
  }

  @Override
  public Wallet getWallet(String requestId, Account account, String walletId) {

    // create query for request history
    return (Wallet) databaseService.getRecord(requestId, account, walletId, Wallet.class);
  }

  @Override
  public Wallet getWallet(String requestId, Account account, String userId, CurrencyISO currency) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("userId", userId);
    filters.put("currency", currency);

    // return result
    return (Wallet) databaseService.getRecord(requestId, account, filters, Wallet.class);
  }

  @Override
  public Wallet getWalletByProviderId(String requestId, Account account, String walletProviderId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", walletProviderId);

    // return result
    return (Wallet) databaseService.getRecord(requestId, account, filters, Wallet.class);
  }

  @Override
  public void saveWallet(String requestId, Account account, Wallet wallet) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(wallet);
  }

  @Override
  public void updateWalletBalance(
      String requestId, Account account, Wallet wallet, Integer valueToUpdate) {

    // update balance
    wallet.getBalance().setValue(wallet.getBalance().getValue() + valueToUpdate);

    // save the wallet
    this.saveWallet(requestId, account, wallet);
  }

  @Override
  public void updateWalletBlockedBalance(
      String requestId, Account account, Wallet wallet, Integer valueToUpdate) {

    // update blocked balance
    wallet.getBlockedBalance().setValue(wallet.getBlockedBalance().getValue() + valueToUpdate);

    // save the wallet
    this.saveWallet(requestId, account, wallet);
  }

  @Override
  public PaginatedList getWalletsPerUser(
      String requestId, Account account, String userId, int page, int pageSize) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("userId", userId);

    // return result
    return databaseService.getRecords(
        requestId, account, filters, QueryFieldOperator.AND, page, pageSize, Wallet.class);
  }
}
