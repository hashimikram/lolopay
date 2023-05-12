package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBirthDate;
import ro.iss.lolopay.validators.IsCountryISO;
import ro.iss.lolopay.validators.IsIncomeRange;

public class RequestSaveNaturalUser extends RestRequest {

  @Required(message = ErrorMessage.ERROR_SAVEUSER_ID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_SAVEUSER_ID_INVALID)
  private String id;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_EMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_SAVEUSER_EMAIL_INVALID)
  private String email;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_MOBILEPHONE_MAXLENGTH)
  private String mobilePhone;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_FNAME_MAXLENGTH)
  private String firstName;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_LNAME_MAXLENGTH)
  private String lastName;

  @ValidateWith(value = IsBirthDate.class, message = ErrorMessage.ERROR_SAVEUSER_BDATE_INVALID)
  private Long birthDate;

  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_SAVEUSER_NATIONALITY_INVALID)
  private String nationality;

  @ValidateWith(value = IsCountryISO.class, message = ErrorMessage.ERROR_SAVEUSER_COUNTRY_INVALID)
  private String countryOfResidence;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_TAG_MAXLENGTH)
  private String customTag;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVEUSER_OCCUPATION_MAXLENGTH)
  private String occupation;

  @ValidateWith(
      value = IsIncomeRange.class,
      message = ErrorMessage.ERROR_SAVEUSER_INCOME_RANGE_INVALID)
  private String incomeRange;

  @Valid private RequestAddress address;

  /** @return the id */
  public String getId() {

    return id;
  }

  /** @param id the id to set */
  public void setId(String id) {

    this.id = id;
  }

  /** @return the email */
  public String getEmail() {

    return email;
  }

  /** @param email the email to set */
  public void setEmail(String email) {

    this.email = email;
  }

  /** @return the mobilePhone */
  public String getMobilePhone() {

    return mobilePhone;
  }

  /** @param mobilePhone the mobilePhone to set */
  public void setMobilePhone(String mobilePhone) {

    this.mobilePhone = mobilePhone;
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

  /** @return the birthDate */
  public Long getBirthDate() {

    return birthDate;
  }

  /** @param birthDate the birthDate to set */
  public void setBirthDate(Long birthDate) {

    this.birthDate = birthDate;
  }

  /** @return the nationality */
  public String getNationality() {

    return nationality;
  }

  /** @param nationality the nationality to set */
  public void setNationality(String nationality) {

    this.nationality = nationality;
  }

  /** @return the countryOfResidence */
  public String getCountryOfResidence() {

    return countryOfResidence;
  }

  /** @param countryOfResidence the countryOfResidence to set */
  public void setCountryOfResidence(String countryOfResidence) {

    this.countryOfResidence = countryOfResidence;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the occupation */
  public String getOccupation() {

    return occupation;
  }

  /** @param occupation the occupation to set */
  public void setOccupation(String occupation) {

    this.occupation = occupation;
  }

  /** @return the incomeRange */
  public String getIncomeRange() {

    return incomeRange;
  }

  /** @param incomeRange the incomeRange to set */
  public void setIncomeRange(String incomeRange) {

    this.incomeRange = incomeRange;
  }

  /** @return the address */
  public RequestAddress getAddress() {

    return address;
  }

  /** @param address the address to set */
  public void setAddress(RequestAddress address) {

    this.address = address;
  }
}
