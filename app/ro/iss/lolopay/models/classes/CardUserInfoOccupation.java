package ro.iss.lolopay.models.classes;

public enum CardUserInfoOccupation {
  PUBLIC_SERVANT("Public servant / Police / Military"),
  AGRICULTURE("Agriculture"),
  CRAFTWORK_TRADE("Craftwork / Trade"),
  ARTS_CULTURE_SPORT("Arts / Culture / Sport"),
  BANKING_INSURANCE("Banking / Insurance / Finance / Auditing"),
  CONSTRUCTION_PUBLICWORKS("Construction / Publicworks"),
  EDUCATION("Education"),
  MANUFACTURING_MAINTENANCE("Manufacturing / Maintenance"),
  MEDICAL_PARAMEDICAL("Medical / Paramedical"),
  FOOD_WFH_HOSPITALITY("Food industry / Work from home / Hospitality / Tourism"),
  SERVICES_IT("Services / IT"),
  SOCIAL_SECURITY_NGO("Social Security / NGO"),
  POLITICIAN("Politician / Elected Member of Parliament");

  public final String label;

  public static final String FIELD_NAME = "Occupation";
  /** @return the label */
  public String getLabel() {

    return label;
  }

  private CardUserInfoOccupation(String label) {

    this.label = label;
  }

  public static CardUserInfoOccupation getEnumByLabel(String label) {

    for (CardUserInfoOccupation e : CardUserInfoOccupation.values()) {
      if (e.label.equalsIgnoreCase(label)) {
        return e;
      }
    }
    return null;
  }
}
