package ro.iss.lolopay.requests;

import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestSubmitDocument {
  @Required(message = ErrorMessage.ERROR_SUBMITDOCUMENT_DOCID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_SUBMITDOCUMENT_DOCID_INVALID)
  private String documentId;

  /** @return the documentId */
  public String getDocumentId() {

    return documentId;
  }

  /** @param documentId the documentId to set */
  public void setDocumentId(String documentId) {

    this.documentId = documentId;
  }
}
