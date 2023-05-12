package ro.iss.lolopay.classes.provider;

public class ProviderRequestTransfer extends ProviderRequest {
  private String cardHolderId;

  private String amount;

  private String currencyCode;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }

  /** @return the amount */
  public String getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(String amount) {

    this.amount = amount;
  }

  /** @return the currencyCode */
  public String getCurrencyCode() {

    return currencyCode;
  }

  /** @param currencyCode the currencyCode to set */
  public void setCurrencyCode(String currencyCode) {

    this.currencyCode = currencyCode;
  }
}
