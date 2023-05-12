package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.classes.ApplicationStatus;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "applications", noClassnameStored = true)
public class Application extends TableCollection {
  /** Application readable id */
  private String applicationId;

  /** Application name */
  private String applicationName;

  /** Application email where to send some emergency email's if the case */
  private String applicationEmail;

  /** Application password, used for api's connection */
  private String applicationPassword;

  /** Application status */
  private ApplicationStatus applicationStatus;

  /** Specify application hook where to post the results of API operations */
  private String applicationHookUrl;

  /** @return the applicationId */
  public String getApplicationId() {

    return applicationId;
  }

  /** @param applicationId the applicationId to set */
  public void setApplicationId(String applicationId) {

    this.applicationId = applicationId;
  }

  /** @return the applicationName */
  public String getApplicationName() {

    return applicationName;
  }

  /** @param applicationName the applicationName to set */
  public void setApplicationName(String applicationName) {

    this.applicationName = applicationName;
  }

  /** @return the applicationEmail */
  public String getApplicationEmail() {

    return applicationEmail;
  }

  /** @param applicationEmail the applicationEmail to set */
  public void setApplicationEmail(String applicationEmail) {

    this.applicationEmail = applicationEmail;
  }

  /** @return the applicationPassword */
  public String getApplicationPassword() {

    return applicationPassword;
  }

  /** @param applicationPassword the applicationPassword to set */
  public void setApplicationPassword(String applicationPassword) {

    this.applicationPassword = applicationPassword;
  }

  /** @return the applicationStatus */
  public ApplicationStatus getApplicationStatus() {

    return applicationStatus;
  }

  /** @param applicationStatus the applicationStatus to set */
  public void setApplicationStatus(ApplicationStatus applicationStatus) {

    this.applicationStatus = applicationStatus;
  }

  /** @return the applicationHookUrl */
  public String getApplicationHookUrl() {

    return applicationHookUrl;
  }

  /** @param applicationHookUrl the applicationHookUrl to set */
  public void setApplicationHookUrl(String applicationHookUrl) {

    this.applicationHookUrl = applicationHookUrl;
  }

  /** @return */
  public ApplicationStamp getApplicationStamp() {

    // create a user time stamp object
    ApplicationStamp applicationStamp = new ApplicationStamp();

    // check if this user was actually created in db and it has an id
    if (this.getId() != null) {
      // update id
      applicationStamp.setApplicationId(this.getId().toString());
    }

    // set other details
    applicationStamp.setApplicationName(this.getApplicationName());
    applicationStamp.setApplicationEmail(this.getApplicationEmail());
    return applicationStamp;
  }
}
