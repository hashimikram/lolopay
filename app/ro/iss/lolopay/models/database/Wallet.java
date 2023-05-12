package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.WalletType;

@Entity(value = "wallets", noClassnameStored = true)
public class Wallet extends TableCollection {
  /** Developer custom tag */
  private String customTag;

  /** A user's ID */
  @Indexed private String userId;

  /** Wallet name or description */
  private String description;

  /** Wallet type */
  private WalletType type;

  /** Wallet available amount */
  private Amount balance;

  /** Wallet blocked amount */
  @JsonIgnore private Amount blockedBalance;

  /** Wallet base currency */
  private CurrencyISO currency;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId;

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the description */
  public String getDescription() {

    return description;
  }

  /** @param description the description to set */
  public void setDescription(String description) {

    this.description = description;
  }

  /** @return the type */
  public WalletType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(WalletType type) {

    this.type = type;
  }

  /** @return the balance */
  public Amount getBalance() {

    return balance;
  }

  /** @param balance the balance to set */
  public void setBalance(Amount balance) {

    this.balance = balance;
  }

  /** @return the blockedBalance */
  public Amount getBlockedBalance() {

    return blockedBalance;
  }

  /** @param blockedBalance the blockedBalance to set */
  public void setBlockedBalance(Amount blockedBalance) {

    this.blockedBalance = blockedBalance;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }
}
