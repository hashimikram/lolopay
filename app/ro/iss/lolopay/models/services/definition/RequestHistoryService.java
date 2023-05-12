package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.services.implementation.RequestHistoryImplementation;

@ImplementedBy(RequestHistoryImplementation.class)
public interface RequestHistoryService {
  public RequestHistory findByRequestId(String requestId);

  public void saveRequestHistory(String requestId, RequestHistory requestHistory);
}
