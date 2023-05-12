package ro.iss.lolopay.responses;

import ro.iss.lolopay.classes.CardRegistration;

public class ResponseCreateCardRegistration extends RestResponseBody {
  private CardRegistration cardRegistration;

  /** @return the cardRegistration */
  public CardRegistration getCardRegistration() {

    return cardRegistration;
  }

  /** @param cardRegistration the cardRegistration to set */
  public void setCardRegistration(CardRegistration cardRegistration) {

    this.cardRegistration = cardRegistration;
  }
}
