package ro.iss.lolopay.models.database;

import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ro.iss.lolopay.models.classes.DocumentRejectReason;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.DocumentType;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "documents", noClassnameStored = true)
public class Document extends TableCollection {
  /** Developer custom tag */
  private String customTag;

  /** A user's ID */
  @Indexed private String userId;

  /** Document type for KYC */
  private DocumentType type;

  /** Document status in the system */
  private DocumentStatus status;

  /** Text message clarifying why a document has been rejected */
  private String rejectionReasonMessage;

  /** Reason for rejection as type from list */
  private DocumentRejectReason rejectionReasonType;

  /** List of page names associated with this document */
  @JsonIgnore private List<String> pages;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId;

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the type */
  public DocumentType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(DocumentType type) {

    this.type = type;
  }

  /** @return the status */
  public DocumentStatus getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(DocumentStatus status) {

    this.status = status;
  }

  /** @return the rejectionReasonMessage */
  public String getRejectionReasonMessage() {

    return rejectionReasonMessage;
  }

  /** @param rejectionReasonMessage the rejectionReasonMessage to set */
  public void setRejectionReasonMessage(String rejectionReasonMessage) {

    this.rejectionReasonMessage = rejectionReasonMessage;
  }

  /** @return the rejectionReasonType */
  public DocumentRejectReason getRejectionReasonType() {

    return rejectionReasonType;
  }

  /** @param rejectionReasonType the rejectionReasonType to set */
  public void setRejectionReasonType(DocumentRejectReason rejectionReasonType) {

    this.rejectionReasonType = rejectionReasonType;
  }

  /** @return the pages */
  public List<String> getPages() {

    return pages;
  }

  /** @param pages the pages to set */
  public void setPages(List<String> pages) {

    this.pages = pages;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }
}
