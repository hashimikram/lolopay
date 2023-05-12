package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestLogin extends RestRequest {
  @Required(message = ErrorMessage.ERROR_LOGIN_ACCOUNTID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGOID,
      message = ErrorMessage.ERROR_LOGIN_ACCOUNTID_INVALID)
  private String accountId;

  @Required(message = ErrorMessage.ERROR_LOGIN_APPID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGOID,
      message = ErrorMessage.ERROR_LOGIN_APPID_INVALID)
  private String applicationId;

  @MaxLength(value = 256, message = ErrorMessage.ERROR_LOGIN_INVALID_CREDENTIALS)
  @Required(message = ErrorMessage.ERROR_LOGIN_PASSWORD_REQUIRED)
  private String password;

  public String getAccountId() {

    return accountId;
  }

  public void setAccountId(String accountId) {

    this.accountId = accountId;
  }

  public String getApplicationId() {

    return applicationId;
  }

  public void setApplicationId(String applicationId) {

    this.applicationId = applicationId;
  }

  public String getPassword() {

    return password;
  }

  public void setPassword(String password) {

    this.password = password;
  }
}
