package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestUpdateBankCard {
  @Required(message = ErrorMessage.ERROR_UPDATEBANKCARD_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_UPDATEBANKCARD_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_UPDATEBANKCARD_FIRSTNAME_REQUIRED)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_UPDATEBANKCARD_FIRSTNAME_MAXLENGTH)
  private String firstName;

  @Required(message = ErrorMessage.ERROR_UPDATEBANKCARD_LASTNAME_REQUIRED)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_UPDATEBANKCARD_LASTNAME_MAXLENGTH)
  private String lastName;

  @Valid private RequestCardUserInfo cardUserInfo;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the firstName */
  public String getFirstName() {

    return firstName;
  }

  /** @param firstName the firstName to set */
  public void setFirstName(String firstName) {

    this.firstName = firstName;
  }

  /** @return the lastName */
  public String getLastName() {

    return lastName;
  }

  /** @param lastName the lastName to set */
  public void setLastName(String lastName) {

    this.lastName = lastName;
  }

  public RequestCardUserInfo getCardUserInfo() {

    return cardUserInfo;
  }

  public void setCardUserInfo(RequestCardUserInfo cardUserInfo) {

    this.cardUserInfo = cardUserInfo;
  }
}
