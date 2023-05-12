package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "companyBankCardTransactions", noClassnameStored = true)
public class CompanyBankCardTransaction extends TableCollection {
  @Indexed private String companyBankCardWalletId;

  private String cardholderId;

  private String amount;

  private String currency;

  private String transactionId;

  private String trantype;

  private String balance;

  /** @return the companyBankCardWalletId */
  public String getCompanyBankCardWalletId() {

    return companyBankCardWalletId;
  }

  /** @param companyBankCardWalletId the companyBankCardWalletId to set */
  public void setCompanyBankCardWalletId(String companyBankCardWalletId) {

    this.companyBankCardWalletId = companyBankCardWalletId;
  }

  /** @return the cardholderId */
  public String getCardholderId() {

    return cardholderId;
  }

  /** @param cardholderId the cardholderId to set */
  public void setCardholderId(String cardholderId) {

    this.cardholderId = cardholderId;
  }

  /** @return the amount */
  public String getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(String amount) {

    this.amount = amount;
  }

  /** @return the currency */
  public String getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  /** @return the transactionId */
  public String getTransactionId() {

    return transactionId;
  }

  /** @param transactionId the transactionId to set */
  public void setTransactionId(String transactionId) {

    this.transactionId = transactionId;
  }

  /** @return the trantype */
  public String getTrantype() {

    return trantype;
  }

  /** @param trantype the trantype to set */
  public void setTrantype(String trantype) {

    this.trantype = trantype;
  }

  /** @return the balance */
  public String getBalance() {

    return balance;
  }

  /** @param balance the balance to set */
  public void setBalance(String balance) {

    this.balance = balance;
  }
}
