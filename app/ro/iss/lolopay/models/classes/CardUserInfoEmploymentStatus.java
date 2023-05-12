package ro.iss.lolopay.models.classes;

public enum CardUserInfoEmploymentStatus {
  DIRECTOR_OWNER("Director / Owner"),
  EXECUTIVE("Executive"),
  MANAGER("Manager"),
  EMPLOYEE_WORKER("Employee / Worker"),
  SELF_EMPLOYED("Self employed"),
  STUDENT("Student"),
  RETIREE("Retiree"),
  UNEMPLOYED("Unemployed");

  public final String label;

  public static final String FIELD_NAME = "Occupation_Profession";

  /** @return the label */
  public String getLabel() {

    return label;
  }

  private CardUserInfoEmploymentStatus(String label) {

    this.label = label;
  }

  public static CardUserInfoEmploymentStatus getEnumByLabel(String label) {

    for (CardUserInfoEmploymentStatus e : CardUserInfoEmploymentStatus.values()) {
      if (e.label.equalsIgnoreCase(label)) {
        return e;
      }
    }
    return null;
  }
}
