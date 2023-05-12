package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestBankCardSendPin {
  @Required(message = ErrorMessage.ERROR_SENDPIN_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_SENDPIN_CARDID_INVALID)
  private String cardId;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }
}
