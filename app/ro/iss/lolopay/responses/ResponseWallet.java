/** */
package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.Wallet;

public class ResponseWallet extends RestResponseBody {
  private Wallet wallet;

  /** @return the wallet */
  public Wallet getWallet() {

    return wallet;
  }

  /** @param wallet the wallet to set */
  public void setWallet(Wallet wallet) {

    this.wallet = wallet;
  }
}
