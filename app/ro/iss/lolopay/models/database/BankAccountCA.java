package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccountCA extends BankAccount {
  /**
   * The name of the bank where the account is held. Must be letters or numbers only and maximum 50
   * characters long. - CA GROUP
   */
  private String bankName;

  /**
   * The institution number of the bank account. Must be numbers only, and 3 or 4 digits long - CA
   * GROUP
   */
  private String institutionNumber;

  /**
   * The branch code of the bank where the bank account. Must be numbers only, and 5 digits long -
   * CA GROUP
   */
  private String branchCode;

  /** Account number. */
  private String accountNumber;

  /** @return the bankName */
  public String getBankName() {

    return bankName;
  }

  /** @param bankName the bankName to set */
  public void setBankName(String bankName) {

    this.bankName = bankName;
  }

  /** @return the institutionNumber */
  public String getInstitutionNumber() {

    return institutionNumber;
  }

  /** @param institutionNumber the institutionNumber to set */
  public void setInstitutionNumber(String institutionNumber) {

    this.institutionNumber = institutionNumber;
  }

  /** @return the branchCode */
  public String getBranchCode() {

    return branchCode;
  }

  /** @param branchCode the branchCode to set */
  public void setBranchCode(String branchCode) {

    this.branchCode = branchCode;
  }

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }
}
