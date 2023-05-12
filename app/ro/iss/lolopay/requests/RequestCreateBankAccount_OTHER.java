package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBIC;
import ro.iss.lolopay.validators.IsCountryISO;

public class RequestCreateBankAccount_OTHER extends RequestCreateBankAccount {
  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_COUNTRY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_COUNTRY_INVALID)
  private String country;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_BIC_REQUIRED)
  @MinLength(value = 8, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_BIC_MINLENGTH)
  @MaxLength(value = 11, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_BIC_MAXLENGTH)
  @ValidateWith(
      value = IsBIC.class,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_BIC_INVALID)
  private String bic;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_ACCOUNTNUMBER_REQUIRED)
  @MaxLength(
      value = 20,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_ACCOUNTNUMBER_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_OTHER_ACCOUNTNUMBER_INVALID)
  private String accountNumber;

  /** @return the country */
  public String getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(String country) {

    this.country = country;
  }

  /** @return the bic */
  public String getBic() {

    return bic;
  }

  /** @param bic the bic to set */
  public void setBic(String bic) {

    this.bic = bic;
  }

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }
}
