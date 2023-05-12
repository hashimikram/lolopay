package ro.iss.lolopay.models.services.implementation;

import javax.inject.Inject;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.services.definition.FailedCallbackService;
import ro.iss.lolopay.services.definition.DatabaseService;

public class FailedCallbackImplementation implements FailedCallbackService {

  @Inject private DatabaseService databaseService;

  @Override
  public FailedCallback getFailedCallback(String requestId, String callbackId) {

    // find callback by id
    return (FailedCallback)
        databaseService.getMainRecord(requestId, callbackId, FailedCallback.class);
  }

  @Override
  public void deleteFailedCallback(String requestId, FailedCallback callback) {

    // remove session from Database
    databaseService.getMainConnection().delete(callback);
  }

  @Override
  public void saveFailedCallback(String requestId, FailedCallback callback) {

    // save callback
    databaseService.getMainConnection().save(callback);
  }
}
