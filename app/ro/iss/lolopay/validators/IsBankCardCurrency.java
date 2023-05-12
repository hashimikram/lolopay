package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsBankCardCurrency extends Validator<String> {

  @Override
  public boolean isValid(String object) {

    if (object == null) {
      return true;
    }

    if (object.toString().equals("EUR")) {
      return true;
    }

    if (object.toString().equals("USD")) {
      return true;
    }

    if (object.toString().equals("GBP")) {
      return true;
    }

    return false;
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when you reference the
    // annotation for validation
    return null;
  }
}
