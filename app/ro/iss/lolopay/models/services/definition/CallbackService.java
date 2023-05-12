package ro.iss.lolopay.models.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.services.implementation.CallbackImplementation;

@ImplementedBy(CallbackImplementation.class)
public interface CallbackService {
  public List<Callback> getOldestCallbacks(String requestId);

  public Callback getCallback(String requestId, String callbackId);

  public void deleteCallback(String requestId, Callback callback);

  public void saveCallback(String requestId, Callback callback);
}
