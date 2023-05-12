package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "requestHistory", noClassnameStored = true)
public class RequestHistory extends TableCollection {
  @Indexed private String requestId;

  private String request;

  private String response;

  /** @return the requestId */
  public String getRequestId() {

    return requestId;
  }

  /** @param requestId the requestId to set */
  public void setRequestId(String requestId) {

    this.requestId = requestId;
  }

  /** @return the request */
  public String getRequest() {

    return request;
  }

  /** @param request the request to set */
  public void setRequest(String request) {

    this.request = request;
  }

  /** @return the response */
  public String getResponse() {

    return response;
  }

  /** @param response the response to set */
  public void setResponse(String response) {

    this.response = response;
  }
}
