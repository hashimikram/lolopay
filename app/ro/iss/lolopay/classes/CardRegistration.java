package ro.iss.lolopay.classes;

import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CurrencyISO;

public class CardRegistration {
  /** Unique identifier. */
  private String id;

  /** Custom data. */
  private String customTag;

  /** Date of creation (UNIX timestamp). */
  private long createdAt;

  /** User Id. */
  private String userProviderId;

  /** Access key. */
  private String accessKey;

  /** Preregistration data. */
  private String preRegistrationData;

  /** Card registration URL. */
  private String cardRegistrationUrl;

  /** Card identifier. */
  private String cardProviderId;

  /** Card registration data. */
  private String registrationData;

  /** Result code. */
  private String resultCode;

  /** Currency. */
  private CurrencyISO currency;

  /** Status. */
  private String status;

  /** Card type. */
  private CardType cardType;

  /** URL where the token will be received */
  private String returnUrl;

  /** @return the returnUrl */
  public String getReturnUrl() {

    return returnUrl;
  }

  /** @param returnUrl the returnUrl to set */
  public void setReturnUrl(String returnUrl) {

    this.returnUrl = returnUrl;
  }

  /** @return the id */
  public String getId() {

    return id;
  }

  /** @param id the id to set */
  public void setId(String id) {

    this.id = id;
  }

  /** @return the tag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param tag the tag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the createdAt */
  public long getCreatedAt() {

    return createdAt;
  }

  /** @param createdAt the createdAt to set */
  public void setCreatedAt(long createdAt) {

    this.createdAt = createdAt;
  }

  /** @return the userId */
  public String getUserProviderId() {

    return userProviderId;
  }

  /** @param userId the userId to set */
  public void setUserProviderId(String userProviderId) {

    this.userProviderId = userProviderId;
  }

  /** @return the accessKey */
  public String getAccessKey() {

    return accessKey;
  }

  /** @param accessKey the accessKey to set */
  public void setAccessKey(String accessKey) {

    this.accessKey = accessKey;
  }

  /** @return the cardRegistrationUrl */
  public String getCardRegistrationUrl() {

    return cardRegistrationUrl;
  }

  /** @param cardRegistrationUrl the cardRegistrationUrl to set */
  public void setCardRegistrationUrl(String cardRegistrationUrl) {

    this.cardRegistrationUrl = cardRegistrationUrl;
  }

  /** @return the cardId */
  public String getCardProviderId() {

    return cardProviderId;
  }

  /** @param cardId the cardProviderId to set */
  public void setCardProviderId(String cardProviderId) {

    this.cardProviderId = cardProviderId;
  }

  /** @return the registrationData */
  public String getRegistrationData() {

    return registrationData;
  }

  /** @param registrationData the registrationData to set */
  public void setRegistrationData(String registrationData) {

    this.registrationData = registrationData;
  }

  /** @return the resultCode */
  public String getResultCode() {

    return resultCode;
  }

  /** @param resultCode the resultCode to set */
  public void setResultCode(String resultCode) {

    this.resultCode = resultCode;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }

  /** @return the status */
  public String getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(String status) {

    this.status = status;
  }

  /** @return the cardType */
  public CardType getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(CardType cardType) {

    this.cardType = cardType;
  }

  /** @return the preRegistrationData */
  public String getPreRegistrationData() {

    return preRegistrationData;
  }

  /** @param preRegistrationData the preRegistrationData to set */
  public void setPreRegistrationData(String preRegistrationData) {

    this.preRegistrationData = preRegistrationData;
  }
}
