package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.BankCard;

public class ResponseBankCard extends RestResponseBody {
  private BankCard bankCard;

  /** @return the card */
  public BankCard getBankCard() {

    return bankCard;
  }

  /** @param card the card to set */
  public void setBankCard(BankCard bankCard) {

    this.bankCard = bankCard;
  }
}
