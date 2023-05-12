package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.CompanyBankCardWalletService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class CompanyBankCardWalletImplementation implements CompanyBankCardWalletService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public CompanyBankCardWalletImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public CompanyBankCardWallet getCompanyBankCardWallet(
      String requestId, Account account, CurrencyISO currency) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("currency", currency);

    // return result
    return (CompanyBankCardWallet)
        databaseService.getRecord(requestId, account, filters, CompanyBankCardWallet.class);
  }

  @Override
  public void saveCompanyBankCardWallet(
      String requestId, Account account, CompanyBankCardWallet companyBankCardWallet) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(companyBankCardWallet);
  }

  @Override
  public List<CompanyBankCardWallet> getCompanyBankCardWallets(String requestId, Account account) {

    // create query for account settings
    @SuppressWarnings("unchecked")
    List<CompanyBankCardWallet> resultList =
        (List<CompanyBankCardWallet>)
            databaseService.getAllRecords(requestId, account, CompanyBankCardWallet.class);

    return resultList;
  }
}
