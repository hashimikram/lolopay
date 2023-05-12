package ro.iss.lolopay.jobs;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.FailedCallback;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.models.services.definition.CallbackService;
import ro.iss.lolopay.models.services.definition.FailedCallbackService;
import ro.iss.lolopay.models.services.definition.ProcessedCallbackService;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.EmailService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

@Singleton
public class ProcessCallbacksJob {
  /** System Actor */
  private final ActorSystem actorSystem;

  /** Execution context provided by the framework */
  private final ExecutionContext executionContext;

  @Inject AccountService accountService;

  @Inject ApplicationService applicationService;

  @Inject CallbackService callbackService;

  @Inject FailedCallbackService failedCallbackService;

  @Inject ProcessedCallbackService processedCallbackService;

  @Inject CoreService coreService;

  @Inject UtilsService utilsService;

  @Inject MangoPayHooks mangoPayHooks;

  @Inject PFSHooks pfsHooks;

  @Inject LogService logService;

  @Inject EmailService emailService;

  /**
   * Job constructor
   *
   * @param actorSystem
   * @param executionContext
   */
  @Inject
  public ProcessCallbacksJob(ActorSystem actorSystem, ExecutionContext executionContext) {

    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.initialize();
  }

  /** Private method to define and start the scheduled job */
  private void initialize() {

    this.actorSystem
        .scheduler()
        .schedule( //
            Duration.create(10, TimeUnit.SECONDS), // initialDelay
            Duration.create(10, TimeUnit.SECONDS), // interval
            () -> executeCallbacksJob(),
            this.executionContext);
  }

