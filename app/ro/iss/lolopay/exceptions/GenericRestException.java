package ro.iss.lolopay.exceptions;

import java.util.ArrayList;
import java.util.List;
import ro.iss.lolopay.responses.ResponseError;

public class GenericRestException extends RuntimeException {

  /** Generated serial version id for serialization */
  private static final long serialVersionUID = -1745663840473078784L;

  /** Provider errors list */
  private List<ResponseError> responseErrors;

  /**
   * Exception constructor
   *
   * @param errorCode
   * @param message
   */
  public GenericRestException() {

    this.responseErrors = new ArrayList<ResponseError>();
  }

  /** @return the serialversionuid */
  public static long getSerialversionuid() {

    return serialVersionUID;
  }

  public void addResponseError(ResponseError responseError) {

    this.responseErrors.add(responseError);
  }

  /** @return the responseErrors */
  public List<ResponseError> getResponseErrors() {

    return responseErrors;
  }

  /** @param responseErrors the responseErrors to set */
  public void setResponseErrors(List<ResponseError> responseErrors) {

    this.responseErrors = responseErrors;
  }
}
