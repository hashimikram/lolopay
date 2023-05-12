package ro.iss.lolopay.requests;

import javax.validation.Valid;
import play.data.validation.Constraints.Required;
import ro.iss.lolopay.enums.ErrorMessage;

public class RequestCreateAVSDirectPayIn extends RequestCreateDirectPayIn {

  @Required(message = ErrorMessage.ERROR_CREATEAVSDIRECTPAYIN_BILLING_ADDRESS_REQUIRED)
  @Valid
  private RequestAddress billing;

  /** @return the billing */
  public RequestAddress getBilling() {

    return billing;
  }

  /** @param billing the billing to set */
  public void setBilling(RequestAddress billing) {

    this.billing = billing;
  }
}
