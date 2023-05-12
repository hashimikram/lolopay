/** */
package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.BankAccount;

public class ResponseBankAccount extends RestResponseBody {

  private BankAccount bankAccount;

  /** @return the bankAccount */
  public BankAccount getBankAccount() {

    return bankAccount;
  }

  /** @param bankAccount the bankAccount to set */
  public void setBankAccount(BankAccount bankAccount) {

    this.bankAccount = bankAccount;
  }
}
