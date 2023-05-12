package ro.iss.lolopay.classes.provider;

public class ProviderRequestCardInquiry extends ProviderRequest {
  /** card holder id */
  private String cardHolderId;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }
}
