package ro.iss.lolopay.validators;

import java.util.Calendar;
import java.util.Date;
import com.typesafe.config.ConfigFactory;
import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class IsBirthDate extends Validator<Long> {
  private Date acum100DeAni() {

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -100);
    return cal.getTime();
  }

  private Date acum18DeAni() {

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -18);
    return cal.getTime();
  }

  private Date acum() {

    Calendar cal = Calendar.getInstance();
    return cal.getTime();
  }

  @Override
  public boolean isValid(Long object) {

    // if null is sent we return true - the rule is that we only validate what exists
    if (object == null) return true;

    // assign Long object which is supposed to be a valid time stamp and we verify it
    long varToValidate = (long) object;

    // if converted long birth date is 0 we fail validations
    // this is commented because causes are created with birth date 0
    // if (varToValidate == 0) return false;

    // create a date from time stamp
    Date dateToValidate = new Date(varToValidate * 1000L);

    // if long number is smaller than current time minus 100 years - old people are banned
    if (dateToValidate.before(acum100DeAni())) return false;

    // if long number is bigger than now, we also have a problem
    if (dateToValidate.after(acum())) return false;

    // check id minors are allowed
    if (ConfigFactory.load().getBoolean("application.allowUsersBefore18")) {
      if (dateToValidate.after(acum18DeAni())) return false;
    }

    return true;
  }

  @Override
  public Tuple<String, Object[]> getErrorMessageKey() {

    // Do NOT implement this, add the message variable on the property when you reference the
    // annotation for validation
    return null;
  }
}
