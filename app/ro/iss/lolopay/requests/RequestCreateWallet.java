/** */
package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCurrencyISO;

public class RequestCreateWallet extends RestRequest {

  @Required(message = ErrorMessage.ERROR_CREATEWALLET_USER_ID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEWALLET_USER_ID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATEWALLET_DESCRIPTION_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEWALLET_DESCRIPTION_INVALID)
  private String description;

  @Required(message = ErrorMessage.ERROR_CREATEWALLET_CURRENCY_REQUIRED)
  @ValidateWith(
      value = IsCurrencyISO.class,
      message = ErrorMessage.ERROR_CREATEWALLET_CURRENCY_INVALID)
  private String currency;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEWALLET_TAG_MAXLENGTH)
  private String customTag;

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

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }
}
