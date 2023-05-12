package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.FeeModel;

public class IsFeeModel extends EnumValidator<FeeModel> {

  public IsFeeModel() {

    super(FeeModel.class);
  }
}
