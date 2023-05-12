package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.DepositCard;

public class ResponseGetDepositCard extends RestResponseBody {
  private DepositCard depositCard;

  /** @return the DepositCard */
  public DepositCard getDepositCard() {

    return depositCard;
  }

  /** @param depositCard the depositCard to set */
  public void setDepositCard(DepositCard depositCard) {

    this.depositCard = depositCard;
  }
}
