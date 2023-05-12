package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.validators.IsBirthDate;
import ro.iss.lolopay.validators.IsCompanyType;
import ro.iss.lolopay.validators.IsCountryISO;

@Validate
public class RequestCreateLegalUser extends RestRequest implements Validatable<String> {

  @Valid private RequestAddress address;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_BDATE_REQUIRED)
  @ValidateWith(
      value = IsBirthDate.class,
      message = ErrorMessage.ERROR_CREATELEGALUSER_BDATE_INVALID)
  private Long birthDate;

  @Valid private RequestAddress companyAddress;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYEMAIL_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYEMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_CREATELEGALUSER_EMAIL_INVALID)
  private String companyEmail;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYNAME_MAXLENGTH)
  private String companyName;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYTYPE_REQUIRED)
  @ValidateWith(
      value = IsCompanyType.class,
      message = ErrorMessage.ERROR_CREATELEGALUSER_COMPANYTYPE_INVALID)
  private String companyType;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_COUNTRY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATELEGALUSER_COUNTRY_INVALID)
  private String countryOfResidence;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_TAG_MAXLENGTH)
  private String customTag;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_EMAIL_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_EMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_CREATELEGALUSER_EMAIL_INVALID)
  private String email;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_FNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_FNAME_MAXLENGTH)
  private String firstName;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_LNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_LNAME_MAXLENGTH)
  private String lastName;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_MOBILEPHONE_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATELEGALUSER_MOBILEPHONE_MAXLENGTH)
  private String mobilePhone;

  @Required(message = ErrorMessage.ERROR_CREATELEGALUSER_NATIONALITY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATELEGALUSER_NATIONALITY_INVALID)
  private String nationality;

  private String companyRegistrationNumber;

  @Override
  public String validate() {

    if (String.valueOf(CompanyType.BUSINESS).equals(this.companyType)) {

      // TODO add regex validation for company number according to headquarters country
      if (this.companyRegistrationNumber == null) {
        return ErrorMessage.ERROR_CREATELEGALUSER_COMPANY_REGISTRATION_NUMBER_INVALID;
      }

      if (this.companyRegistrationNumber.isEmpty()) {
        return ErrorMessage.ERROR_CREATELEGALUSER_COMPANY_REGISTRATION_NUMBER_INVALID;
      }
    }
    return null;
  }

  /** @return the companyRegistrationNumber */
  public String getCompanyRegistrationNumber() {

    return companyRegistrationNumber;
  }

  /** @param companyRegistrationNumber the companyRegistrationNumber to set */
  public void setCompanyRegistrationNumber(String companyRegistrationNumber) {

    this.companyRegistrationNumber = companyRegistrationNumber;
  }

  /** @return the address */
  public RequestAddress getAddress() {

    return address;
  }

  /** @return the birthDate */
  public Long getBirthDate() {

    return birthDate;
  }

  /** @return the companyAddress */
  public RequestAddress getCompanyAddress() {

    return companyAddress;
  }

  /** @return the companyEmail */
  public String getCompanyEmail() {

    return companyEmail;
  }

  /** @return the companyName */
  public String getCompanyName() {

    return companyName;
  }

  /** @return the companyType */
  public String getCompanyType() {

    return companyType;
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

  /** @param address the address to set */
  public void setAddress(RequestAddress address) {

    this.address = address;
  }

  /** @param birthDate the birthDate to set */
  public void setBirthDate(Long birthDate) {

    this.birthDate = birthDate;
  }

  /** @param companyAddress the companyAddress to set */
  public void setCompanyAddress(RequestAddress companyAddress) {

    this.companyAddress = companyAddress;
  }

  /** @param companyEmail the companyEmail to set */
  public void setCompanyEmail(String companyEmail) {

    this.companyEmail = companyEmail;
  }

  /** @param companyName the companyName to set */
  public void setCompanyName(String companyName) {

    this.companyName = companyName;
  }

  /** @param companyType the companyType to set */
  public void setCompanyType(String companyType) {

    this.companyType = companyType;
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
}
