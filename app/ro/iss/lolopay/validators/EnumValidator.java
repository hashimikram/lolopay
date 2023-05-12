package ro.iss.lolopay.validators;

import play.data.validation.Constraints.Validator;
import play.libs.F.Tuple;

public class EnumValidator<E extends Enum<E>> extends Validator<String> {

  private final Class<E> enumType;

  public EnumValidator(Class<E> enumType) {

    // need this because in java we cannot get the runtime class from generic types
    this.enumType = enumType;
  }

  @Override
  public boolean isValid(String object) {

    if (object == null) {
      return true;
    }

    if (object instanceof String) {
      if (object.isEmpty()) {
        return true;
      }
    }

    try {
      Enum.valueOf(enumType, object);
      return true;
    } catch (Exception e) {
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
