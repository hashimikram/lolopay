package ro.iss.lolopay.services.implementation;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ro.iss.logger.rabbit.QueueLogger;
import ro.iss.logger.rabbit.QueueParameters;
import ro.iss.lolopay.services.definition.LogService;

@Singleton
public class LogImplementation implements LogService {

  private QueueLogger queueLogger;

  private Config config = ConfigFactory.load();

  private static final Logger logger = LoggerFactory.getLogger(LogImplementation.class);

  /** Log singleton creation moment */
  public LogImplementation() {

    logger.debug("Singleton created");

    QueueParameters queueParameters = new QueueParameters();

    queueParameters.setHost(config.getString("logger.host"));
    queueParameters.setPort(config.getInt("logger.port"));
    queueParameters.setUsername(config.getString("logger.username"));
    queueParameters.setPassword(config.getString("logger.password"));
    queueParameters.setQueueName(config.getString("logger.queueName"));
    queueParameters.setConnectionTimeOut(config.getInt("logger.conTimeOut"));
    queueParameters.setSetHandshakeTimeout(config.getInt("logger.hskTimeOut"));

    this.queueLogger = new QueueLogger(queueParameters);
  }

  @Override
  public void header(
      String requestId,
      String application,
      String action,
      String method,
      String sessionKey,
      String phone) {

    if (config.getString("application.environment").equals("local")) {
      if (!action.equals("DatabaseImplementation")) {
        logger.info(
            "HEADER {} {} {} {} {} {}", requestId, application, action, method, sessionKey, phone);
      }
      return;
    }

    if (requestId != null && !requestId.equals("")) {
      CompletableFuture.runAsync(
          () -> {
            try {
              queueLogger.header(requestId, application, action, method, sessionKey, phone);
            } catch (IOException | TimeoutException e) {
              logger.error("Log system does not work " + e.getMessage());
              e.printStackTrace();
            }
          });
    }
  }

  @Override
  public void debug(String requestId, String direction, String paramName, Object paramValue) {

    StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
    String action = stackTrace.getClass().getSimpleName();
    String method = stackTrace.getMethodName();

    if (config.getString("application.environment").equals("local")) {
      switch (action) {
        case "DatabaseImplementation":
        case "StackTraceElement":
          break;
        default:
          logger.debug(
              "{} {} {} {} {} {}", requestId, action, method, direction, paramName, paramValue);
          break;
      }
      return;
    }

    if (requestId != null && !requestId.equals("")) {
      CompletableFuture.runAsync(
          () -> {
            try {
              if (paramValue != null) {
                queueLogger.debug(
                    requestId, action, method, direction, paramName, paramValue.toString());
              } else {
                queueLogger.debug(requestId, action, method, direction, paramName, null);
              }

            } catch (IOException | TimeoutException e) {
              logger.error("Log system does not work " + e.getMessage());
              e.printStackTrace();
            }
          });
    }
  }

  @Override
  public void error(String requestId, String direction, String paramName, Object paramValue) {
    StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
    String action = stackTrace.getClass().getSimpleName();
    String method = stackTrace.getMethodName();

    if (config.getString("application.environment").equals("local")) {
      switch (action) {
        case "DatabaseImplementation":
        case "StackTraceElement":
          break;
        default:
          logger.error(
              "{} {} {} {} {} {}", requestId, action, method, direction, paramName, paramValue);
          break;
      }
      return;
    }

    if (requestId != null && !requestId.equals("")) {
      CompletableFuture.runAsync(
          () -> {
            try {
              if (paramValue != null) {
                queueLogger.error(
                    requestId, action, method, direction, paramName, paramValue.toString());
              } else {
                queueLogger.error(requestId, action, method, direction, paramName, null);
              }
            } catch (IOException | TimeoutException e) {
              logger.error("Log system does not work " + e.getMessage());
              e.printStackTrace();
            }
          });
    }
  }

  @Override
  public void info(String requestId, String direction, String paramName, Object paramValue) {

    StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
    String action = stackTrace.getClass().getSimpleName();
    String method = stackTrace.getMethodName();

    if (config.getString("application.environment").equals("local")) {
      switch (action) {
        case "DatabaseImplementation":
        case "StackTraceElement":
          break;
        default:
          logger.info(
              "{} {} {} {} {} {}", requestId, action, method, direction, paramName, paramValue);
          break;
      }
      return;
    }

    if (requestId != null && !requestId.equals("")) {
      CompletableFuture.runAsync(
          () -> {
            try {
              if (paramValue != null) {
                queueLogger.info(
                    requestId, action, method, direction, paramName, paramValue.toString());
              } else {
                queueLogger.info(requestId, action, method, direction, paramName, null);
              }
            } catch (IOException | TimeoutException e) {
              logger.error("Log system does not work " + e.getMessage());
              e.printStackTrace();
            }
          });
    }
  }
}
