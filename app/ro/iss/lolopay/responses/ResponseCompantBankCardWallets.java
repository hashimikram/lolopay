package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;

public class ResponseCompantBankCardWallets extends RestResponseBody {
  private List<CompanyBankCardWallet> companyBankCardWallets;

  /** @return the companyBankCardWallets */
  public List<CompanyBankCardWallet> getCompanyBankCardWallets() {

    return companyBankCardWallets;
  }

  /** @param companyBankCardWallets the companyBankCardWallets to set */
  public void setCompanyBankCardWallets(List<CompanyBankCardWallet> companyBankCardWallets) {

    this.companyBankCardWallets = companyBankCardWallets;
  }
}
