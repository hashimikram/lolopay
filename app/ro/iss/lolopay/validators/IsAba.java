package ro.iss.lolopay.validators;

import org.apache.commons.validator.routines.checkdigit.ABANumberCheckDigit;
import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsAba extends Validator<String> {
  @Override
  public boolean isValid(String object) {

    // if null is sent we return true - the rule is that we only validate what exists
    if (object == null) {
      return true;
    }

    // if empty we return true - the rule is that we only validate what exists
    if (object.equals("")) {
      return true;
    }

    // validate content
    ABANumberCheckDigit ababaValidator = new ABANumberCheckDigit();
    return ababaValidator.isValid(object);
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when you reference the
    // annotation for validation
    return null;
  }
}
