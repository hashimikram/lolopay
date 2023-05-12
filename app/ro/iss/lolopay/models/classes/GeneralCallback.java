package ro.iss.lolopay.models.classes;

import java.util.HashMap;

public class GeneralCallback extends TableCollection {

  private String provider;

  private String accountId;

  private String applicationId;

  private String tag;

  private HashMap<String, Object> parameters;

  private int noFails = 0;

  /** @return the provider */
  public String getProvider() {

    return provider;
  }

  /** @param provider the provider to set */
  public void setProvider(String provider) {

    this.provider = provider;
  }

  /** @return the accountId */
  public String getAccountId() {

    return accountId;
  }

  /** @param accountId the accountId to set */
  public void setAccountId(String accountId) {

    this.accountId = accountId;
  }

  /** @return the applicationId */
  public String getApplicationId() {

    return applicationId;
  }

  /** @param applicationId the applicationId to set */
  public void setApplicationId(String applicationId) {

    this.applicationId = applicationId;
  }

  /** @return the tag */
  public String getTag() {

    return tag;
  }

  /** @param tag the tag to set */
  public void setTag(String tag) {

    this.tag = tag;
  }

  /** @return the parameters */
  public HashMap<String, Object> getParameters() {

    return parameters;
  }

  /** @param parameters the parameters to set */
  public void setParameters(HashMap<String, Object> parameters) {

    this.parameters = parameters;
  }

  /** @return the noFails */
  public int getNoFails() {

    return noFails;
  }

  /** @param noFails the noFails to set */
  public void setNoFails(int noFails) {

    this.noFails = noFails;
  }
}
