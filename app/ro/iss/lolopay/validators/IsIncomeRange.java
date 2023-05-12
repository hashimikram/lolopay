package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.IncomeRange;

public class IsIncomeRange extends EnumValidator<IncomeRange> {

  public IsIncomeRange() {

    super(IncomeRange.class);
  }
}
