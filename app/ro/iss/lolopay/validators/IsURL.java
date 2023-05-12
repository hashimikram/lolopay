package ro.iss.lolopay.validators;

import org.apache.commons.validator.routines.UrlValidator;
import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsURL extends Validator<String> {

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

    // define allowed schemes
    String[] schemes = {"http", "https"};

    // init URL validator
    UrlValidator urlValidator = new UrlValidator(schemes);

    // test input
    if (urlValidator.isValid(object)) {
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
