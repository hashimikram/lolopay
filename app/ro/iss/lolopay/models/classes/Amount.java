package ro.iss.lolopay.models.classes;

public class Amount {

  /** Amount value */
  private Integer value;

  /** Amount currency */
  private CurrencyISO currency;

  /** @return the value */
  public Integer getValue() {

    return value;
  }

  /** @param value the value to set */
  public void setValue(Integer value) {

    this.value = value;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }
}
