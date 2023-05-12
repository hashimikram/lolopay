package ro.iss.lolopay.responses;

public class ResponseCardExpiryDate extends RestResponseBody {
  private String expiryDate;

  /** @return the expiryDate */
  public String getExpiryDate() {

    return expiryDate;
  }

  /** @param expiryDate the expiryDate to set */
  public void setExpiryDate(String expiryDate) {

    this.expiryDate = expiryDate;
  }
}
