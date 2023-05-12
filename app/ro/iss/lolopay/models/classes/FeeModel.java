package ro.iss.lolopay.models.classes;

public enum FeeModel {
  /**
   * Fee is included in sent amount (Eg: receiver gets 97.5 instead of 100, and transaction fee of
   * 2.5 when fee is 2.5%)
   */
  INCLUDED,

  /**
   * Fee is not included in sent amount, and is calculated separately (Eg: receiver gets 100, and
   * sender pay additional transaction fee of 2.5 when fee is 2.5%)
   */
  NOT_INCLUDED
}
