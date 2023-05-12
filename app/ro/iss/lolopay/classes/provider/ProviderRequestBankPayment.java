package ro.iss.lolopay.classes.provider;

public class ProviderRequestBankPayment extends ProviderRequest {
  private String cardHolderId;

  private String beneficiaryName;

  private String paymentAmount;

  private String reference;

  private String creditorIban;

  private String creditorBic;

  /** @return the cardHolderId */
  public String getCardHolderId() {

    return cardHolderId;
  }

  /** @param cardHolderId the cardHolderId to set */
  public void setCardHolderId(String cardHolderId) {

    this.cardHolderId = cardHolderId;
  }

  /** @return the beneficiaryName */
  public String getBeneficiaryName() {

    return beneficiaryName;
  }

  /** @param beneficiaryName the beneficiaryName to set */
  public void setBeneficiaryName(String beneficiaryName) {

    this.beneficiaryName = beneficiaryName;
  }

  /** @return the paymentAmount */
  public String getPaymentAmount() {

    return paymentAmount;
  }

  /** @param paymentAmount the paymentAmount to set */
  public void setPaymentAmount(String paymentAmount) {

    this.paymentAmount = paymentAmount;
  }

  /** @return the reference */
  public String getReference() {

    return reference;
  }

  /** @param reference the reference to set */
  public void setReference(String reference) {

    this.reference = reference;
  }

  /** @return the creditorIban */
  public String getCreditorIban() {

    return creditorIban;
  }

  /** @param creditorIban the creditorIban to set */
  public void setCreditorIban(String creditorIban) {

    this.creditorIban = creditorIban;
  }

  /** @return the creditorBic */
  public String getCreditorBic() {

    return creditorBic;
  }

  /** @param creditorBic the creditorBic to set */
  public void setCreditorBic(String creditorBic) {

    this.creditorBic = creditorBic;
  }
}
