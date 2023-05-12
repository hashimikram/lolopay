package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestRefundTransfer extends RestRequest {
  /** The ID of the transaction to be refunded */
  @Required(message = ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_REFUNDTRANSACTION_TRANSACTIONID_INVALID)
  private String transactionId;

  /** Custom data that you can add to this item */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_REFUNDTRANSACTION_TAG_MAXLENGTH)
  private String customTag;

  /** @return the transactionId */
  public String getTransactionId() {

    return transactionId;
  }

  /** @param transactionId the transactionId to set */
  public void setTransactionId(String transactionId) {

    this.transactionId = transactionId;
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
