package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.BankCardStatus;

public class IsBankCardStatus extends EnumValidator<BankCardStatus> {

  public IsBankCardStatus() {

    super(BankCardStatus.class);
  }
}
