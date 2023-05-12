package ro.iss.lolopay.models.database;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.classes.UboDeclarationRefusedReasonType;
import ro.iss.lolopay.models.classes.UboDeclarationStatus;

@Entity(value = "ubodeclarations", noClassnameStored = true)
public class UboDeclaration extends TableCollection {
  /** A user's ID */
  @Indexed private String userId;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId;

  /** Date of process. */
  private int processedDate;

  /** Developer custom tag */
  private String customTag;

  /** Ubo declaration status in the system */
  private UboDeclarationStatus status;

  /** Text message clarifying why a document has been rejected */
  private String message;

  /** Reason for rejection as type from list */
  private UboDeclarationRefusedReasonType reason;

  /** List of ubos (Ultimate Beneficial Owners of a BUSINESS Legal User) */
  private List<Ubo> ubos;

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the status */
  public UboDeclarationStatus getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(UboDeclarationStatus status) {

    this.status = status;
  }

  /** @return the rejectionReasonMessage */
  public String getMessage() {

    return message;
  }

  /** @param rejectionReasonMessage the rejectionReasonMessage to set */
  public void setMessage(String rejectionReasonMessage) {

    this.message = rejectionReasonMessage;
  }

  /** @return the rejectionReasonType */
  public UboDeclarationRefusedReasonType getReason() {

    return reason;
  }

  /** @param rejectionReasonType the rejectionReasonType to set */
  public void setReason(UboDeclarationRefusedReasonType rejectionReasonType) {

    this.reason = rejectionReasonType;
  }

  /** @return the ubos */
  public List<Ubo> getUbos() {

    return ubos;
  }

  /** @param ubos the ubos to set */
  public void setUbos(List<Ubo> ubos) {

    this.ubos = ubos;
  }

  /**
   * Adds an ubo
   *
   * @param ubo
   */
  public void addUbo(Ubo ubo) {

    if (this.ubos == null) {
      this.ubos = new ArrayList<Ubo>();
    }
    this.ubos.add(ubo);
  }

  /**
   * Updates an ubo
   *
   * @param ubo
   */
  public void updateUbo(Ubo ubo) {

    if (this.ubos == null) {
      return;
    }
    String uboId = ubo.getProviderId();

    ListIterator<Ubo> iterator = ubos.listIterator();
    while (iterator.hasNext()) {
      Ubo existingUbo = iterator.next();
      // finds the ubo by id and replaces it
      if (existingUbo.getProviderId().equals(uboId)) {
        iterator.set(ubo);
        break;
      }
    }
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return processedDate */
  public int getProcessedDate() {

    return processedDate;
  }

  /** @param processedDate the processedDate to set */
  public void setProcessedDate(int processedDate) {

    this.processedDate = processedDate;
  }

  /** @return customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }
}
