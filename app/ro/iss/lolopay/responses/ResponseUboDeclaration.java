package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.UboDeclaration;

public class ResponseUboDeclaration extends RestResponseBody {
  private UboDeclaration document;

  /** @return the document */
  public UboDeclaration getDocument() {

    return document;
  }

  /** @param document the document to set */
  public void setDocument(UboDeclaration document) {

    this.document = document;
  }
}
