package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "applicationErrors", noClassnameStored = true)
public class ApplicationError extends TableCollection {
  private String errorCode;

  @Indexed private String errorKey;

  /** @return the errorCode */
  public String getErrorCode() {

    return errorCode;
  }

  /** @param errorCode the errorCode to set */
  public void setErrorCode(String errorCode) {

    this.errorCode = errorCode;
  }

  /** @return the errorKey */
  public String getErrorKey() {

    return errorKey;
  }

  /** @param errorKey the errorKey to set */
  public void setErrorKey(String errorKey) {

    this.errorKey = errorKey;
  }
}
