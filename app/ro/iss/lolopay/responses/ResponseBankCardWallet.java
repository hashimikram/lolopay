package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.BankCardWallet;

public class ResponseBankCardWallet extends RestResponseBody {
  private BankCardWallet bankCardWallet;

  /** @return the bankCardWallet */
  public BankCardWallet getBankCardWallet() {

    return bankCardWallet;
  }

  /** @param bankCardWallet the bankCardWallet to set */
  public void setBankCardWallet(BankCardWallet bankCardWallet) {

    this.bankCardWallet = bankCardWallet;
  }
}
