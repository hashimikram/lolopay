package ro.iss.lolopay.responses;

public class ResponseCardCvv extends RestResponseBody {
  private String cvv;

  /** @return the cvv */
  public String getCvv() {

    return cvv;
  }

  /** @param cvv the cvv to set */
  public void setCvv(String cvv) {

    this.cvv = cvv;
  }
}
