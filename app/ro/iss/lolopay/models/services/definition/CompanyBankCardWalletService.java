package ro.iss.lolopay.models.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.CompanyBankCardWalletImplementation;

@ImplementedBy(CompanyBankCardWalletImplementation.class)
public interface CompanyBankCardWalletService {
  public CompanyBankCardWallet getCompanyBankCardWallet(
      String requestId, Account account, CurrencyISO currency);

  public void saveCompanyBankCardWallet(
      String requestId, Account account, CompanyBankCardWallet companyBankCardWallet);

  public List<CompanyBankCardWallet> getCompanyBankCardWallets(String requestId, Account account);
}
