package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankCardWalletService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class BankCardWalletImplementation implements BankCardWalletService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public BankCardWalletImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public BankCardWallet getBankCardWallet(
      String requestId, Account account, String bankCardId, CurrencyISO currency) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("cardId", bankCardId);
    filters.put("currency", currency);

    // return result
    return (BankCardWallet)
        databaseService.getRecord(requestId, account, filters, BankCardWallet.class);
  }

  @Override
  public void saveBankCardWallet(String requestId, Account account, BankCardWallet bankCardWallet) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(bankCardWallet);
  }

  @Override
  public PaginatedList getBankCardWallets(String requestId, Account account, String bankCardId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("cardId", bankCardId);

    // return result
    return databaseService.getRecords(
        requestId, account, filters, QueryFieldOperator.AND, -1, -1, BankCardWallet.class);
  }
}
