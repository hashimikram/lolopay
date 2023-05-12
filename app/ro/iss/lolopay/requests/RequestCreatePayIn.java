package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCardType;
import ro.iss.lolopay.validators.IsCultureCode;
import ro.iss.lolopay.validators.IsFeeModel;
import ro.iss.lolopay.validators.IsSecureMode;
import ro.iss.lolopay.validators.IsURL;

public class RequestCreatePayIn {
  /** The ID of the wallet that will be credited */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEPAYIN_CREDITWALLETID_INVALID)
  private String creditedWalletId;

  /** Amount of money being credited (The currency - should be ISO_4217 format) */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_DEBITFUNDS_REQUIRED)
  @Valid
  private RequestAmount amount;

  /** Amount of money being charged for deposit (The currency - should be ISO_4217 format) */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_FEES_REQUIRED)
  @Valid
  private RequestAmount fees;

  /** The way fees are applied */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_REQUIRED)
  @ValidateWith(value = IsFeeModel.class, message = ErrorMessage.ERROR_CREATEPAYIN_FEEMODEL_INVALID)
  private String feeModel;

  /** The URL to redirect to after payment (whether successful or not) */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_MAXLENGTH)
  @ValidateWith(value = IsURL.class, message = ErrorMessage.ERROR_CREATEPAYIN_RETURNURL_INVALID)
  private String returnURL;

  /** The type of card */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_REQUIRED)
  @ValidateWith(value = IsCardType.class, message = ErrorMessage.ERROR_CREATEPAYIN_CARDTYPE_INVALID)
  private String cardType;

  /**
   * The SecureMode corresponds to '3D secure' for CB Visa and MasterCard. This field lets you
   * activate it manually. The field lets you activate it automatically with "DEFAULT" (Secured Mode
   * will be activated from €50 or when MANGOPAY detects there is a higher risk ), "FORCE" (if you
   * wish to specifically force the secured mode).
   */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_REQUIRED)
  @ValidateWith(
      value = IsSecureMode.class,
      message = ErrorMessage.ERROR_CREATEPAYIN_SECUREMODE_INVALID)
  private String secureMode;

  /** The language to use for the payment page - needs to be the ISO code of the language */
  @Required(message = ErrorMessage.ERROR_CREATEPAYIN_CULTURE_REQUIRED)
  @ValidateWith(
      value = IsCultureCode.class,
      message = ErrorMessage.ERROR_CREATEPAYIN_CULTURE_INVALID)
  private String culture;

  /**
   * A URL to an SSL page to allow you to customise the payment page. Must be in the format:
   * array("PAYLINE"=>"https://...") and meet all the specifications listed here:
   * https://docs.mangopay.com/guide/customising-the-design. Note that only a template for Payline
   * is currently available
   */
  @ValidateWith(value = IsURL.class, message = ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_INVALID)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEPAYIN_TEMPLATEURL_MAXLENGTH)
  private String templateURL;

  /**
   * A custom description to appear on the user's bank statement. It can be up to 10 characters
   * long, and can only include alphanumeric characters or spaces. See thsi link
   * https://docs.mangopay.com/guide/customising-bank-statement-references for important info and
   * note that this functionality is in private beta and not available for all clients.
   */
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_ALPHANUMERIC,
      message = ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_INVALID)
  @MaxLength(value = 10, message = ErrorMessage.ERROR_CREATEPAYIN_STATEMENTDESC_MAXLENGTH)
  private String statementDescriptor;

  /** Custom data that you can add to this item */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEPAYIN_TAG_MAXLENGTH)
  private String customTag;

  /** @return the creditedWalletId */
  public String getCreditedWalletId() {

    return creditedWalletId;
  }

  /** @param creditedWalletId the creditedWalletId to set */
  public void setCreditedWalletId(String creditedWalletId) {

    this.creditedWalletId = creditedWalletId;
  }

  /** @return the amount */
  public RequestAmount getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(RequestAmount amount) {

    this.amount = amount;
  }

  /** @return the fees */
  public RequestAmount getFees() {

    return fees;
  }

  /** @param fees the fees to set */
  public void setFees(RequestAmount fees) {

    this.fees = fees;
  }

  /** @return the feeModel */
  public String getFeeModel() {

    return feeModel;
  }

  /** @param feeModel the feeModel to set */
  public void setFeeModel(String feeModel) {

    this.feeModel = feeModel;
  }

  /** @return the returnURL */
  public String getReturnURL() {

    return returnURL;
  }

  /** @param returnURL the returnURL to set */
  public void setReturnURL(String returnURL) {

    this.returnURL = returnURL;
  }

  /** @return the cardType */
  public String getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(String cardType) {

    this.cardType = cardType;
  }

  /** @return the secureMode */
  public String getSecureMode() {

    return secureMode;
  }

  /** @param secureMode the secureMode to set */
  public void setSecureMode(String secureMode) {

    this.secureMode = secureMode;
  }

  /** @return the culture */
  public String getCulture() {

    return culture;
  }

  /** @param culture the culture to set */
  public void setCulture(String culture) {

    this.culture = culture;
  }

  /** @return the templateURL */
  public String getTemplateURL() {

    return templateURL;
  }

  /** @param templateURL the templateURL to set */
  public void setTemplateURL(String templateURL) {

    this.templateURL = templateURL;
  }

  /** @return the statementDescriptor */
  public String getStatementDescriptor() {

    return statementDescriptor;
  }

  /** @param statementDescriptor the statementDescriptor to set */
  public void setStatementDescriptor(String statementDescriptor) {

    this.statementDescriptor = statementDescriptor;
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
