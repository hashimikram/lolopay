package ro.iss.lolopay.classes.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.responses.ResponseError;

public class ProviderResponse {
  /** Provider errors list */
  private List<ResponseError> providerErrors;

  /** Provider response data */
  private Map<String, Object> providerData;

  /** Status of the data posting, delivered by provider */
  private ProviderOperationStatus providerOperationStatus;

  public ProviderResponse() {

    this.providerData = new HashMap<String, Object>();
    this.providerErrors = new ArrayList<ResponseError>();
  }

  /**
   * Add provider error
   *
   * @param responseError
   */
  public void addProviderError(ResponseError responseError) {

    this.providerErrors.add(responseError);
  }

  /**
   * Add provider error based on provider error message only
   *
   * @param providerErrorMessage
   */
  public void addProviderError(String providerErrorMessage) {

    ResponseError responseError = new ResponseError();
    responseError.setErrorCode(ErrorMessage.ERROR_PROVIDER);
    responseError.setErrorDescription(providerErrorMessage);
    this.providerErrors.add(responseError);
  }

  /**
   * Get provider data from provider map based on provided key
   *
   * @param key
   * @return
   */
  public Object getProviderData(String key) {

    return this.providerData.getOrDefault(key, null);
  }

  /**
   * Add some data to provider map
   *
   * @param key
   * @param value
   */
  public void addProviderData(String key, Object value) {

    this.providerData.put(key, value);
  }

  /** @return the providerErrors */
  public List<ResponseError> getProviderErrors() {

    return providerErrors;
  }

  /** @param providerErrors the providerErrors to set */
  public void setProviderErrors(List<ResponseError> providerErrors) {

    this.providerErrors = providerErrors;
  }

  /** @return the providerData */
  public Map<String, Object> getProviderData() {

    return providerData;
  }

  /** @param providerData the providerData to set */
  public void setProviderData(Map<String, Object> providerData) {

    this.providerData = providerData;
  }

  /** @return the providerOperationStatus */
  public ProviderOperationStatus getProviderOperationStatus() {

    return providerOperationStatus;
  }

  /** @param providerOperationStatus the providerOperationStatus to set */
  public void setProviderOperationStatus(ProviderOperationStatus providerOperationStatus) {

    this.providerOperationStatus = providerOperationStatus;
  }
}
