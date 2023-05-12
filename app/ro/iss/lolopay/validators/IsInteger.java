package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;
import ro.iss.lolopay.enums.ApplicationConstants;

public class IsInteger extends Validator<Integer> {

  @Override
  public boolean isValid(Integer object) {

    if (object == null) {
      return true;
    }

    if (object.toString().equals("")) {
      return true;
    }

    if (object.toString().matches(ApplicationConstants.REGEX_VALIDATE_DIGITS)) {
      return true;
    }

    return false;
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when
    // you reference the annotation for validation
    return null;
  }
}
