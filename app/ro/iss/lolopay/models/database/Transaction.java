package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;

@Entity(value = "transactions", noClassnameStored = true)
public class Transaction extends TableCollection {
  /** Developer custom tag */
  private String customTag;

  /** A user's ID */
  @Indexed private String debitedUserId;

  /** The ID of the wallet that was debited */
  @Indexed private String debitedWalletId;

  /** The user ID who is credited (defaults to the owner of the wallet) */
  @Indexed private String creditedUserId;

  /** The ID of the wallet where money will be credited */
  @Indexed private String creditedWalletId;

  /** Information about the funds that are being debited */
  private Amount amount;

  /** The status of the transaction */
  private TransactionStatus status;

  /** The result code */
  private String resultCode = "";

  /** A verbal explanation of the ResultCode */
  private String resultMessage = "";

  /** When the transaction happened */
  private Long executionDate;

  /** The type of the transaction: in, out or transfer */
  private TransactionType type;

  /** The nature of the transaction */
  private TransactionNature nature;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId = "";

  /** Reference for related fee transactions */
  @Indexed private String relatedTransactionId = "";

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the debitedUserId */
  public String getDebitedUserId() {

    return debitedUserId;
  }

  /** @param debitedUserId the debitedUserId to set */
  public void setDebitedUserId(String debitedUserId) {

    this.debitedUserId = debitedUserId;
  }

  /** @return the debitedWalletId */
  public String getDebitedWalletId() {

    return debitedWalletId;
  }

  /** @param debitedWalletId the debitedWalletId to set */
  public void setDebitedWalletId(String debitedWalletId) {

    this.debitedWalletId = debitedWalletId;
  }

  /** @return the creditedUserId */
  public String getCreditedUserId() {

    return creditedUserId;
  }

  /** @param creditedUserId the creditedUserId to set */
  public void setCreditedUserId(String creditedUserId) {

    this.creditedUserId = creditedUserId;
  }

  /** @return the creditedWalletId */
  public String getCreditedWalletId() {

    return creditedWalletId;
  }

  /** @param creditedWalletId the creditedWalletId to set */
  public void setCreditedWalletId(String creditedWalletId) {

    this.creditedWalletId = creditedWalletId;
  }

  /** @return the amount */
  public Amount getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(Amount amount) {

    this.amount = amount;
  }

  /** @return the status */
  public TransactionStatus getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(TransactionStatus status) {

    this.status = status;
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

  /** @return the executionDate */
  public Long getExecutionDate() {

    return executionDate;
  }

  /** @param executionDate the executionDate to set */
  public void setExecutionDate(Long executionDate) {

    this.executionDate = executionDate;
  }

  /** @return the type */
  public TransactionType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(TransactionType type) {

    this.type = type;
  }

  /** @return the nature */
  public TransactionNature getNature() {

    return nature;
  }

  /** @param nature the nature to set */
  public void setNature(TransactionNature nature) {

    this.nature = nature;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return the relatedTransactionId */
  public String getRelatedTransactionId() {

    return relatedTransactionId;
  }

  /** @param relatedTransactionId the relatedTransactionId to set */
  public void setRelatedTransactionId(String relatedTransactionId) {

    this.relatedTransactionId = relatedTransactionId;
  }
}
