package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "sessions", noClassnameStored = true)
public class Session extends TableCollection {
  @Indexed private String applicationId;

  private String accountId;

  private Long expiryDate;

  /** @return the applicationId */
  public String getApplicationId() {

    return applicationId;
  }

  /** @param applicationId the applicationId to set */
  public void setApplicationId(String applicationId) {

    this.applicationId = applicationId;
  }

  /** @return the accountId */
  public String getAccountId() {

    return accountId;
  }

  /** @param accountId the accountId to set */
  public void setAccountId(String accountId) {

    this.accountId = accountId;
  }

  /** @return the expiryDate */
  public Long getExpiryDate() {

    return expiryDate;
  }

  /** @param expiryDate the expiryDate to set */
  public void setExpiryDate(Long expiryDate) {

    this.expiryDate = expiryDate;
  }
}
