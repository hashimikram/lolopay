package ro.iss.lolopay.models.classes;

public class DisputeReason {
  private DisputeReasonType disputeReasonType;

  private String disputeReasonMessage;

  /** @return the disputeReasonType */
  public DisputeReasonType getDisputeReasonType() {

    return disputeReasonType;
  }

  /** @param disputeReasonType the disputeReasonType to set */
  public void setDisputeReasonType(DisputeReasonType disputeReasonType) {

    this.disputeReasonType = disputeReasonType;
  }

  /** @return the disputeReasonMessage */
  public String getDisputeReasonMessage() {

    return disputeReasonMessage;
  }

  /** @param disputeReasonMessage the disputeReasonMessage to set */
  public void setDisputeReasonMessage(String disputeReasonMessage) {

    this.disputeReasonMessage = disputeReasonMessage;
  }
}
