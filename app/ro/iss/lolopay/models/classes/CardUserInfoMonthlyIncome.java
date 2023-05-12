package ro.iss.lolopay.models.classes;

public enum CardUserInfoMonthlyIncome {
  BELOW_THAN_500_EUR("LESS THAN 500 EUR"),
  BELOW_1000_EUR("500 to 1000 EUR"),
  BELOW_1500_EUR("1001 to 1500 EUR"),
  BELOW_2000_EUR("1501 to 2000 EUR"),
  BELOW_3000_EUR("2001 to 3000 EUR"),
  ABOVE_3000_EUR("MORE THAN 3000 EUR");

  public final String label;

  public static final String FIELD_NAME = "Monthly_Income";

  /** @return the label */
  public String getLabel() {

    return label;
  }

  private CardUserInfoMonthlyIncome(String label) {

    this.label = label;
  }

  public static CardUserInfoMonthlyIncome getEnumByLabel(String label) {

    for (CardUserInfoMonthlyIncome e : CardUserInfoMonthlyIncome.values()) {
      if (e.label.equalsIgnoreCase(label)) {
        return e;
      }
    }
    return null;
  }
}
