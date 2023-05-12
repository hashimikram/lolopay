package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.CountryISO;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccountOTHER extends BankAccount {
  /** The Country of the Address - OTHER GROUP */
  private CountryISO country;

  /** Valid BIC format. */
  private String bic;

  /** Account number. */
  private String accountNumber;

  /** @return the country */
  public CountryISO getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(CountryISO country) {

    this.country = country;
  }

  /** @return the bic */
  public String getBic() {

    return bic;
  }

  /** @param bic the bic to set */
  public void setBic(String bic) {

    this.bic = bic;
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
