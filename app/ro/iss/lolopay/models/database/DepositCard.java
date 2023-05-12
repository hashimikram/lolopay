package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CardValidity;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "depositCards", noClassnameStored = true)
public class DepositCard extends TableCollection {
  /** The expiry date of the card - must be in format MMYY */
  private String expirationDate;

  /** A partially obfuscated version of the credit card number e.g. 497010XXXXXX4414 */
  private String alias;

  /** The type of card e.g. CB_VISA_MASTERCARD, DINERS, MASTERPASS ... */
  private CardType cardType;

  /** The provider of the card */
  private String cardProvider;

  /** The Country of the Address */
  private String country;

  /** Whether the card is active or not */
  private boolean active;

  /** The currency - should be ISO_4217 format */
  private CurrencyISO currency;

  /**
   * Whether the card is valid or not. Once they process (or attempt to process) a payment with the
   * card we are able to indicate if it is "valid" or "invalid". If they didn’t process a payment
   * yet the "Validity" stay at "unknown".
   */
  private CardValidity validity;

  /** User Id at provider */
  private String userProviderId;

  /** User Id in database */
  private String userId;

  /** Deposit card Id at provider */
  private String providerId;

  /** Custom data that you can add to this item */
  private String customTag;

  /** A unique representation of a 16-digits card number e.g. 50a6a8da09654c4cab901814a741f924 */
  private String fingerprint;

  /** @return the expirationDate */
  public String getExpirationDate() {

    return expirationDate;
  }

  /** @param expirationDate the expirationDate to set */
  public void setExpirationDate(String expirationDate) {

    this.expirationDate = expirationDate;
  }

  /** @return the alias */
  public String getAlias() {

    return alias;
  }

  /** @param alias the alias to set */
  public void setAlias(String alias) {

    this.alias = alias;
  }

  /** @return the cardType */
  public CardType getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(CardType cardType) {

    this.cardType = cardType;
  }

  /** @return the cardProvider */
  public String getCardProvider() {

    return cardProvider;
  }

  /** @param cardProvider the cardProvider to set */
  public void setCardProvider(String cardProvider) {

    this.cardProvider = cardProvider;
  }

  /** @return the country */
  public String getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(String country) {

    this.country = country;
  }

  /** @return the active */
  public boolean isActive() {

    return active;
  }

  /** @param active the active to set */
  public void setActive(boolean active) {

    this.active = active;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }

  /** @return the validity */
  public CardValidity getValidity() {

    return validity;
  }

  /** @param validity the validity to set */
  public void setValidity(CardValidity validity) {

    this.validity = validity;
  }

  /** @return the userProviderId */
  public String getUserProviderId() {

    return userProviderId;
  }

  /** @param userProviderId the userProviderId to set */
  public void setUserProviderId(String userProviderId) {

    this.userProviderId = userProviderId;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the fingerprint */
  public String getFingerprint() {

    return fingerprint;
  }

  /** @param fingerprint the fingerprint to set */
  public void setFingerprint(String fingerprint) {

    this.fingerprint = fingerprint;
  }
}
