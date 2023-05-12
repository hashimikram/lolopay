package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.Wallet;

public class ResponseWallets extends RestResponseBody {
  private List<Wallet> wallets;

  /** @return the wallets */
  public List<Wallet> getWallets() {

    return wallets;
  }

  /** @param wallets the wallets to set */
  public void setWallets(List<Wallet> wallets) {

    this.wallets = wallets;
  }
}
