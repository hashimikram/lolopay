package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCardUserInfoEmploymentStatus;
import ro.iss.lolopay.validators.IsCardUserInfoEstate;
import ro.iss.lolopay.validators.IsCardUserInfoMonthlyIncome;
import ro.iss.lolopay.validators.IsCardUserInfoOccupation;
import ro.iss.lolopay.validators.IsCardUserInfoPurpose;

public class RequestCardUserInfo {
  @Required(message = ErrorMessage.ERROR_CARDUSERINFO_EMPLOYMENTSTATUS_REQUIRED)
  @ValidateWith(
      value = IsCardUserInfoEmploymentStatus.class,
      message = ErrorMessage.ERROR_CARDUSERINFO_EMPLOYMENTSTATUS_INVALID)
  private String employmentStatus;

  @Required(message = ErrorMessage.ERROR_CARDUSERINFO_OCCUPATION_REQUIRED)
  @ValidateWith(
      value = IsCardUserInfoOccupation.class,
      message = ErrorMessage.ERROR_CARDUSERINFO_OCCUPATION_INVALID)
  private String occupation;

  @Required(message = ErrorMessage.ERROR_CARDUSERINFO_PURPOSE_REQUIRED)
  @ValidateWith(
      value = IsCardUserInfoPurpose.class,
      message = ErrorMessage.ERROR_CARDUSERINFO_PURPOSE_INVALID)
  private String purpose;

  @Required(message = ErrorMessage.ERROR_CARDUSERINFO_MONTHLYINCOME_REQUIRED)
  @ValidateWith(
      value = IsCardUserInfoMonthlyIncome.class,
      message = ErrorMessage.ERROR_CARDUSERINFO_MONTHLYINCOME_INVALID)
  private String monthlyIncome;

  @Required(message = ErrorMessage.ERROR_CARDUSERINFO_ESTATE_REQUIRED)
  @ValidateWith(
      value = IsCardUserInfoEstate.class,
      message = ErrorMessage.ERROR_CARDUSERINFO_ESTATE_INVALID)
  private String estate;

  /** @return the employmentStatus */
  public String getEmploymentStatus() {

    return employmentStatus;
  }

  /** @param employmentStatus the employmentStatus to set */
  public void setEmploymentStatus(String employmentStatus) {

    this.employmentStatus = employmentStatus;
  }

  /** @return the occupation */
  public String getOccupation() {

    return occupation;
  }

  /** @param occupation the occupation to set */
  public void setOccupation(String occupation) {

    this.occupation = occupation;
  }

  /** @return the purpose */
  public String getPurpose() {

    return purpose;
  }

  /** @param purpose the purpose to set */
  public void setPurpose(String purpose) {

    this.purpose = purpose;
  }

  /** @return the monthlyIncome */
  public String getMonthlyIncome() {

    return monthlyIncome;
  }

  /** @param monthlyIncome the monthlyIncome to set */
  public void setMonthlyIncome(String monthlyIncome) {

    this.monthlyIncome = monthlyIncome;
  }

  /** @return the estate */
  public String getEstate() {

    return estate;
  }

  /** @param estate the estate to set */
  public void setEstate(String estate) {

    this.estate = estate;
  }
}
