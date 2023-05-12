package ro.iss.lolopay.classes.provider;

public class ProviderRequestGetCardWallet extends ProviderRequest {
  /** card holder id */
  private String cardHolderId;

  private String currency;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
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
