package ro.iss.lolopay.responses;

public class ResponseDummy extends RestResponseBody {
  private String response;

  /** @return the response */
  public String getResponse() {

    return response;
  }

  /** @param response the response to set */
  public void setResponse(String response) {

    this.response = response;
  }
}
