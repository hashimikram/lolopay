package ro.iss.lolopay.responses;

import ro.iss.lolopay.classes.ExecuteCardWalletsTrade;

public class ResponseExecuteCardWalletsTrade extends RestResponseBody {
  ExecuteCardWalletsTrade executeCardWalletsTrade;

  /** @return the executeCardWalletsTrade */
  public ExecuteCardWalletsTrade getExecuteCardWalletsTrade() {

    return executeCardWalletsTrade;
  }

  /** @param executeCardWalletsTrade the executeCardWalletsTrade to set */
  public void setExecuteCardWalletsTrade(ExecuteCardWalletsTrade executeCardWalletsTrade) {

    this.executeCardWalletsTrade = executeCardWalletsTrade;
  }
}
