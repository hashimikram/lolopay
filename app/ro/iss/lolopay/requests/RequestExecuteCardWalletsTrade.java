package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCurrencyISO;
import ro.iss.lolopay.validators.IsInteger;

public class RequestExecuteCardWalletsTrade {
  @Required(message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CURRENCYFROM_REQUIRED)
  @ValidateWith(
      value = IsCurrencyISO.class,
      message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CURRENCYFROM_INVALID)
  private String currencyFrom;

  @Required(message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CURRENCYTO_REQUIRED)
  @ValidateWith(
      value = IsCurrencyISO.class,
      message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CURRENCYTO_INVALID)
  private String currencyTo;

  @Required(message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_AMOUNT_REQUIRED)
  @ValidateWith(
      value = IsInteger.class,
      message = ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_AMOUNT_INVALID)
  private Integer amount;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the currencyFrom */
  public String getCurrencyFrom() {

    return currencyFrom;
  }

  /** @param currencyFrom the currencyFrom to set */
  public void setCurrencyFrom(String currencyFrom) {

    this.currencyFrom = currencyFrom;
  }

  /** @return the currencyTo */
  public String getCurrencyTo() {

    return currencyTo;
  }

  /** @param currencyTo the currencyTo to set */
  public void setCurrencyTo(String currencyTo) {

    this.currencyTo = currencyTo;
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
