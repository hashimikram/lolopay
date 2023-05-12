package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "companyBankCardWallets", noClassnameStored = true)
public class CompanyBankCardWallet extends TableCollection {
  /** Company wallet balance */
  private Amount balance;

  /** Company wallet currency */
  private CurrencyISO currency;

  /** @return the balance */
  public Amount getBalance() {

    return balance;
  }

  /** @param balance the balance to set */
  public void setBalance(Amount balance) {

    this.balance = balance;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }
}
