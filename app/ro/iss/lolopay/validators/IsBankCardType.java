package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.BankCardType;

public class IsBankCardType extends EnumValidator<BankCardType> {

  public IsBankCardType() {

    super(BankCardType.class);
  }
}
