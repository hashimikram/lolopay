package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.services.implementation.FailedCallbackImplementation;

@ImplementedBy(FailedCallbackImplementation.class)
public interface FailedCallbackService {
  public FailedCallback getFailedCallback(String requestId, String callbackId);

  public void deleteFailedCallback(String requestId, FailedCallback callback);

  public void saveFailedCallback(String requestId, FailedCallback callback);
}
