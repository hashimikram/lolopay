package ro.iss.lolopay.classes.provider;

public class ProviderRequestViewStatement extends ProviderRequest {
  /** card holder id */
  private String cardHolderId;

  /** Wallet currency */
  private String currency;

  /** Report start date */
  private String startDate;

  /** Report end date */
  private String endDate;

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

  /** @return the startDate */
  public String getStartDate() {

    return startDate;
  }

  /** @param startDate the startDate to set */
  public void setStartDate(String startDate) {

    this.startDate = startDate;
  }

  /** @return the endDate */
  public String getEndDate() {

    return endDate;
  }

  /** @param endDate the endDate to set */
  public void setEndDate(String endDate) {

    this.endDate = endDate;
  }
}
