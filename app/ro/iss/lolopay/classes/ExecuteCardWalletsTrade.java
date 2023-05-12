package ro.iss.lolopay.classes;

public class ExecuteCardWalletsTrade {
  private String fromCurrency;

  private String toCurrency;

  private String amount;

  private String rate;

  /** @return the fromCurrency */
  public String getFromCurrency() {

    return fromCurrency;
  }

  /** @param fromCurrency the fromCurrency to set */
  public void setFromCurrency(String fromCurrency) {

    this.fromCurrency = fromCurrency;
  }

  /** @return the toCurrency */
  public String getToCurrency() {

    return toCurrency;
  }

  /** @param toCurrency the toCurrency to set */
  public void setToCurrency(String toCurrency) {

    this.toCurrency = toCurrency;
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
