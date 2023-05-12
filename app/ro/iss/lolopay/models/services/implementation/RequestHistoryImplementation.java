package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import ro.iss.lolopay.models.main.RequestHistory;
import ro.iss.lolopay.models.services.definition.RequestHistoryService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class RequestHistoryImplementation implements RequestHistoryService {
  @Inject private DatabaseService databaseService;

  @Override
  public RequestHistory findByRequestId(String requestId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("requestId", requestId);

    // return result
    return (RequestHistory) databaseService.getMainRecord(requestId, filters, RequestHistory.class);
  }

  @Override
  public void saveRequestHistory(String requestId, RequestHistory requestHistory) {

    // save record in database
    databaseService.getMainConnection().save(requestHistory);
  }
}
