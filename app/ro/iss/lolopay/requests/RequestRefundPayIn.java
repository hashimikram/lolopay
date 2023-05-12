package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.Amount;

public class RequestRefundPayIn extends RestRequest {
  /** Custom data that you can add to this item */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEPAYINREFUND_TAG_MAXLENGTH)
  private String customTag;

  /** Amount of money being credited (The currency - should be ISO_4217 format) */
  @Valid private RequestAmount amount;

  /** Amount of money being charged for deposit (The currency - should be ISO_4217 format) */
  @Valid private RequestAmount fees;

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the amount */
  public RequestAmount getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(RequestAmount amount) {

    this.amount = amount;
  }

  /** @param amount the amount to set */
  public void setAmount(Amount amount) {

    this.amount.setCurrency(amount.getCurrency().toString());
    this.amount.setValue(amount.getValue());
  }

  /** @return the fees */
  public RequestAmount getFees() {

    return fees;
  }

  /** @param fees the fees to set */
  public void setFees(RequestAmount fees) {

    this.fees = fees;
  }
}
