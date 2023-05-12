package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.DepositAccountType;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccountUS extends BankAccount {
  /** Account number. */
  private String accountNumber;

  /** The ABA of the bank account. Must be numbers only, and 9 digits long - US GROUP */
  private String aba;

  /** The type of US account - US GROUP */
  private DepositAccountType depositAccountType;

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }

  /** @return the aba */
  public String getAba() {

    return aba;
  }

  /** @param aba the aba to set */
  public void setAba(String aba) {

    this.aba = aba;
  }

  /** @return the depositAccountType */
  public DepositAccountType getDepositAccountType() {

    return depositAccountType;
  }

  /** @param depositAccountType the depositAccountType to set */
  public void setDepositAccountType(DepositAccountType depositAccountType) {

    this.depositAccountType = depositAccountType;
  }
}
