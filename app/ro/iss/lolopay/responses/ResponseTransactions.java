package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.Transaction;

public class ResponseTransactions extends RestResponseBody {
  private List<Transaction> transactions;

  /** @return the transactions */
  public List<Transaction> getTransactions() {

    return transactions;
  }

  /** @param transactions the transactions to set */
  public void setTransactions(List<Transaction> transactions) {

    this.transactions = transactions;
  }
}
