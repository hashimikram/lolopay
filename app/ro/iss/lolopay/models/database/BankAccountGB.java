package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccountGB extends BankAccount {
  /** Account number. */
  private String accountNumber;

  /** The sort code of the bank account. Must be numbers only, and 6 digits long - GB GROUP */
  private String sortCode;

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }

  /** @return the sortCode */
  public String getSortCode() {

    return sortCode;
  }

  /** @param sortCode the sortCode to set */
  public void setSortCode(String sortCode) {

    this.sortCode = sortCode;
  }
}
