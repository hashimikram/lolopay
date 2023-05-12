package shared;

public enum ProviderApis {
  MANGO_OAUTH("/v2.01/oauth/token"),
  MANGO_CARD_REGISTRATION_TOKEN("/webpayment/getToken"),
  MANGO_CREATE_NATURAL_USER("/v2.01/m3application5test/users/natural"),
  MANGO_CREATE_LEGAL_USER("/v2.01/m3application5test/users/legal"),
  MANGO_SAVE_LEGAL("/v2.01/m3application5test/users/legal/%s"),
  MANGO_GET_BANK_ACCOUNT("/v2.01/m3application5test/users/%s"),
  MANGO_CREATE_BANKACCOUNT_CA("/v2.01/m3application5test/users/%s/bankaccounts/ca"),
  MANGO_SAVE_NATURAL("/v2.01/m3application5test/users/natural/%s"),
  MANGO_CREATE_BANKACCOUNT_GB("/v2.01/m3application5test/users/%s/bankaccounts/gb"),
  MANGO_CREATE_BANKACCOUNT_IBAN("/v2.01/m3application5test/users/%s/bankaccounts/iban"),
  MANGO_CREATE_BANKACCOUNT_OTHER("/v2.01/m3application5test/users/%s/bankaccounts/other"),
  MANGO_CREATE_BANKACCOUNT_US("/v2.01/m3application5test/users/%s/bankaccounts/us"),
  MANGO_CREATE_BANKACCOUNT_DEACTIVATE("/v2.01/m3application5test/users/%1$s/bankaccounts/%2$s"),
  MANGO_GET_DISPUTES("/v2.01/m3application5test/users/%s/disputes"),
  MANGO_SUBMIT_DOCUMENT_GET("/v2.01/m3application5test/KYC/documents/%s"),
  MANGO_SUBMIT_DOCUMENT("/v2.01/m3application5test/users/%s/KYC/documents/%s"),
  MANGO_CREATE_DOCUMENT_PAGE("/v2.01/m3application5test/users/%s/KYC/documents/%s/pages"),
  MANGO_CREATE_WALLET("/v2.01/m3application5test/wallets"),
  MANGO_CREATE_CARDREGISTRATION("/v2.01/m3application5test/cardregistrations"),
  MANGO_UPDATE_CARDREGISTRATION("/v2.01/m3application5test/cardregistrations/%s"),
  MANGO_GET_CARDREGISTRATION("/v2.01/m3application5test/cardregistrations/card/%s"),
  MANGO_VIEW_CARD("/v2.01/m3application5test/cards/%s"),
  MANGO_CREATE_TRANSACTION_DIRECTPAYIN("/v2.01/m3application5test/payins/card/direct/"),
  MANGO_CREATE_TRANSACTION_PAYIN("/v2.01/m3application5test/payins/card/web/"),
  MANGO_CREATE_TRANSFER("/v2.01/m3application5test/transfers"),
  MANGO_VIEW_TRANSFER("/v2.01/m3application5test/transfers/%s"),
  MANGO_REFUND_TRANSFER("/v2.01/m3application5test/transfers/%s/refunds"),
  MANGO_VIEW_REFUND("/v2.01/m3application5test/refunds/%s"),
  MANGO_CREATE_DOCUMENT("/v2.01/m3application5test/users/%s/KYC/documents/"),
  MANGO_REFUND_PAYIN("/v2.01/m3application5test/payins/%s/refunds"),
  MANGO_GET_PAYINS("/v2.01/m3application5test/payins/%s"),
  MANGO_CREATE_PAYOUT("/v2.01/m3application5test/payouts/bankwire/"),
  MANGO_VIEW_PAYOUT("/v2.01/m3application5test/payouts/%s"),
  MANGO_CREATE_UBO_DECLARATION("/v2.01/m3application5test/users/%s/kyc/ubodeclarations"),
  MANGO_SUBMIT_UBO_DECLARATION("/v2.01/m3application5test/users/%s/kyc/ubodeclarations/%s"),
  MANGO_CREATE_UBO("/v2.01/m3application5test/users/%s/kyc/ubodeclarations/%s/ubos"),
  MANGO_UPDATE_UBO("/v2.01/m3application5test/users/%s/kyc/ubodeclarations/%s/ubos/%s"),
  PFS_CREATE_CARD("/cardIssue"),
  PFS_ADD_CARD_CURRENCY("/addCardCurrency"),
  PFS_CHANGE_STATUS("/changeCardStatus"),
  PFS_UPGRADE_CARD("/updateCard"),
  PFS_REPLACE_CARD("/replaceCard"),
  PFS_GET_FX_QUOTE("/currencyFXQuote"),
  PFS_GET_CARD_WALLET("/getCardBalance"),
  PFS_GET_CARD_WALLET_TRANSACTIONS("/viewStatement"),
  PFS_GET_CARD_EXPIRY_DATE("/getExpDate"),
  PFS_GET_CARD_CVV("/getCvv"),
  PFS_LOCK_UNLOCK_CARD("/lockUnlock"),
  PFS_TRANSFER_TO("/depositToCard"),
  PFS_TRANSFER_FROM("/purchaseOnUs"),
  PFS_BANK_PAYMENT("/bankPayment"),
  PFS_GET_CARD_NUMBER("/getCardNumber");

  private String uri;

  /** @param text */
  ProviderApis(String uri) {
    this.uri = uri;
  }

  /** @return the uri */
  public String getUri() {
    return this.uri;
  }
}
