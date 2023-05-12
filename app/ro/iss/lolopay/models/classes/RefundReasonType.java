package ro.iss.lolopay.models.classes;

public enum RefundReasonType {
  INITIALIZED_BY_CLIENT,
  BANKACCOUNT_INCORRECT,
  OWNER_DOT_NOT_MATCH_BANKACCOUNT,
  BANKACCOUNT_HAS_BEEN_CLOSED,
  WITHDRAWAL_IMPOSSIBLE_ON_SAVINGS_ACCOUNTS,
  OTHER
}