package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.Document;

public class ResponseDocuments extends RestResponseBody {
  private List<Document> documents;

  /** @return the documents */
  public List<Document> getDocuments() {

    return documents;
  }

  /** @param documents the documents to set */
  public void setDocuments(List<Document> documents) {

    this.documents = documents;
  }
}
