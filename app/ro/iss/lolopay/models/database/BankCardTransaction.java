package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.BankCardTransactionDirection;
import ro.iss.lolopay.models.classes.BankCardTransactionOrigin;
import ro.iss.lolopay.models.classes.BankCardTransactionStatus;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "bankCardTransactions", noClassnameStored = true)
public class BankCardTransaction extends TableCollection {
  private int recordNo;

  private String transactionId;

  @Indexed private String bankCardProviderId;

  private BankCardTransactionStatus transactionStatus;

  @Indexed private long date;

  @Indexed private CurrencyISO currency;

  private Amount availableBalance;

  private Amount ledgerBalance;

  private Amount amount;

  private Amount fee;

  private String description;

  private Integer originalAmount;

  private String originalCurrency;

  private String terminalName;

  private String terminalCity;

  private String terminalCountry;

  /** @return the transactionId */
  public String getTransactionId() {

    return transactionId;
  }

  /** @param transactionId the transactionId to set */
  public void setTransactionId(String transactionId) {

    this.transactionId = transactionId;
  }

  /** API or ATM/POS */
  private BankCardTransactionOrigin origin;

  /**
   * CREDIT/DEBIT - Use MT field to see if an amount was a debit or a credit on the card wallet: -
   * if MTI starts with "15" --> the trx is a debit - if MTI starts with "27" --> the trx is a
   * credit
   */
  private BankCardTransactionDirection direction;

  /** @return the recordNo */
  public int getRecordNo() {

    return recordNo;
  }

  /** @param recordNo the recordNo to set */
  public void setRecordNo(int recordNo) {

    this.recordNo = recordNo;
  }

  /** @return the date */
  public long getDate() {

    return date;
  }

  /** @param date the date to set */
  public void setDate(long date) {

    this.date = date;
  }

  /** @return the availableBalance */
  public Amount getAvailableBalance() {

    return availableBalance;
  }

  /** @param availableBalance the availableBalance to set */
  public void setAvailableBalance(Amount availableBalance) {

    this.availableBalance = availableBalance;
  }

  /** @return the ledgerBalance */
  public Amount getLedgerBalance() {

    return ledgerBalance;
  }

  /** @param ledgerBalance the ledgerBalance to set */
  public void setLedgerBalance(Amount ledgerBalance) {

    this.ledgerBalance = ledgerBalance;
  }

  /** @return the amount */
  public Amount getAmount() {

    return amount;
  }

  /** @param amount the amount to set */
  public void setAmount(Amount amount) {

    this.amount = amount;
  }

  /** @return the fee */
  public Amount getFee() {

    return fee;
  }

  /** @param fee the fee to set */
  public void setFee(Amount fee) {

    this.fee = fee;
  }

  /** @return the description */
  public String getDescription() {

    return description;
  }

  /** @param description the description to set */
  public void setDescription(String description) {

    this.description = description;
  }

  /** @return the origin */
  public BankCardTransactionOrigin getOrigin() {

    return origin;
  }

  /** @param origin the origin to set */
  public void setOrigin(BankCardTransactionOrigin origin) {

    this.origin = origin;
  }

  /** @return the direction */
  public BankCardTransactionDirection getDirection() {

    return direction;
  }

  /** @param direction the direction to set */
  public void setDirection(BankCardTransactionDirection direction) {

    this.direction = direction;
  }

  /** @return the bankCardProviderId */
  public String getBankCardProviderId() {

    return bankCardProviderId;
  }

  /** @param bankCardProviderId the bankCardProviderId to set */
  public void setBankCardProviderId(String bankCardId) {

    this.bankCardProviderId = bankCardId;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }

  public Integer getOriginalAmount() {

    return originalAmount;
  }

  public void setOriginalAmount(Integer origTransAmt) {

    this.originalAmount = origTransAmt;
  }

  public String getOriginalCurrency() {

    return originalCurrency;
  }

  public void setOriginalCurrency(String termCurrency) {

    this.originalCurrency = termCurrency;
  }

  /** @return the terminal name */
  public String getTerminalName() {

    return terminalName;
  }

  /** @param terminalName the terminal name to set */
  public void setTerminalName(String terminalName) {

    this.terminalName = terminalName;
  }

  /** @return the terminal city */
  public String getTerminalCity() {

    return terminalCity;
  }

  /** @param terminalCity the terminal city to set */
  public void setTerminalCity(String terminalCity) {

    this.terminalCity = terminalCity;
  }

  /** @return the terminal country */
  public String getTerminalCountry() {

    return terminalCountry;
  }

  /** @param terminalCountry the terminal country to set */
  public void setTerminalCountry(String terminalCountry) {

    this.terminalCountry = terminalCountry;
  }

  /** @return the transaction status */
  public BankCardTransactionStatus getTransactionStatus() {

    return transactionStatus;
  }

  /** @param transactionStatus the transaction status to set */
  public void setTransactionStatus(BankCardTransactionStatus transactionStatus) {

    this.transactionStatus = transactionStatus;
  }
}
