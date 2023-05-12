package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;
import ro.iss.lolopay.models.classes.CountryISO;

public class IsCountryISO extends Validator<String> {

  @Override
  public boolean isValid(String object) {

    if (object == null) {
      return true;
    }

    for (CountryISO c : CountryISO.values()) {
      if (c.name().equals(object)) {
        return true;
      }
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
