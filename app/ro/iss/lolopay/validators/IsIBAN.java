package ro.iss.lolopay.validators;

import org.iban4j.IbanFormat;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsIBAN extends Validator<String> {

  @Override
  public boolean isValid(String iban) {

    // if null is sent we return true - the rule is that we only validate what exists
    if (iban == null) {
      return true;
    }

    // if empty we return true - the rule is that we only validate what exists
    if (iban.equals("")) {
      return true;
    }

    try {
      iban = iban.replaceAll("[^A-Za-z0-9]+", "").toUpperCase();
      IbanUtil.validate(iban, IbanFormat.None);
      return true;
    } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
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
