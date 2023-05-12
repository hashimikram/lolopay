package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsBase64;

public class RequestCreateDocumentPage {
  @Required(message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USERID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USERID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_DOCID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_DOCID_INVALID)
  private String documentId;

  @Required(message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_FILE_REQUIRED)
  @MinLength(value = 10, message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_FILE_MINLENGTH)
  @ValidateWith(
      value = IsBase64.class,
      message = ErrorMessage.ERROR_CREATEDOCUMENTPAGE_FILE_INVALIDB64)
  private String file;

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the documentId */
  public String getDocumentId() {

    return documentId;
  }

  /** @param documentId the documentId to set */
  public void setDocumentId(String documentId) {

    this.documentId = documentId;
  }

  /** @return the file */
  public String getFile() {

    return file;
  }

  /** @param file the file to set */
  public void setFile(String file) {

    this.file = file;
  }
}
