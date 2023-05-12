package ro.iss.lolopay.validators;

import ro.iss.lolopay.models.classes.DocumentType;

public class IsDocumentType extends EnumValidator<DocumentType> {

  public IsDocumentType() {

    super(DocumentType.class);
  }
}
