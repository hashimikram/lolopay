package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.database.Document;

public class ResponseDocument extends RestResponseBody {
  private Document document;

  /** @return the document */
  public Document getDocument() {

    return document;
  }

  /** @param document the document to set */
  public void setDocument(Document document) {

    this.document = document;
  }
}
