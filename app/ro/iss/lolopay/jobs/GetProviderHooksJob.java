package ro.iss.lolopay.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mangopay.MangoPayApi;
import com.mangopay.core.FilterEvents;
import com.mangopay.core.Pagination;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.entities.Event;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.models.classes.GeneralCallback;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.models.services.definition.CallbackService;
import ro.iss.lolopay.models.services.definition.FailedCallbackService;
import ro.iss.lolopay.models.services.definition.ProcessedCallbackService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.EmailService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

public class GetProviderHooksJob {
  /** System Actor */
  private final ActorSystem actorSystem;

  /** Execution context provided by the framework */
  private final ExecutionContext executionContext;

  /** MangoPay api client */
  private MangoPayApi api = null;

  @Inject CoreService coreService;

  @Inject UtilsService utilsService;

  @Inject LogService logService;

  @Inject EmailService emailService;

  @Inject AccountService accountService;

  @Inject ApplicationService applicationService;

  @Inject CallbackService callbackService;

  @Inject FailedCallbackService failedCallbackService;

  @Inject ProcessedCallbackService processedCallbackService;

  /**
   * Job constructor
   *
   * @param actorSystem
   * @param executionContext
   */
  @Inject
  public GetProviderHooksJob(ActorSystem actorSystem, ExecutionContext executionContext) {

    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.initialize();
  }

  /** Private method to define and start the scheduled job */
  private void initialize() {

    if (api == null) {
      api = new MangoPayApi();

      // configuration
      api.getConfig().setBaseUrl(ConfigFactory.load().getString("mangopay.baseUrl"));
      api.getConfig().setClientId(ConfigFactory.load().getString("mangopay.clientId"));
      api.getConfig().setClientPassword(ConfigFactory.load().getString("mangopay.clientPassword"));
      api.getConfig().setConnectTimeout(ConfigFactory.load().getInt("mangopay.connectionTimeout"));
      api.getConfig().setDebugMode(ConfigFactory.load().getBoolean("mangopay.debugMode"));
      api.getConfig().setReadTimeout(ConfigFactory.load().getInt("mangopay.readTimeout"));
    }

    this.actorSystem
        .scheduler()
        .schedule( //
            Duration.create(5, TimeUnit.SECONDS), // initialDelay
            Duration.create(60, TimeUnit.SECONDS), // interval
            () -> executeJob(),
            this.executionContext);
  }

  /** Scheduled job execution */
  private void executeJob() {

    // get request id - will be used in responses
    String requestId = utilsService.generateRandomString(16);

    // register log header for this operation
    logService.header(
        requestId,
        "LoLo",
        "executeJob",
        "getMangoEvents",
        requestId,
        utilsService.timeStampToDate(utilsService.getTimeStamp(), "dd/MM/yyyy hh:mm:ss"));

    Account sessionAccount = accountService.getAccountByAccountId(requestId, "moneymailme");
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "mango: account not found");
      return;
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, "m3Service");
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "mango: application not found");
      return;
    }

    Long afterDate = utilsService.getTimeStamp() - 60 * 60 * 24 * 7;

    FilterEvents filterEvents = new FilterEvents();
    filterEvents.setAfterDate(afterDate);
    filterEvents.setType(EventType.ALL);

    Pagination pagination = new Pagination();
    pagination.setItemsPerPage(25);
    pagination.setPage(0);

    Sorting sorting = new Sorting();
    sorting.addField("Date", SortDirection.desc);

    try {
      logService.debug(requestId, "L", "start", "get events");
      List<Event> events = api.getEventApi().get(filterEvents, pagination, sorting);
      logService.debug(requestId, "L", "events", events.size());

      for (Event event : events) {
        // get incoming notification type
        NotificationType incommingMangoNotification =
            NotificationType.valueOf(event.getEventType().toString());

        // incoming hook id
        String localHookId = event.getResourceId().concat(event.getEventType().toString());

        // check for event in pending table
        GeneralCallback existingCallback = callbackService.getCallback(requestId, localHookId);
        if (existingCallback != null) {
          logService.debug(
              requestId,
              "L",
              "end",
              "mango: event already processed (PENDING) with id " + localHookId);

          // if event found in history then we skip it
          continue;
        }

        // check for event in failed events
        existingCallback = failedCallbackService.getFailedCallback(requestId, localHookId);
        if (existingCallback != null) {
          logService.debug(
              requestId,
              "L",
              "end",
              "mango: event already processed (FAILED) with id " + localHookId);

          // if event found in history then we skip it
          continue;
        }

        // check for event in success events
        existingCallback = processedCallbackService.getProcessedCallback(requestId, localHookId);
        if (existingCallback != null) {

          logService.debug(
              requestId,
              "L",
              "end",
              "mango: event already processed (PROCESSED) with id " + localHookId);

          // if event found in history then we skip it
          continue;
        }

        // build callback
        Callback newCallback = new Callback();
        newCallback.setId(localHookId);
        newCallback.setProvider("MANGO");
        newCallback.setAccountId("moneymailme");
        newCallback.setApplicationId("m3Service");
        newCallback.setNoFails(0);
        newCallback.setTag(event.getEventType().toString());
        newCallback.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);

        // build callback parameters
        HashMap<String, Object> callbackParameters = new HashMap<String, Object>();
        callbackParameters.put("EventType", incommingMangoNotification);
        callbackParameters.put("RessourceId", event.getResourceId());
        callbackParameters.put("Date", event.getDate());
        newCallback.setParameters(callbackParameters);

        // save callback
        callbackService.saveCallback(requestId, newCallback);

        // reply to Mango hook
        logService.debug(
            requestId, "L", "end", "mango: event registered with id " + event.getResourceId());
      }
    } catch (Exception exception) {
      // log error
      logService.error(requestId, "L", "error", exception.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(exception));

      // send email to developer
      // build email content
      StringBuffer sb = new StringBuffer();
      sb.append("Processing error: ");
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
          ConfigFactory.load().getString("application.environment") + " LoLo get Mango Hooks Error",
          sb.toString());
    }
  }
}
