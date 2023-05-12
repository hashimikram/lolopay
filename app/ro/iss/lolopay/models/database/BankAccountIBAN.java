package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccountIBAN extends BankAccount {
  /** Bank Account IBAN, specific for EU mostly - IBAN GROUP */
  private String iban;

  /** Bank Account BIC */
  private String bic;

  /** @return the iban */
  public String getIban() {

    return iban;
  }

  /** @param iban the iban to set */
  public void setIban(String iban) {

    this.iban = iban;
  }

  /** @return the bic */
  public String getBic() {

    return bic;
  }

  /** @param bic the bic to set */
  public void setBic(String bic) {

    this.bic = bic;
  }
}
