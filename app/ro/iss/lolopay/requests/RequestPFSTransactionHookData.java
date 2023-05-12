package ro.iss.lolopay.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestPFSTransactionHookData {
  @JsonProperty("CARDHOLDERID")
  private String cardholderid;

  @JsonProperty("AMOUNT1")
  private String amount1;

  @JsonProperty("AMOUNT2")
  private String amount2;

  @JsonProperty("TRANAMT")
  private String tranAmount;

  @JsonProperty("MESSAGETYPE")
  private String messageType;

  @JsonProperty("TRANSCODE")
  private String transCode;

  @JsonProperty("TRANSLOGDATETIME")
  private String transLogDateTime;

  /** Currency of the card used */
  @JsonProperty("TERMCURRENCY")
  private String termCurrency;

  /** Currency of the transaction */
  @JsonProperty("CHCODE")
  private String currencyCode;

  /** @return the cardholderid */
  public String getCardholderid() {

    return cardholderid;
  }

  /** @param cardholderid the cardholderid to set */
  public void setCardholderid(String cardholderid) {

    this.cardholderid = cardholderid;
  }

  /** @return the termCurrency */
  public String getTermCurrency() {

    return termCurrency;
  }

  /** @param termCurrency the termCurrency to set */
  public void setTermCurrency(String currencyCode) {

    this.termCurrency = currencyCode;
  }

  /** @return the transLogDateTime */
  public String getTransLogDateTime() {

    return transLogDateTime;
  }

  /** @param transLogDateTime the transLogDateTime to set */
  public void setTransLogDateTime(String transdatetime) {

    this.transLogDateTime = transdatetime;
  }

  /** @return the currencyCode */
  public String getCurrencyCode() {

    return currencyCode;
  }

  /** @param currencyCode the currencyCode to set */
  public void setCurrencyCode(String currencyCode) {

    this.currencyCode = currencyCode;
  }

  /** @return the amount2 */
  public String getAmount2() {

    return amount2;
  }

  /** @param amount2 the amount2 to set */
  public void setAmount2(String amount2) {

    this.amount2 = amount2;
  }

  /** @return the messageType */
  public String getMessageType() {

    return messageType;
  }

  /** @param messageType the messageType to set */
  public void setMessageType(String messageType) {

    this.messageType = messageType;
  }

  /** @return the amount1 */
  public String getAmount1() {

    return amount1;
  }

  /** @param amount1 the amount1 to set */
  public void setAmount1(String amount1) {

    this.amount1 = amount1;
  }

  /** @return the tranAmount */
  public String getTranAmount() {

    return tranAmount;
  }

  /** @param tranAmount the tranAmount to set */
  public void setTranAmount(String tranAmount) {

    this.tranAmount = tranAmount;
  }

  /** @return the transCode */
  public String getTransCode() {

    return transCode;
  }

  /** @param transCode the transCode to set */
  public void setTransCode(String transCode) {

    this.transCode = transCode;
  }
}
