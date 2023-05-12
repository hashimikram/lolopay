package ro.iss.lolopay.models.classes;

public enum CardUserInfoEstate {
  BELOW_20K_EUR("LESS THAN 20 000 EUR"),
  BELOW_50K_EUR("20 000 to 50 000 EUR"),
  BELOW_75K_EUR("50 001 to 75 000 EUR"),
  BELOW_100K_EUR("75 001 to 100 000 EUR"),
  BELOW_250K_EUR("100 001 to 250 000 EUR"),
  BELOW_500K_EUR("250 001 to 500 000 EUR"),
  BELOW_1M_EUR("500 001 to 1M EUR"),
  ABOVE_1M_EUR("MORE THAN 1M EUR");

  public final String label;

  public static final String FIELD_NAME = "Estate";

  /** @return the label */
  public String getLabel() {

    return label;
  }

  private CardUserInfoEstate(String label) {

    this.label = label;
  }

  public static CardUserInfoEstate getEnumByLabel(String label) {

    for (CardUserInfoEstate e : CardUserInfoEstate.values()) {
      if (e.label.equalsIgnoreCase(label)) {
        return e;
      }
    }
    return null;
  }
}
