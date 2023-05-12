package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "applicationsActivities", noClassnameStored = true)
public class ApplicationActivity extends TableCollection {
  private Long lastActivity;

  private String lastUsedIp;

  private Long lastLogIn;

  private Long lastRefresh;

  private String lastUserAgent;

  /** @return the lastActivity */
  public Long getLastActivity() {

    return lastActivity;
  }

  /** @param lastActivity the lastActivity to set */
  public void setLastActivity(Long lastActivity) {

    this.lastActivity = lastActivity;
  }

  /** @return the lastUsedIp */
  public String getLastUsedIp() {

    return lastUsedIp;
  }

  /** @param lastUsedIp the lastUsedIp to set */
  public void setLastUsedIp(String lastUsedIp) {

    this.lastUsedIp = lastUsedIp;
  }

  /** @return the lastLogIn */
  public Long getLastLogIn() {

    return lastLogIn;
  }

  /** @param lastLogIn the lastLogIn to set */
  public void setLastLogIn(Long lastLogIn) {

    this.lastLogIn = lastLogIn;
  }

  /** @return the lastRefresh */
  public Long getLastRefresh() {

    return lastRefresh;
  }

  /** @param lastRefresh the lastRefresh to set */
  public void setLastRefresh(Long lastRefresh) {

    this.lastRefresh = lastRefresh;
  }

  /** @return the lastUserAgent */
  public String getLastUserAgent() {

    return lastUserAgent;
  }

  /** @param lastUserAgent the lastUserAgent to set */
  public void setLastUserAgent(String lastUserAgent) {

    this.lastUserAgent = lastUserAgent;
  }
}
