package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBirthDate;
import ro.iss.lolopay.validators.IsCountryISO;

public class RequestUbo {
  @Required(message = ErrorMessage.ERROR_CREATEUBO_FNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUBO_FNAME_MAXLENGTH)
  private String firstName;

  @Required(message = ErrorMessage.ERROR_CREATEUBO_LNAME_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEUBO_LNAME_MAXLENGTH)
  private String lastName;

  @Valid private RequestAddress address;

  @Required(message = ErrorMessage.ERROR_CREATEUBO_NATIONALITY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATEUBO_NATIONALITY_INVALID)
  private String nationality;

  @Required(message = ErrorMessage.ERROR_CREATEUBO_BDATE_REQUIRED)
  @ValidateWith(value = IsBirthDate.class, message = ErrorMessage.ERROR_CREATEUBO_BDATE_INVALID)
  private Long birthday;

  @Valid private RequestBirthplace birthplace;

  /** @return the firstName */
  public String getFirstName() {

    return firstName;
  }

  /** @return the lastName */
  public String getLastName() {

    return lastName;
  }

  /** @param firstName the firstName to set */
  public void setFirstName(String firstName) {

    this.firstName = firstName;
  }

  /** @param lastName the lastName to set */
  public void setLastName(String lastName) {

    this.lastName = lastName;
  }

  /** @return the address */
  public RequestAddress getAddress() {

    return address;
  }

  /** @return the birthday */
  public Long getBirthday() {

    return birthday;
  }

  /** @return the nationality */
  public String getNationality() {

    return nationality;
  }

  /** @return the birthplace */
  public RequestBirthplace getBirthplace() {

    return birthplace;
  }

  /** @param address the address to set */
  public void setAddress(RequestAddress address) {

    this.address = address;
  }

  /** @param birthday the birthday to set */
  public void setBirthday(Long birthday) {

    this.birthday = birthday;
  }

  /** @param nationality the nationality to set */
  public void setNationality(String nationality) {

    this.nationality = nationality;
  }

  /** @param birthplace the birthplace to set */
  public void setBirthplace(RequestBirthplace birthplace) {

    this.birthplace = birthplace;
  }
}
