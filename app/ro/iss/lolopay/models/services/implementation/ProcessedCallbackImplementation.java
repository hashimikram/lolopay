package ro.iss.lolopay.models.services.implementation;

import javax.inject.Inject;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.services.definition.ProcessedCallbackService;
import ro.iss.lolopay.services.definition.DatabaseService;

public class ProcessedCallbackImplementation implements ProcessedCallbackService {
  @Inject private DatabaseService databaseService;

  @Override
  public ProcessedCallback getProcessedCallback(String requestId, String callbackId) {

    // find callback by id
    return (ProcessedCallback)
        databaseService.getMainRecord(requestId, callbackId, ProcessedCallback.class);
  }

  @Override
  public void deleteProcessedCallback(String requestId, ProcessedCallback callback) {

    // remove session from Database
    databaseService.getMainConnection().delete(callback);
  }

  @Override
  public void saveProcessedCallback(String requestId, ProcessedCallback callback) {

    // save callback
    databaseService.getMainConnection().save(callback);
  }
}
