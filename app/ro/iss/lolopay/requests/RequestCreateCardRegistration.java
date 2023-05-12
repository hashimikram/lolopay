package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCardType;
import ro.iss.lolopay.validators.IsCurrencyISO;

public class RequestCreateCardRegistration {
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATECARDREGISTRATION_TAG_INVALID)
  private String tag;

  @Required(message = ErrorMessage.ERROR_CREATECARDREGISTRATION_USERID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATECARDREGISTRATION_USERID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATECARDREGISTRATION_CURRENCY_REQUIRED)
  @ValidateWith(
      value = IsCurrencyISO.class,
      message = ErrorMessage.ERROR_CREATECARDREGISTRATION_CURRENCY_INVALID)
  private String currency;

  @ValidateWith(
      value = IsCardType.class,
      message = ErrorMessage.ERROR_CREATECARDREGISTRATION_CARDTYPE_INVALID)
  private String cardType;

  private String apiVersion = "api6";

  /** @return the tag */
  public String getTag() {

    return tag;
  }

  /** @param tag the tag to set */
  public void setTag(String tag) {

    this.tag = tag;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  /** @return the cardType */
  public String getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(String cardType) {

    this.cardType = cardType;
  }

  /** @return the apiVersion */
  public String getApiVersion() {

    return apiVersion;
  }

  /** @param apiVersion the apiVersion to set */
  public void setApiVersion(String apiVersion) {

    this.apiVersion = apiVersion;
  }
}
