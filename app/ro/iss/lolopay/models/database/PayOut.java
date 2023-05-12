package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.PaymentType;

@Entity(value = "transactions", noClassnameStored = true)
public class PayOut extends Transaction {
  /** Payment type can be Card or Bank, it applies to PayIn and PayOut only */
  private PaymentType paymentType;

  /** Pay out associated bank account */
  private String bankAccountId;

  /** A custom reference you wish to appear on the user’s bank statement */
  private String bankWireRef;

  /** An external reference/provider bank transfer ID */
  private String externalReference;

  /** @return the paymentType */
  public PaymentType getPaymentType() {

    return paymentType;
  }

  /** @param paymentType the paymentType to set */
  public void setPaymentType(PaymentType paymentType) {

    this.paymentType = paymentType;
  }

  /** @return the bankAccountId */
  public String getBankAccountId() {

    return bankAccountId;
  }

  /** @param bankAccountId the bankAccountId to set */
  public void setBankAccountId(String bankAccountId) {

    this.bankAccountId = bankAccountId;
  }

  /** @return the bankWireRef */
  public String getBankWireRef() {

    return bankWireRef;
  }

  /** @param bankWireRef the bankWireRef to set */
  public void setBankWireRef(String bankWireRef) {

    this.bankWireRef = bankWireRef;
  }

  /** @return the externalReference */
  public String getExternalReference() {

    return externalReference;
  }

  /** @param externalReference the externalReference to set */
  public void setExternalReference(String externalReference) {

    this.externalReference = externalReference;
  }
}
