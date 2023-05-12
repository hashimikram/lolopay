package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBirthDate;
import ro.iss.lolopay.validators.IsCountryISO;
import ro.iss.lolopay.validators.IsIncomeRange;

public class RequestCreateNaturalUser extends RestRequest {
  @Valid private RequestAddress address;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_BDATE_REQUIRED)
  @ValidateWith(value = IsBirthDate.class, message = ErrorMessage.ERROR_CREATEUSER_BDATE_INVALID)
  private Long birthDate;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_COUNTRY_REQUIRED)
  @ValidateWith(value = IsCountryISO.class, message = ErrorMessage.ERROR_CREATEUSER_COUNTRY_INVALID)
  private String countryOfResidence;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_TAG_MAXLENGTH)
  private String customTag;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_EMAIL_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_EMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_CREATEUSER_EMAIL_INVALID)
  private String email;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_FNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_FNAME_MAXLENGTH)
  private String firstName;

  @ValidateWith(
      value = IsIncomeRange.class,
      message = ErrorMessage.ERROR_CREATEUSER_INCOME_RANGE_INVALID)
  private String incomeRange;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_LNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_LNAME_MAXLENGTH)
  private String lastName;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_MOBILEPHONE_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_MOBILEPHONE_MAXLENGTH)
  private String mobilePhone;

  @Required(message = ErrorMessage.ERROR_CREATEUSER_NATIONALITY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATEUSER_NATIONALITY_INVALID)
  private String nationality;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUSER_OCCUPATION_MAXLENGTH)
  private String occupation;

  /** @return the address */
  public RequestAddress getAddress() {

    return address;
  }

  /** @return the birthDate */
  public Long getBirthDate() {

    return birthDate;
  }

  /** @return the countryOfResidence */
  public String getCountryOfResidence() {

    return countryOfResidence;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @return the email */
  public String getEmail() {

    return email;
  }

  /** @return the firstName */
  public String getFirstName() {

    return firstName;
  }

  /** @return the incomeRange */
  public String getIncomeRange() {

    return incomeRange;
  }

  /** @return the lastName */
  public String getLastName() {

    return lastName;
  }

  /** @return the mobilePhone */
  public String getMobilePhone() {

    return mobilePhone;
  }

  /** @return the nationality */
  public String getNationality() {

    return nationality;
  }

  /** @return the occupation */
  public String getOccupation() {

    return occupation;
  }

  /** @param address the address to set */
  public void setAddress(RequestAddress address) {

    this.address = address;
  }

  /** @param birthDate the birthDate to set */
  public void setBirthDate(Long birthDate) {

    this.birthDate = birthDate;
  }

  /** @param countryOfResidence the countryOfResidence to set */
  public void setCountryOfResidence(String countryOfResidence) {

    this.countryOfResidence = countryOfResidence;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @param email the email to set */
  public void setEmail(String email) {

    this.email = email;
  }

  /** @param firstName the firstName to set */
  public void setFirstName(String firstName) {

    this.firstName = firstName;
  }

  /** @param incomeRange the incomeRange to set */
  public void setIncomeRange(String incomeRange) {

    this.incomeRange = incomeRange;
  }

  /** @param lastName the lastName to set */
  public void setLastName(String lastName) {

    this.lastName = lastName;
  }

  /** @param mobilePhone the mobilePhone to set */
  public void setMobilePhone(String mobilePhone) {

    this.mobilePhone = mobilePhone;
  }

  /** @param nationality the nationality to set */
  public void setNationality(String nationality) {

    this.nationality = nationality;
  }

  /** @param occupation the occupation to set */
  public void setOccupation(String occupation) {

    this.occupation = occupation;
  }
}
