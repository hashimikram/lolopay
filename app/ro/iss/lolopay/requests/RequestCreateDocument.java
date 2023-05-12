package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsDocumentType;

public class RequestCreateDocument {
  @Required(message = ErrorMessage.ERROR_CREATEDOCUMENT_USERID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEDOCUMENT_USERID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATEDOCUMENT_TYPE_REQUIRED)
  @ValidateWith(
      value = IsDocumentType.class,
      message = ErrorMessage.ERROR_CREATEDOCUMENT_TYPE_INVALID)
  private String type;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEDOCUMENT_TAG_MAXLENGTH)
  private String customTag;

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the type */
  public String getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(String type) {

    this.type = type;
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
