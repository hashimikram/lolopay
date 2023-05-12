/** */
package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.validators.IsBirthDate;
import ro.iss.lolopay.validators.IsCompanyType;
import ro.iss.lolopay.validators.IsCountryISO;

@Validate
public class RequestSaveLegalUser extends RestRequest implements Validatable<String> {

  @Required(message = ErrorMessage.ERROR_SAVELEGALUSER_ID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_SAVEUSER_ID_INVALID)
  private String id;

  @ValidateWith(
      value = IsCompanyType.class,
      message = ErrorMessage.ERROR_SAVELEGALUSER_COMPANYTYPE_INVALID)
  private String companyType;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_COMPANYNAME)
  private String companyName;

  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_SAVELEGALUSER_COUNTRY_INVALID)
  private String countryOfResidence;

  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_SAVELEGALUSER_NATIONALITY_INVALID)
  private String nationality;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_FNAME_MAXLENGTH)
  private String firstName;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_LNAME_MAXLENGTH)
  private String lastName;

  @ValidateWith(value = IsBirthDate.class, message = ErrorMessage.ERROR_SAVELEGALUSER_BDATE_INVALID)
  private Long birthDate;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_MOBILEPHONE_MAXLENGTH)
  private String mobilePhone;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_COMPANYEMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_SAVELEGALUSER_COMPANYEMAIL_INVALID)
  private String companyEmail;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_EMAIL_MAXLENGTH)
  @Email(message = ErrorMessage.ERROR_SAVELEGALUSER_EMAIL_INVALID)
  private String email;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_SAVELEGALUSER_TAG_MAXLENGTH)
  private String customTag;

  @Valid private RequestAddress address;

  @Valid private RequestAddress companyAddress;

  private String companyRegistrationNumber;

  @Override
  public String validate() {

    if (this.companyType.equals(String.valueOf(CompanyType.BUSINESS))) {

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

  /** @return the id */
  public String getId() {

    return id;
  }

  /** @param id the id to set */
  public void setId(String id) {

    this.id = id;
  }

  /** @return the companyType */
  public String getCompanyType() {

    return companyType;
  }

  /** @param companyType the companyType to set */
  public void setCompanyType(String companyType) {

    this.companyType = companyType;
  }

  /** @return the companyName */
  public String getCompanyName() {

    return companyName;
  }

  /** @param companyName the companyName to set */
  public void setCompanyName(String companyName) {

    this.companyName = companyName;
  }

  /** @return the countryOfResidence */
  public String getCountryOfResidence() {

    return countryOfResidence;
  }

  /** @param countryOfResidence the countryOfResidence to set */
  public void setCountryOfResidence(String countryOfResidence) {

    this.countryOfResidence = countryOfResidence;
  }

  /** @return the nationality */
  public String getNationality() {

    return nationality;
  }

  /** @param nationality the nationality to set */
  public void setNationality(String nationality) {

    this.nationality = nationality;
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

  /** @return the mobilePhone */
  public String getMobilePhone() {

    return mobilePhone;
  }

  /** @param mobilePhone the mobilePhone to set */
  public void setMobilePhone(String mobilePhone) {

    this.mobilePhone = mobilePhone;
  }

  /** @return the companyEmail */
  public String getCompanyEmail() {

    return companyEmail;
  }

  /** @param companyEmail the companyEmail to set */
  public void setCompanyEmail(String companyEmail) {

    this.companyEmail = companyEmail;
  }

  /** @return the email */
  public String getEmail() {

    return email;
  }

  /** @param email the email to set */
  public void setEmail(String email) {

    this.email = email;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the address */
  public RequestAddress getAddress() {

    return address;
  }

  /** @param address the address to set */
  public void setAddress(RequestAddress address) {

    this.address = address;
  }

  /** @return the companyAddress */
  public RequestAddress getCompanyAddress() {

    return companyAddress;
  }

  /** @param companyAddress the companyAddress to set */
  public void setCompanyAddress(RequestAddress companyAddress) {

    this.companyAddress = companyAddress;
  }
}
