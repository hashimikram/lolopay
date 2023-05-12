package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.classes.Ubo;

public class ResponseUbo extends RestResponseBody {
  private Ubo ubo;

  /** @return the Ubo */
  public Ubo getUbo() {

    return ubo;
  }

  /** @param ubo Ubo to set */
  public void setUbo(Ubo ubo) {

    this.ubo = ubo;
  }
}
