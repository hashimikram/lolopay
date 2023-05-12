package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestCreateBankAccount_GB extends RequestCreateBankAccount {
  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_SORTCODE_REQUIRED)
  @MinLength(value = 6, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_SORTCODE_MINLENGTH)
  @MaxLength(value = 6, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_SORTCODE_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_SORTCODE_INVALID)
  private String sortCode;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_REQUIRED)
  @MinLength(value = 8, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_MINLENGTH)
  @MaxLength(value = 8, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_GB_ACCOUNTNUMBER_INVALID)
  private String accountNumber;

  /** @return the sortCode */
  public String getSortCode() {

    return sortCode;
  }

  /** @param sortCode the sortCode to set */
  public void setSortCode(String sortCode) {

    this.sortCode = sortCode;
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
