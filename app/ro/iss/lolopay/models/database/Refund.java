package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.RefundReasonType;
import ro.iss.lolopay.models.classes.TransactionType;

@Entity(value = "transactions", noClassnameStored = true)
public class Refund extends Transaction {
  /** Initial transaction identifier. */
  private String initialTransactionId;

  /** Initial transaction type. */
  private TransactionType initialTransactionType;

  /** Refund reason */
  private RefundReasonType refundReasonType;

  /** Refund reason note */
  private String refusedReasonMessage;

  /** @return the initialTransactionId */
  public String getInitialTransactionId() {

    return initialTransactionId;
  }

  /** @param initialTransactionId the initialTransactionId to set */
  public void setInitialTransactionId(String initialTransactionId) {

    this.initialTransactionId = initialTransactionId;
  }

  /** @return the initialTransactionType */
  public TransactionType getInitialTransactionType() {

    return initialTransactionType;
  }

  /** @param initialTransactionType the initialTransactionType to set */
  public void setInitialTransactionType(TransactionType initialTransactionType) {

    this.initialTransactionType = initialTransactionType;
  }

  /** @return the refundReasonType */
  public RefundReasonType getRefundReasonType() {

    return refundReasonType;
  }

  /** @param refundReasonType the refundReasonType to set */
  public void setRefundReasonType(RefundReasonType refundReasonType) {

    this.refundReasonType = refundReasonType;
  }

  /** @return the refusedReasonMessage */
  public String getRefusedReasonMessage() {

    return refusedReasonMessage;
  }

  /** @param refusedReasonMessage the refusedReasonMessage to set */
  public void setRefusedReasonMessage(String refusedReasonMessage) {

    this.refusedReasonMessage = refusedReasonMessage;
  }
}
