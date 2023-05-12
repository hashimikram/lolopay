package ro.iss.lolopay.responses;

import java.util.List;
import ro.iss.lolopay.models.database.Dispute;

public class ResponseGetDisputes extends RestResponseBody {
  private List<Dispute> disputes;

  /** @return the disputes */
  public List<Dispute> getDisputes() {

    return disputes;
  }

  /** @param disputes the disputes to set */
  public void setDisputes(List<Dispute> disputes) {

    this.disputes = disputes;
  }
}
