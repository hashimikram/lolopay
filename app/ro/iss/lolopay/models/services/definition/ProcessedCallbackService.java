package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.services.implementation.ProcessedCallbackImplementation;

@ImplementedBy(ProcessedCallbackImplementation.class)
public interface ProcessedCallbackService {
  public ProcessedCallback getProcessedCallback(String requestId, String callbackId);

  public void deleteProcessedCallback(String requestId, ProcessedCallback callback);

  public void saveProcessedCallback(String requestId, ProcessedCallback callback);
}
