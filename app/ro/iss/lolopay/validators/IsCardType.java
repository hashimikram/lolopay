package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.CardType;

public class IsCardType extends EnumValidator<CardType> {

  public IsCardType() {

    super(CardType.class);
  }
}
