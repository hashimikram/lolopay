package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.BankCardTransaction;

public class ResponseBankCardWalletTransactions extends RestResponseBody {
  private List<BankCardTransaction> bankCardTransactions;

  /** @return the bankCardTransaction */
  public List<BankCardTransaction> getBankCardTransactions() {

    return bankCardTransactions;
  }

  /** @param bankCardTransaction the bankCardTransaction to set */
  public void setBankCardTransactions(List<BankCardTransaction> bankCardTransactions) {

    this.bankCardTransactions = bankCardTransactions;
  }
}
