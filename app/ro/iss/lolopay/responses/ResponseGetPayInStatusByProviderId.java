package ro.iss.lolopay.responses;

public class ResponseGetPayInStatusByProviderId extends RestResponseBody {
  private String status;

  /** @return the status */
  public String getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(String status) {

    this.status = status;
  }
}
