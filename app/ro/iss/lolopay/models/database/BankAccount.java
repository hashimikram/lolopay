package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.BankAccountType;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "bankAccounts", noClassnameStored = true)
public class BankAccount extends TableCollection {
  /** Developer custom tag */
  private String customTag;

  /** A user's ID */
  @Indexed private String userId;

  /** Bank account type - structure of the bank account based on the country */
  private BankAccountType type;

  /** Bank account owner name */
  private String ownerName;

  /** Bank account owner address */
  private Address ownerAddress;

  /**
   * Bank account is active or not / the delete operation does not exists but deactivate is possible
   */
  private boolean active;

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
  public BankAccountType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(BankAccountType type) {

    this.type = type;
  }

  /** @return the ownerName */
  public String getOwnerName() {

    return ownerName;
  }

  /** @param ownerName the ownerName to set */
  public void setOwnerName(String ownerName) {

    this.ownerName = ownerName;
  }

  /** @return the ownerAddress */
  public Address getOwnerAddress() {

    return ownerAddress;
  }

  /** @param ownerAddress the ownerAddress to set */
  public void setOwnerAddress(Address ownerAddress) {

    this.ownerAddress = ownerAddress;
  }

  /** @return the active */
  public boolean isActive() {

    return active;
  }

  /** @param active the active to set */
  public void setActive(boolean active) {

    this.active = active;
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
