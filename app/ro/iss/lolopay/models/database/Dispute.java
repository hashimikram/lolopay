package ro.iss.lolopay.models.database;

import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.DisputeReason;
import ro.iss.lolopay.models.classes.DisputeStatus;
import ro.iss.lolopay.models.classes.DisputeType;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionType;

public class Dispute extends TableCollection {
  private String tag;

  private String initialTransactionId;

  private TransactionType initialTransactionType;

  private String resultCode;

  private String resultMessage;

  private DisputeReason disputeReason;

  private DisputeStatus status;

  private String statusMessage;

  private Amount disputedFunds;

  private Amount contestedFunds;

  private DisputeType disputeType;

  private long contestDeadlineDate;

  private String repudiationId;

  /** @return the tag */
  public String getTag() {

    return tag;
  }

  /** @param tag the tag to set */
  public void setTag(String tag) {

    this.tag = tag;
  }

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

  /** @return the resultCode */
  public String getResultCode() {

    return resultCode;
  }

  /** @param resultCode the resultCode to set */
  public void setResultCode(String resultCode) {

    this.resultCode = resultCode;
  }

  /** @return the resultMessage */
  public String getResultMessage() {

    return resultMessage;
  }

  /** @param resultMessage the resultMessage to set */
  public void setResultMessage(String resultMessage) {

    this.resultMessage = resultMessage;
  }

  /** @return the disputeReason */
  public DisputeReason getDisputeReason() {

    return disputeReason;
  }

  /** @param disputeReason the disputeReason to set */
  public void setDisputeReason(DisputeReason disputeReason) {

    this.disputeReason = disputeReason;
  }

  /** @return the status */
  public DisputeStatus getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(DisputeStatus status) {

    this.status = status;
  }

  /** @return the statusMessage */
  public String getStatusMessage() {

    return statusMessage;
  }

  /** @param statusMessage the statusMessage to set */
  public void setStatusMessage(String statusMessage) {

    this.statusMessage = statusMessage;
  }

  /** @return the disputedFunds */
  public Amount getDisputedFunds() {

    return disputedFunds;
  }

  /** @param disputedFunds the disputedFunds to set */
  public void setDisputedFunds(Amount disputedFunds) {

    this.disputedFunds = disputedFunds;
  }

  /** @return the contestedFunds */
  public Amount getContestedFunds() {

    return contestedFunds;
  }

  /** @param contestedFunds the contestedFunds to set */
  public void setContestedFunds(Amount contestedFunds) {

    this.contestedFunds = contestedFunds;
  }

  /** @return the disputeType */
  public DisputeType getDisputeType() {

    return disputeType;
  }

  /** @param disputeType the disputeType to set */
  public void setDisputeType(DisputeType disputeType) {

    this.disputeType = disputeType;
  }

  /** @return the contestDeadlineDate */
  public long getContestDeadlineDate() {

    return contestDeadlineDate;
  }

  /** @param contestDeadlineDate the contestDeadlineDate to set */
  public void setContestDeadlineDate(long contestDeadlineDate) {

    this.contestDeadlineDate = contestDeadlineDate;
  }

  /** @return the repudiationId */
  public String getRepudiationId() {

    return repudiationId;
  }

  /** @param repudiationId the repudiationId to set */
  public void setRepudiationId(String repudiationId) {

    this.repudiationId = repudiationId;
  }
}
