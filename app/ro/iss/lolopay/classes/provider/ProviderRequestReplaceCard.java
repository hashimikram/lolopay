package ro.iss.lolopay.classes.provider;

public class ProviderRequestReplaceCard extends ProviderRequest {
  private String cardHolderId;

  private String reason;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }

  /** @return the reason */
  public String getReason() {

    return reason;
  }

  /** @param reason the reason to set */
  public void setReason(String reason) {

    this.reason = reason;
  }
}
