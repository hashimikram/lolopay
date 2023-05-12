package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.mongodb.WriteConcern;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.ApplicationActivity;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.services.definition.CacheService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class ApplicationImplementation implements ApplicationService {
  @Inject private CacheService cacheService;

  @Inject private DatabaseService databaseService;

  @Inject private UtilsService utilsService;

  @Override
  public void saveApplication(String requestId, Account account, Application application) {

    // save application in database
    databaseService.getConnection(account.getId().toString()).save(application);

    // add new application in cache
    cacheService.addApplicationToCache(requestId, account, application);
  }

  /**
   * Find one application by id and account owner
   *
   * @param accountId
   * @param applicationId
   * @return
   */
  @Override
  public Application getApplication(String requestId, Account account, String applicationId) {

    return cacheService.getApplication(requestId, account, applicationId);
  }

  @Override
  public Application getApplicationByApplicationId(
      String requestId, Account account, String readableApplicationId) {

    return cacheService.getApplicationByApplicationId(requestId, account, readableApplicationId);
  }

  /** Update application details */
  @Override
  public void updateApplicationDetails(
      String requestId,
      Account account,
      Application application,
      String remoteAddress,
      String userAgent) {

    // create update fields
    Map<String, Object> fieldsToUpdate = new HashMap<String, Object>();
    fieldsToUpdate.put("lastActivity", utilsService.getTimeStamp());
    fieldsToUpdate.put("lastUsedIp", remoteAddress);
    fieldsToUpdate.put("lastUserAgent", userAgent);

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("id", application.getId());

    // update
    databaseService.updateRecord(
        requestId,
        account,
        application,
        filters,
        fieldsToUpdate,
        WriteConcern.UNACKNOWLEDGED,
        ApplicationActivity.class);
  }

  /** Update app details - login moment */
  @Override
  public void updateApplicationDetailsLogin(
      String requestId, Account account, Application application) {

    // create update fields
    Map<String, Object> fieldsToUpdate = new HashMap<String, Object>();
    fieldsToUpdate.put("lastLogIn", utilsService.getTimeStamp());

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("id", application.getId());

    // update
    databaseService.updateRecord(
        requestId,
        account,
        application,
        filters,
        fieldsToUpdate,
        WriteConcern.UNACKNOWLEDGED,
        ApplicationActivity.class);
  }

  /** update app details logout moment */
  @Override
  public void updateApplicationDetailsRefresh(
      String requestId, Account account, Application application) {

    // create update fields
    Map<String, Object> fieldsToUpdate = new HashMap<String, Object>();
    fieldsToUpdate.put("lastRefresh", utilsService.getTimeStamp());

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("id", application.getId());

    // update
    databaseService.updateRecord(
        requestId,
        account,
        application,
        filters,
        fieldsToUpdate,
        WriteConcern.UNACKNOWLEDGED,
        ApplicationActivity.class);
  }

  /**
   * Retrieve list of all applications of an account
   *
   * @param accountId
   * @param limitResults
   * @return
   */
  @Override
  public List<Application> getApplicationsForAccount(
      String requestId, Account account, int limitResults) {

    return cacheService.getApplicationsForAccount(requestId, account, limitResults);
  }
}
