package ro.iss.lolopay.responses;

import ro.iss.lolopay.models.main.ProcessedCallback;

public class ResponseProcessedCallback extends RestResponseBody {
  private ProcessedCallback processedCallback;

  /** @return the processedCallback */
  public ProcessedCallback getProcessedCallback() {

    return processedCallback;
  }

  /** @param processedCallback the processedCallback to set */
  public void setProcessedCallback(ProcessedCallback processedCallback) {

    this.processedCallback = processedCallback;
  }
}
