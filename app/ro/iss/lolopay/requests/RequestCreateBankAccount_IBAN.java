package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBIC;
import ro.iss.lolopay.validators.IsIBAN;

public class RequestCreateBankAccount_IBAN extends RequestCreateBankAccount {
  @Required(message = ErrorMessage.ERROR_CREATEBANKACCOUNT_IBAN_IBAN_REQUIRED)
  @ValidateWith(
      value = IsIBAN.class,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_IBAN_IBAN_INVALID)
  private String iban;

  @MinLength(value = 8, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_IBAN_BIC_MINLENGTH)
  @MaxLength(value = 11, message = ErrorMessage.ERROR_CREATEBANKACCOUNT_IBAN_BIC_MAXLENGTH)
  @ValidateWith(
      value = IsBIC.class,
      message = ErrorMessage.ERROR_CREATEBANKACCOUNT_IBAN_BIC_INVALID)
  private String bic;

  /** @return the iban */
  public String getIban() {

    return iban;
  }

  /** @param iban the iban to set */
  public void setIban(String iban) {

    this.iban = iban;
  }

  /** @return the bic */
  public String getBic() {

    return bic;
  }

  /** @param bic the bic to set */
  public void setBic(String bic) {

    this.bic = bic;
  }
}
