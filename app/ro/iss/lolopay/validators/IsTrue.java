package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsTrue extends Validator<Boolean> {

  @Override
  public boolean isValid(Boolean object) {

    // if null is sent we return true - the rule is that we only validate what exists
    if (object == null) {
      return true;
    }

    // we return true only if the object is not null and it has it's value True
    // FIXME: the above comment is false, it returns true for Boolean.FALSE also
    if ("true".equalsIgnoreCase(object.toString()) || "false".equalsIgnoreCase(object.toString())) {
      return true;
    }

    // we return false in any other way
    return false;
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when you reference the
    // annotation for validation
    return null;
  }
}
