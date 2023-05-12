package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBIC;
import ro.iss.lolopay.validators.IsIBAN;
import ro.iss.lolopay.validators.IsInteger;

public class RequestBankPayment {
  @Required(message = ErrorMessage.ERROR_BANKPAYMENT_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_BANKPAYMENT_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_REQUIRED)
  // TODO Remove max length (already in Pattern match)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_BENEFICIARY_NAME,
      message = ErrorMessage.ERROR_BANKPAYMENT_BENEFICIARYNAME_INVALID)
  private String beneficiaryName;

  // TODO Should this be required?
  @ValidateWith(value = IsIBAN.class, message = ErrorMessage.ERROR_BANKPAYMENT_CREDITORIBAN_INVALID)
  private String creditorIban;

  // TODO Should this be required?
  // TODO Remove Min and Max Length (already in ValidateWith)
  @MinLength(value = 8, message = ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_MINLENGTH)
  @MaxLength(value = 11, message = ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_MAXLENGTH)
  @ValidateWith(value = IsBIC.class, message = ErrorMessage.ERROR_BANKPAYMENT_CREDITORBIC_INVALID)
  private String creditorBic;

  @Required(message = ErrorMessage.ERROR_BANKPAYMENT_PAYMENTAMOUNT_REQUIRED)
  @ValidateWith(
      value = IsInteger.class,
      message = ErrorMessage.ERROR_BANKPAYMENT_PAYMENTAMOUNT_INVALID)
  private Integer paymentAmount;

  @Required(message = ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_REQUIRED)
  @MaxLength(value = 35, message = ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_ALPHANUMERIC,
      message = ErrorMessage.ERROR_BANKPAYMENT_REFERENCE_INVALID)
  private String reference;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the beneficiaryName */
  public String getBeneficiaryName() {

    return beneficiaryName;
  }

  /** @param beneficiaryName the beneficiaryName to set */
  public void setBeneficiaryName(String beneficiaryName) {

    this.beneficiaryName = beneficiaryName;
  }

  /** @return the creditorIban */
  public String getCreditorIban() {

    return creditorIban;
  }

  /** @param creditorIban the creditorIban to set */
  public void setCreditorIban(String creditorIban) {

    this.creditorIban = creditorIban;
  }

  /** @return the creditorBic */
  public String getCreditorBic() {

    return creditorBic;
  }

  /** @param creditorBic the creditorBic to set */
  public void setCreditorBic(String creditorBic) {

    this.creditorBic = creditorBic;
  }

  /** @return the paymentAmount */
  public Integer getPaymentAmount() {

    return paymentAmount;
  }

  /** @param paymentAmount the paymentAmount to set */
  public void setPaymentAmount(Integer paymentAmount) {

    this.paymentAmount = paymentAmount;
  }

  /** @return the reference */
  public String getReference() {

    return reference;
  }

  /** @param reference the reference to set */
  public void setReference(String reference) {

    this.reference = reference;
  }
}
