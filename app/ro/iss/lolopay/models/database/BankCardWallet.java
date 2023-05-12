package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "bankCardWallets", noClassnameStored = true)
public class BankCardWallet extends TableCollection {
  /** A user's ID */
  @Indexed private String cardId;

  /** Wallet amount */
  private Amount availableBalance;

  /** Wallet ledger amount */
  private Amount ledgerBalance;

  /** Wallet base currency */
  private CurrencyISO currency;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the availableBalance */
  public Amount getAvailableBalance() {

    return availableBalance;
  }

  /** @param availableBalance the availableBalance to set */
  public void setAvailableBalance(Amount availableBalance) {

    this.availableBalance = availableBalance;
  }

  /** @return the ledgerBalance */
  public Amount getLedgerBalance() {

    return ledgerBalance;
  }

  /** @param ledgerBalance the ledgerBalance to set */
  public void setLedgerBalance(Amount ledgerBalance) {

    this.ledgerBalance = ledgerBalance;
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
