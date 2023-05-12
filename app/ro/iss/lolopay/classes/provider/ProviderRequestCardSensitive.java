package ro.iss.lolopay.classes.provider;

public class ProviderRequestCardSensitive extends ProviderRequest {
  /** card holder id */
  private String cardHolderId;

  /** Phone number */
  private String phone1;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }

  /** @return the phone1 */
  public String getPhone1() {

    return phone1;
  }

  /** @param phone1 the phone1 to set */
  public void setPhone1(String phone1) {

    this.phone1 = phone1;
  }
}
