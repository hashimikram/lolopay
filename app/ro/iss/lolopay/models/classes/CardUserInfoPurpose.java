package ro.iss.lolopay.models.classes;

public enum CardUserInfoPurpose {
  RECEIVE_SOCIAL_WELFARE("Receive my social welfare payments"),
  RECEIVE_SALARY("Receive my salary"),
  PAY_BILLS("Pay my bills"),
  PERSONAL_EXPENSES("Personal expenses"),
  RECEIPT_BUSINESS_INCOME("Receipt of income from business"),
  SEND_OR_RECEIVE_MONEY_TRANSFER("Send or Receive Money transfer"),
  TRAVEL_EXPENSES("Travel expenses"),
  GAMBLE_OR_BET_ONLINE("Gamble or bet online"),
  PAY_IN_STORE("Pay in store"),
  SAVE_MONEY("Save money");

  public final String label;

  public static final String FIELD_NAME = "Card_Purpose";

  /** @return the label */
  public String getLabel() {

    return label;
  }

  private CardUserInfoPurpose(String label) {

    this.label = label;
  }

  public static CardUserInfoPurpose getEnumByLabel(String label) {

    for (CardUserInfoPurpose e : CardUserInfoPurpose.values()) {
      if (e.label.equalsIgnoreCase(label)) {
        return e;
      }
    }
    return null;
  }
}
