package ro.iss.lolopay.classes.provider;

public class ProviderRequestChangeCardStatus extends ProviderRequest {
  /** card holder id */
  private String cardHolderId;

  private String oldStatus;

  private String newStatus;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }

  /** @return the oldStatus */
  public String getOldStatus() {

    return oldStatus;
  }

  /** @param oldStatus the oldStatus to set */
  public void setOldStatus(String oldStatus) {

    this.oldStatus = oldStatus;
  }

  /** @return the newStatus */
  public String getNewStatus() {

    return newStatus;
  }

  /** @param newStatus the newStatus to set */
  public void setNewStatus(String newStatus) {

    this.newStatus = newStatus;
  }
}
