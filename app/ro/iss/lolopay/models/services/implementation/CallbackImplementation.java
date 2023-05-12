package ro.iss.lolopay.models.services.implementation;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.query.Query;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.services.definition.CallbackService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class CallbackImplementation implements CallbackService {
  @Inject private DatabaseService databaseService;

  @Override
  public Callback getCallback(String requestId, String callbackId) {

    // find callback by id
    return (Callback) databaseService.getMainRecord(requestId, callbackId, Callback.class);
  }

  @Override
  public void deleteCallback(String requestId, Callback callback) {

    // remove session from Database
    databaseService.getMainConnection().delete(callback);
  }

  @Override
  public void saveCallback(String requestId, Callback callback) {

    // save callback
    databaseService.getMainConnection().save(callback);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Callback> getOldestCallbacks(String requestId) {

    // create query to retrieve existent unprocessed callback hooks
    Query<? extends TableCollection> query =
        databaseService.getMainConnection().createQuery(Callback.class);
    query.order("updatedAt");

    // return result
    return (List<Callback>) databaseService.getMainAllRecords(requestId, query);
  }
}
