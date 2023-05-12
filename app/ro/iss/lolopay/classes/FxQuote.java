package ro.iss.lolopay.classes;

public class FxQuote {
  private String currencyFrom;

  private String currencyTo;

  private String amount;

  private String rate;

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
  public String getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(String amount) {

    this.amount = amount;
  }

  /** @return the rate */
  public String getRate() {

    return rate;
  }

  /** @param rate the rate to set */
  public void setRate(String rate) {

    this.rate = rate;
  }
}
