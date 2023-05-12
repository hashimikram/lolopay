package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestReplaceCard extends RequestClient {
  @Required(message = ErrorMessage.ERROR_REPLACECARD_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_REPLACECARD_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_REPLACECARD_REASON_REQUIRED)
  // TODO Remove Max Length (already in Pattern)
  @MaxLength(value = 250, message = ErrorMessage.ERROR_REPLACECARD_REASON_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_REPLACE_CARD_REASON,
      message = ErrorMessage.ERROR_REPLACECARD_REASON_INVALID)
  private String reason;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the reason */
  public String getReason() {

    return reason;
  }

  /** @param reason the reason to set */
  public void setReason(String reason) {

    this.reason = reason;
  }
}
