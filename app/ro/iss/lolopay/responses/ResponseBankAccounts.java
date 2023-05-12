package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.BankAccount;

public class ResponseBankAccounts extends RestResponseBody {
  private List<BankAccount> bankAccounts;

  /** @return the bankAccounts */
  public List<BankAccount> getBankAccounts() {

    return bankAccounts;
  }

  /** @param bankAccounts the bankAccounts to set */
  public void setBankAccounts(List<BankAccount> bankAccounts) {

    this.bankAccounts = bankAccounts;
  }
}
