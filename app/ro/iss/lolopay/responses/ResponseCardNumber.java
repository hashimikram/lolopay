package ro.iss.lolopay.responses;

public class ResponseCardNumber extends RestResponseBody {
  private String cardNumber;

  /** @return the cardNumber */
  public String getCardNumber() {

    return cardNumber;
  }

  /** @param cardNumber the cardNumber to set */
  public void setCardNumber(String cardNumber) {

    this.cardNumber = cardNumber;
  }
}
