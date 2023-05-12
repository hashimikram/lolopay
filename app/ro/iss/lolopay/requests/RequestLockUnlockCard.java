package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBankCardStatus;

public class RequestLockUnlockCard {
  @Required(message = ErrorMessage.ERROR_LOCKUNLOCKCARD_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_LOCKUNLOCKCARD_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_LOCKUNLOCKCARD_OLDSTATUS_REQUIRED)
  @ValidateWith(
      value = IsBankCardStatus.class,
      message = ErrorMessage.ERROR_LOCKUNLOCKCARD_OLDSTATUS_INVALID)
  private String oldStatus;

  @Required(message = ErrorMessage.ERROR_LOCKUNLOCKCARD_NEWSTATUS_REQUIRED)
  @ValidateWith(
      value = IsBankCardStatus.class,
      message = ErrorMessage.ERROR_LOCKUNLOCKCARD_NEWSTATUS_INVALID)
  private String newStatus;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the oldStatus */
  public String getOldStatus() {

    return oldStatus;
  }

  /** @param oldStatus the oldStatus to set */
  public void setOldStatus(String oldStatus) {

    this.oldStatus = oldStatus;
  }

  /** @return the newStatus */
  public String getNewStatus() {

    return newStatus;
  }

  /** @param newStatus the newStatus to set */
  public void setNewStatus(String newStatus) {

    this.newStatus = newStatus;
  }
}
