package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public abstract class RequestCreateBankAccount extends RestRequest {
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_TAG_MAXLENGTH)
  private String customTag;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_USER_ID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERNAME_MAXLENGTH)
  private String ownerName;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OWNERADDRESS_REQUIRED)
  @Valid
  private RequestAddress ownerAddress;

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the ownerName */
  public String getOwnerName() {

    return ownerName;
  }

  /** @param ownerName the ownerName to set */
  public void setOwnerName(String ownerName) {

    this.ownerName = ownerName;
  }

  /** @return the ownerAddress */
  public RequestAddress getOwnerAddress() {

    return ownerAddress;
  }

  /** @param ownerAddress the ownerAddress to set */
  public void setOwnerAddress(RequestAddress beneficiaryAddress) {

    this.ownerAddress = beneficiaryAddress;
  }
}
