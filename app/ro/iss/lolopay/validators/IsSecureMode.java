package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.SecureMode;

public class IsSecureMode extends EnumValidator<SecureMode> {

  public IsSecureMode() {

    super(SecureMode.class);
  }
}
