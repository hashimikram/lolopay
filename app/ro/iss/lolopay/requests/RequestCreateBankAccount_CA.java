package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestCreateBankAccount_CA extends RequestCreateBankAccount {
  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BRANCHCODE_REQUIRED)
  @MaxLength(value = 5, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BRANCHCODE_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BRANCHCODE_INVALID)
  private String branchCode;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_INSTNUMBER_REQUIRED)
  @MinLength(value = 3, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_INSTNUMBER_MINLENGTH)
  @MaxLength(value = 4, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_INSTNUMBER_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_INSTNUMBER_INVALID)
  private String institutionNumber;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_ACCOUNTNUMBER_REQUIRED)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_ACCOUNTNUMBER_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_ACCOUNTNUMBER_INVALID)
  private String accountNumber;

  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BANKNAME_REQUIRED)
  @MaxLength(value = 50, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BANKNAME_MAXLENGTH)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS_LETTERS_SPACE,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_CA_BANKNAME_INVALID)
  private String bankName;

  /** @return the branchCode */
  public String getBranchCode() {

    return branchCode;
  }

  /** @param branchCode the branchCode to set */
  public void setBranchCode(String branchCode) {

    this.branchCode = branchCode;
  }

  /** @return the institutionNumber */
  public String getInstitutionNumber() {

    return institutionNumber;
  }

  /** @param institutionNumber the institutionNumber to set */
  public void setInstitutionNumber(String institutionNumber) {

    this.institutionNumber = institutionNumber;
  }

  /** @return the accountNumber */
  public String getAccountNumber() {

    return accountNumber;
  }

  /** @param accountNumber the accountNumber to set */
  public void setAccountNumber(String accountNumber) {

    this.accountNumber = accountNumber;
  }

  /** @return the bankName */
  public String getBankName() {

    return bankName;
  }

  /** @param bankName the bankName to set */
  public void setBankName(String bankName) {

    this.bankName = bankName;
  }
}
