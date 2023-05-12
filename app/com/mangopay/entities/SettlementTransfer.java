package com.mangopay.entities;

import com.google.gson.annotations.SerializedName;
import com.mangopay.core.EntityBase;
import com.mangopay.core.Money;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** Settlement transfer entity. */
public class SettlementTransfer extends EntityBase {

  /** The Id of the author of the original PayIn that was repudiated. */
  @SerializedName("AuthorId")
  private String authorId;

  /** The funds debited from the debited wallet. */
  @SerializedName("DebitedFunds")
  private Money debitedFunds;

  /**
   * The amount you wish to charge for this settlement. This can be equal to 0, or more than 0 to
   * charge for the settlement or less than 0 to refund some of the original Fees that were taken on
   * the original settlement (eg. DebitedFunds of 1000 and Fees of -200 will transfer 800 from the
   * original wallet to the credit wallet, and transfer 200 from your Fees wallet to your Credit
   * wallet.
   */
  @SerializedName("Fees")
  private Money fees;

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public Money getDebitedFunds() {
    return debitedFunds;
  }

  public void setDebitedFunds(Money debitedFunds) {
    this.debitedFunds = debitedFunds;
  }

  public Money getFees() {
    return fees;
  }

  public void setFees(Money fees) {
    this.fees = fees;
  }

  /**
   * Gets map which property is an object and what type of object.
   *
   * @return Collection of field name-field type pairs.
   */
  @Override
  public Map<String, Type> getSubObjects() {

    return new HashMap<String, Type>() {
      {
        put("DebitedFunds", Money.class);
        put("Fees", Money.class);
      }
    };
  }
}
