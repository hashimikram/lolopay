package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.LogImplementation;

@ImplementedBy(LogImplementation.class)
public interface LogService {
  /**
   * Request log
   *
   * @param requestId
   * @param application
   * @param action
   * @param method
   * @param sessionKey
   * @param phone
   */
  void header(
      String requestId,
      String application,
      String action,
      String method,
      String sessionKey,
      String phone);

  /**
   * Debugging log sent during request processing
   *
   * @param requestId
   * @param action
   * @param method
   * @param direction
   * @param paramName
   * @param paramValue
   */
  void debug(String requestId, String direction, String paramName, Object paramValue);

  /**
   * Error log sent during request processing
   *
   * @param requestId
   * @param action
   * @param method
   * @param direction
   * @param paramName
   * @param paramValue
   */
  void error(String requestId, String direction, String paramName, Object paramValue);

  /**
   * Information log sent during request processing
   *
   * @param requestId
   * @param action
   * @param method
   * @param direction
   * @param paramName
   * @param paramValue
   */
  void info(String requestId, String direction, String paramName, Object paramValue);
}
