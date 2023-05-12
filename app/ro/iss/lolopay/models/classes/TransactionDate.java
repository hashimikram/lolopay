package ro.iss.lolopay.models.classes;

public class TransactionDate {

  private CurrencyISO currency;

  private long startDate;

  private long endDate;

  public TransactionDate() {

    super();
  }

  public TransactionDate(CurrencyISO currency, long endDate) {

    super();
    this.currency = currency;
    this.endDate = endDate;
  }

  /**
   * @param currency
   * @param startDate
   * @param endDate
   */
  public TransactionDate(CurrencyISO currency, long startDate, long endDate) {

    super();
    this.currency = currency;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /** @return the currency */
  public CurrencyISO getCurrency() {

    return currency;
  }

  /** @param currency the currency to set */
  public void setCurrency(CurrencyISO currency) {

    this.currency = currency;
  }

  /** @return the startDate */
  public long getStartDate() {

    return startDate;
  }

  /** @param startDate the startDate to set */
  public void setStartDate(long startDate) {

    this.startDate = startDate;
  }

  /** @return the endDate */
  public long getEndDate() {

    return endDate;
  }

  /** @param endDate the endDate to set */
  public void setEndDate(long endDate) {

    this.endDate = endDate;
  }
}
