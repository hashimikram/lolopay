package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsTrue;

public class RequestDeactivateBankAccount extends RestRequest {
  @Required(message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_USERID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_USERID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_BANKACCOUNTID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_INVALID_BANKACCOUNTID)
  private String bankAccountId;

  @Required(message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_NEWSTATUS_REQUIRED)
  @ValidateWith(
      value = IsTrue.class,
      message = ErrorMessage.ERROR_DEACTIVATE_BANKACCOUNT_NEWSTATUS_INVALID)
  private Boolean active;

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the bankAccountId */
  public String getBankAccountId() {

    return bankAccountId;
  }

  /** @param bankAccountId the bankAccountId to set */
  public void setBankAccountId(String bankAccountId) {

    this.bankAccountId = bankAccountId;
  }

  /** @return the active */
  public Boolean getActive() {

    return active;
  }

  /** @param active the active to set */
  public void setActive(Boolean active) {

    this.active = active;
  }
}
