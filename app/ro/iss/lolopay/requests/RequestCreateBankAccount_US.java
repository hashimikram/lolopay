package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsAba;
import ro.iss.lolopay.validators.IsDepositAccountType;

public class RequestCreateBankAccount_US extends RequestCreateBankAccount {
  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ACCOUNTNUMBER_REQUIRED)
  @MaxLength(value = 25, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ACCOUNTNUMBER_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ACCOUNTNUMBER_INVALID)
  private String accountNumber;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ABA_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ABA_MAXLENGTH)
  @ValidateWith(value = IsAba.class, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_ABA_INVALID)
  private String aba;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_DEPACCTYPE_REQUIRED)
  @ValidateWith(
      value = IsDepositAccountType.class,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_US_DEPACCTYPE_INVALID)
  private String depositAccountType;

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }

  /** @return the aba */
  public String getAba() {

    return aba;
  }

  /** @param aba the aba to set */
  public void setAba(String aba) {

    this.aba = aba;
  }

  /** @return the depositAccountType */
  public String getDepositAccountType() {

    return depositAccountType;
  }

  /** @param depositAccountType the depositAccountType to set */
  public void setDepositAccountType(String depositAccountType) {

    this.depositAccountType = depositAccountType;
  }
}
