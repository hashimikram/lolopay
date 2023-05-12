package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCurrencyISO;
import ro.iss.lolopay.validators.IsDouble;

public class RequestPFSCompanyWalletHook {
  /**
   * Card holder id - id of the card who was debited or credited from main company wallet. Eg:
   * 400000628474
   */
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_PFSHOOK_CARDID_INVALID)
  private String cardholderId;

  /** Amount credited or debit from main company wallet. Eg: 500.0 */
  @Required(message = ErrorMessage.ERROR_PFSHOOK_AMOUNT_REQUIRED)
  @ValidateWith(value = IsDouble.class, message = ErrorMessage.ERROR_PFSHOOK_AMOUNT_INVALID)
  private String amount;

  /** Currency of transfer. Eg: EUR */
  @Required(message = ErrorMessage.ERROR_PFSHOOK_CURRENCY_REQUIRED)
  @ValidateWith(value = IsCurrencyISO.class, message = ErrorMessage.ERROR_PFSHOOK_CURRENCY_INVALID)
  private String currency;

  /** PFS associated transaction id. Eg: 238566 */
  @Required(message = ErrorMessage.ERROR_PFSHOOK_TRANSACTIONID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_DIGITS,
      message = ErrorMessage.ERROR_PFSHOOK_TRANSACTIONID_INVALID)
  private String transactionId;

  /** Transaction type. Eg: D / C */
  @Required(message = ErrorMessage.ERROR_PFSHOOK_TRANSACTION_TYPE_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_PFSHOOK_TRANSACTION_TYPE,
      message = ErrorMessage.ERROR_PFSHOOK_TRANSACTION_TYPE_INVALID)
  private String trantype;

  /** The actual main company wallet balance. Eg: 8.204098E7 */
  @Required(message = ErrorMessage.ERROR_PFSHOOK_BALANCE_REQUIRED)
  @ValidateWith(value = IsDouble.class, message = ErrorMessage.ERROR_PFSHOOK_BALANCE_INVALID)
  private String balance;

  /** @return the cardholderId */
  public String getCardholderId() {

    return cardholderId;
  }

  /** @param cardholderId the cardholderId to set */
  public void setCardholderId(String cardholderId) {

    this.cardholderId = cardholderId;
  }

  /** @return the amount */
  public String getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(String amount) {

    this.amount = amount;
  }

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  /** @return the transactionId */
  public String getTransactionId() {

    return transactionId;
  }

  /** @param transactionId the transactionId to set */
  public void setTransactionId(String transactionId) {

    this.transactionId = transactionId;
  }

  /** @return the trantype */
  public String getTrantype() {

    return trantype;
  }

  /** @param trantype the trantype to set */
  public void setTrantype(String trantype) {

    this.trantype = trantype;
  }

  /** @return the balance */
  public String getBalance() {

    return balance;
  }

  /** @param balance the balance to set */
  public void setBalance(String balance) {

    this.balance = balance;
  }
}
