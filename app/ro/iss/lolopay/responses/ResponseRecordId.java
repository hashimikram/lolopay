package ro.iss.lolopay.responses;

public class ResponseRecordId extends RestResponseBody {
  private String id;

  /** @return the id */
  public String getId() {

    return id;
  }

  /** @param id the id to set */
  public void setId(String id) {

    this.id = id;
  }
}
