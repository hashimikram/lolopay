package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsFeeModel;

public class RequestCreateTransfer extends RestRequest {
  /** The ID of the wallet that was debited */
  @Required(message = ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATETRANSFER_DEBITWALLETID_INVALID)
  private String debitedWalletId;

  /** The ID of the wallet where money will be credited */
  @Required(message = ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATETRANSFER_CREDITWALLETID_INVALID)
  private String creditedWalletId;

  /** Amount of money being debited (The currency - should be ISO_4217 format) */
  @Required(message = ErrorMessage.ERROR_CREATETRANSFER_DEBITFUNDS_REQUIRED)
  @Valid
  private RequestAmount amount;

  /** Amount of money being charged for transfer (The currency - should be ISO_4217 format) */
  @Required(message = ErrorMessage.ERROR_CREATETRANSFER_FEES_REQUIRED)
  @Valid
  private RequestAmount fees;

  /** The way fees are applied */
  @Required(message = ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_REQUIRED)
  @ValidateWith(
      value = IsFeeModel.class,
      message = ErrorMessage.ERROR_CREATETRANSFER_FEEMODEL_INVALID)
  private String feeModel;

  /** Custom data that you can add to this item */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATETRANSFER_TAG_MAXLENGTH)
  private String customTag;

  /** @return the debitedWalletId */
  public String getDebitedWalletId() {

    return debitedWalletId;
  }

  /** @param debitedWalletId the debitedWalletId to set */
  public void setDebitedWalletId(String debitedWalletId) {

    this.debitedWalletId = debitedWalletId;
  }

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

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }
}
