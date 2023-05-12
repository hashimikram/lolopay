package ro.iss.lolopay.validators;

import org.iban4j.BicFormatException;
import org.iban4j.BicUtil;
import org.iban4j.UnsupportedCountryException;
import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsBIC extends Validator<String> {

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

    // How to validate BIC
    try {
      BicUtil.validate(object);
      return true;
    } catch (BicFormatException | UnsupportedCountryException e) {
      return false;
    }
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when you reference the
    // annotation for validation
    return null;
  }
}
