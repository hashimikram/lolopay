package com.mangopay.core;

import com.google.gson.annotations.SerializedName;
import com.mangopay.core.enumerations.CurrencyIso;

/** Class represents money value with currency. */
public class Money extends Dto {

  /** Currency code in ISO 4217 standard. */
  @SerializedName("Currency")
  private CurrencyIso currency;

  /** The currency amount of money, in cents */
  @SerializedName("Amount")
  private int amount;

  public CurrencyIso getCurrency() {
    return currency;
  }

  public void setCurrency(CurrencyIso currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }
}
