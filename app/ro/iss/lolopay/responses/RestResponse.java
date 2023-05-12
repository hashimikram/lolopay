package ro.iss.lolopay.responses;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class RestResponse {
  private boolean success;

  private RestResponseBody body;

  private List<ResponseError> errors;

  public boolean isSuccess() {

    return success;
  }

  public void setSuccess(boolean success) {

    this.success = success;
  }

  public RestResponseBody getBody() {

    return body;
  }

  public void setBody(RestResponseBody body) {

    this.body = body;
  }

  public List<ResponseError> getErrors() {

    return errors;
  }

  public void setErrors(List<ResponseError> errors) {

    this.errors = errors;
  }

  public void addError(ResponseError errorCode) {

    if (null == this.errors) {
      this.errors = new ArrayList<ResponseError>();
    }
    this.errors.add(errorCode);
  }
}
