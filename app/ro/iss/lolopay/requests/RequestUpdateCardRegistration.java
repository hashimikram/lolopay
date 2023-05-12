package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestUpdateCardRegistration {
  @MaxLength(value = 255, message = ErrorMessage.ERROR_UPDATECARDREGISTRATION_TAG_INVALID)
  private String tag;

  @MaxLength(
      value = 255,
      message = ErrorMessage.ERROR_UPDATECARDREGISTRATION_REGISTRATIONDATA_INVALID)
  private String registrationData;

  @Required(message = ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_ID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_UPDATECARDREGISTRATION_USER_ID_INVALID)
  private String userId;

  private String apiVersion = "api6";

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the tag */
  public String getTag() {

    return tag;
  }

  /** @param tag the tag to set */
  public void setTag(String tag) {

    this.tag = tag;
  }

  /** @return the registrationData */
  public String getRegistrationData() {

    return registrationData;
  }

  /** @param registrationData the registrationData to set */
  public void setRegistrationData(String registrationData) {

    this.registrationData = registrationData;
  }

  /** @return the apiVersion */
  public String getApiVersion() {

    return apiVersion;
  }

  /** @param apiVersion the apiVersion to set */
  public void setApiVersion(String apiVersion) {

    this.apiVersion = apiVersion;
  }
}
