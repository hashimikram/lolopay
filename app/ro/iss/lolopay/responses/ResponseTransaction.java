package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.Transaction;

public class ResponseTransaction extends RestResponseBody {
  private Transaction transaction;

  /** @return the transaction */
  public Transaction getTransaction() {

    return transaction;
  }

  /** @param transaction the transaction to set */
  public void setTransaction(Transaction transaction) {

    this.transaction = transaction;
  }
}
