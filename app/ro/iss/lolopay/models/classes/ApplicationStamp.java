package ro.iss.lolopay.models.classes;

public class ApplicationStamp {
  private String applicationId;

  private String applicationName;

  private String applicationEmail;

  /** @return the applicationId */
  public String getApplicationId() {

    return applicationId;
  }

  /** @param applicationId the applicationId to set */
  public void setApplicationId(String applicationId) {

    this.applicationId = applicationId;
  }

  public String getApplicationName() {

    return applicationName;
  }

  public void setApplicationName(String applicationName) {

    this.applicationName = applicationName;
  }

  public String getApplicationEmail() {

    return applicationEmail;
  }

  public void setApplicationEmail(String applicationEmail) {

    this.applicationEmail = applicationEmail;
  }
}
