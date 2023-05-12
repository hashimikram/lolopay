package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsDouble extends Validator<String> {

  @Override
  public boolean isValid(String object) {

    if (object == null) {
      return true;
    }

    if (object.toString().equals("")) {
      return true;
    }

    try {
      Double.valueOf(object);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when
    // you reference the annotation for validation
    return null;
  }
}
