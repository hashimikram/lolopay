package ro.iss.lolopay.classes.provider;

public class ProviderRequestCurrencyFXTrade {
  private String cardHolderId;

  private String currencyFrom;

  private String currencyTo;

  private String amount;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
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
  public String getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(String amount) {

    this.amount = amount;
  }
}
