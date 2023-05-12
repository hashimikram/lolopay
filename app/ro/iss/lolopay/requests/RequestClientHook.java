package ro.iss.lolopay.requests;

import ro.iss.lolopay.enums.NotificationType;

public class RequestClientHook extends RestRequest {
  private String applicationId;

  private String applicationName;

  NotificationType notificationType;

  String resourceId;

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

  /** @return the notificationType */
  public NotificationType getNotificationType() {

    return notificationType;
  }

  /** @param notificationType the notificationType to set */
  public void setNotificationType(NotificationType notificationType) {

    this.notificationType = notificationType;
  }

  /** @return the resourceId */
  public String getResourceId() {

    return resourceId;
  }

  /** @param resourceId the resourceId to set */
  public void setResourceId(String resourceId) {

    this.resourceId = resourceId;
  }
}
