package ro.iss.lolopay.requests;

import play.libs.Json;

public class RequestClient {
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    return Json.toJson(this).toString();
  }
}
