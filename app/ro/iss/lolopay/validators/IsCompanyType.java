package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.CompanyType;

public class IsCompanyType extends EnumValidator<CompanyType> {

  public IsCompanyType() {

    super(CompanyType.class);
  }
}