  /** Scheduled job execution */
  public void executeCallbacksJob() {

    List<Callback> pendingCallbacks = callbackService.getOldestCallbacks("");

    if (pendingCallbacks.size() > 0) {
      for (Callback callback : pendingCallbacks) {
        // generate callback request id
        String requestId = utilsService.generateRandomString(16);

        // register log header for this operation
        logService.header(
            requestId,
            "LoLo",
            "executeCallbacksJob",
            callback.getTag(),
            callback.getProvider().concat(String.valueOf(callback.getNoFails())),
            callback.getId());

        // for each callback identify the account
        Account sessionAccount =
            accountService.getAccountByAccountId(requestId, callback.getAccountId());

        // if account is not found
        if (sessionAccount == null) {
          // update callback audit
          callback.updateAudit(coreService.getSystemApplicationStamp());

          // save callback and update save time as well
          callbackService.saveCallback(requestId, callback);

          // log error
          logService.error(requestId, "L", "error", "Account not found");

          // send email to developer
          // build email content
          StringBuffer sb = new StringBuffer();
          sb.append("Request Id / Callback id: ");
          sb.append(requestId);
          sb.append(System.lineSeparator());
          sb.append("Error: Account not found");
          sb.append(System.lineSeparator());
          sb.append("Error stack:");
          sb.append(ExceptionUtils.getStackTrace(new Throwable()));

          // send an email
          emailService.email(
              ConfigFactory.load().getString("application.noreplyemail"),
              ConfigFactory.load().getString("application.name"),
              ConfigFactory.load().getString("application.devemail"),
              "LoLo Developer",
              ConfigFactory.load().getString("application.environment") + " LoLo callBackError",
              sb.toString());

          // jump to the next callback
          continue;
        }

        // get session application
        Application sessionApplication =
            applicationService.getApplicationByApplicationId(
                requestId, sessionAccount, callback.getApplicationId());
        if (sessionApplication == null) {
          // update callback audit
          callback.updateAudit(coreService.getSystemApplicationStamp());

          // save callback and update save time as well
          callbackService.saveCallback(requestId, callback);

          // log error
          logService.error(requestId, "L", "error", "Application not found");

          // send email to developer
          // build email content
          StringBuffer sb = new StringBuffer();
          sb.append("Request Id / Callback id: ");
          sb.append(requestId);
          sb.append(System.lineSeparator());
          sb.append("Error: Application not found");
          sb.append(System.lineSeparator());
          sb.append("Error stack:");
          sb.append(ExceptionUtils.getStackTrace(new Throwable()));

          // send an email
          emailService.email(
              ConfigFactory.load().getString("application.noreplyemail"),
              ConfigFactory.load().getString("application.name"),
              ConfigFactory.load().getString("application.devemail"),
              "LoLo Developer",
              ConfigFactory.load().getString("application.environment") + " LoLo callBackError",
              sb.toString());

          // jump to the next callback
          continue;
        }

        logService.info(requestId, "L", "start", requestId);

        try {
          if (callback.getProvider().equals("PFS")) {
            logService.info(requestId, "L", "hookProvider", "PFS");

            // try to process the call back
            pfsHooks.process(requestId, sessionAccount, sessionApplication, callback);
          } else if (callback.getProvider().equals("MANGO")) {
            logService.info(requestId, "L", "hookProvider", "MANGO");

            // try to process the call back
            mangoPayHooks.process(requestId, sessionAccount, sessionApplication, callback);
          } else {
            ResponseError responseError = new ResponseError();
            responseError.setErrorCode(ErrorMessage.ERROR_HOOKS_INVALID_PROVIDER);
            responseError.setErrorDescription(
                "Provider not implemented : " + callback.getProvider());

            GenericRestException gre = new GenericRestException();
            gre.addResponseError(responseError);
            throw gre;
          }

          // save success callback in history table
          ProcessedCallback processedCallback = new ProcessedCallback();
          processedCallback.setId(callback.getId());
          processedCallback.setProvider(callback.getProvider());
          processedCallback.setAccountId(callback.getAccountId());
          processedCallback.setApplicationId(callback.getApplicationId());
          processedCallback.setParameters(callback.getParameters());
          processedCallback.setNoFails(callback.getNoFails() + 1);
          processedCallback.setTag(callback.getTag());
          processedCallback.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);
          processedCallbackService.saveProcessedCallback(requestId, processedCallback);

          // delete callback
          callbackService.deleteCallback(requestId, callback);

          // log completed process
          logService.info(requestId, "OUT", "success", requestId);

        } catch (Exception exception) {
          if (callback.getNoFails() > 3) {
            if (exception instanceof GenericRestException) {
              // log error
              logService.error(requestId, "L", "error LoLo", "GenericRestException");
              logService.error(
                  requestId, "L", "errorStack", ExceptionUtils.getStackTrace(exception));

              // build email content
              StringBuffer sb = new StringBuffer();
              sb.append("Callback id: ");
              sb.append(callback.getId());
              sb.append(System.lineSeparator());
              sb.append("Error: Processing error: ");

              for (ResponseError responseError :
                  ((GenericRestException) exception).getResponseErrors()) {
                sb.append("Error code: ");
                sb.append(responseError.getErrorCode());
                sb.append("Error description: ");
                sb.append(responseError.getErrorDescription());
                sb.append(System.lineSeparator());
              }

              sb.append("Error stack:");
              sb.append(ExceptionUtils.getStackTrace(exception));

              // send an email
              emailService.email(
                  ConfigFactory.load().getString("application.noreplyemail"),
                  ConfigFactory.load().getString("application.name"),
                  ConfigFactory.load().getString("application.devemail"),
                  "LoLo Developer",
                  ConfigFactory.load().getString("application.environment") + " LoLo callBackError",
                  sb.toString());
            } else {
              // log error
              logService.error(requestId, "L", "error System", exception.getMessage());
              logService.error(
                  requestId, "L", "errorStack", ExceptionUtils.getStackTrace(exception));

              // build email content
              StringBuffer sb = new StringBuffer();
              sb.append("Callback id: ");
              sb.append(callback.getId());
              sb.append(System.lineSeparator());
              sb.append("Error: Processing error: ");
              sb.append(exception.getMessage());
              sb.append(System.lineSeparator());
              sb.append("Error stack:");
              sb.append(ExceptionUtils.getStackTrace(exception));

              // send an email
              emailService.email(
                  ConfigFactory.load().getString("application.noreplyemail"),
                  ConfigFactory.load().getString("application.name"),
                  ConfigFactory.load().getString("application.devemail"),
                  "LoLo Developer",
                  ConfigFactory.load().getString("application.environment") + " LoLo callBackError",
                  sb.toString());
            }

            // if failures are more than 5 move the callback in a back up table
            FailedCallback failedCallback = new FailedCallback();
            failedCallback.setId(callback.getId());
            failedCallback.setProvider(callback.getProvider());
            failedCallback.setAccountId(callback.getAccountId());
            failedCallback.setApplicationId(callback.getApplicationId());
            failedCallback.setParameters(callback.getParameters());
            failedCallback.setNoFails(callback.getNoFails() + 1);
            failedCallback.setTag(callback.getTag());
            failedCallback.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);
            failedCallbackService.saveFailedCallback(requestId, failedCallback);
            logService.info(requestId, "L", "moved callback", callback.getId());

            // delete callback
            callbackService.deleteCallback(requestId, callback);
            logService.info(requestId, "L", "deleted callback", callback.getId());
          } else {
            if (exception instanceof GenericRestException) {
              // log error without email
              logService.error(requestId, "L", "error LoLo", "GenericRestException");
              logService.error(
                  requestId, "L", "errorStack", ExceptionUtils.getStackTrace(exception));
            } else {
              // log error without email
              logService.error(requestId, "L", "error System", exception.getMessage());
              logService.error(
                  requestId, "L", "errorStack", ExceptionUtils.getStackTrace(exception));
            }

            // increase failures
            callback.setNoFails(callback.getNoFails() + 1);

            // update callback audit
            callback.updateAudit(coreService.getSystemApplicationStamp());

            // save callback and update save time as well
            callbackService.saveCallback(requestId, callback);
            logService.info(requestId, "L", "increased no fails for callback", callback.getId());
          }
        }

        logService.info(requestId, "OUT", "finished", requestId);
      }
    }
  }
}
