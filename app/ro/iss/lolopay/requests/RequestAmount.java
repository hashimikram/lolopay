package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCurrencyISO;
import ro.iss.lolopay.validators.IsInteger;

public class RequestAmount extends RestRequest {
  /** Amount value */
  @Required(message = ErrorMessage.ERROR_AMOUNT_VALUE_REQUIRED)
  @ValidateWith(value = IsInteger.class, message = ErrorMessage.ERROR_AMOUNT_INVALID)
  private Integer value;

  /** Amount currency */
  @Required(message = ErrorMessage.ERROR_AMOUNT_CURRENCY_REQUIRED)
  @ValidateWith(value = IsCurrencyISO.class, message = ErrorMessage.ERROR_AMOUNT_CURRENCY_INVALID)
  private String currency;

  /** @return the value */
  public Integer getValue() {

    return value;
  }

  /** @param value the value to set */
  public void setValue(Integer value) {

    this.value = value;
  }

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }
}
