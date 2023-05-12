package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBankCardCurrency;
import ro.iss.lolopay.validators.IsInteger;

public class RequestBankCardTransfer {
  @Required(message = ErrorMessage.ERROR_CARDTRANSFER_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CARDTRANSFER_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_REQUIRED)
  @ValidateWith(
      value = IsBankCardCurrency.class,
      message = ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_INVALID)
  private String currency;

  @Required(message = ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_REQUIRED)
  @ValidateWith(value = IsInteger.class, message = ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_INVALID)
  private Integer amount;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  /** @return the amount */
  public Integer getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(Integer amount) {

    this.amount = amount;
  }
}
