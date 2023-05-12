package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.DepositAccountType;

public class IsDepositAccountType extends EnumValidator<DepositAccountType> {

  public IsDepositAccountType() {

    super(DepositAccountType.class);
  }
}
