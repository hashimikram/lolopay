package ro.iss.lolopay.models.services.implementation;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.query.Query;
import play.Logger;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.models.services.definition.SessionService;
import ro.iss.lolopay.services.definition.CacheService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class SessionImplementation implements SessionService {

  @Inject private CacheService cacheService;

  @Inject private DatabaseService databaseService;

  @Inject private UtilsService utilsService;

  @Override
  public Session getSession(String requestId, String sessionId) {

    return cacheService.getSession(requestId, sessionId);
  }

  @Override
  public void deleteSession(String requestId, Session session) {

    // remove session from Database
    databaseService.getMainConnection().delete(session);

    // remove session from cache
    cacheService.removeSessionFromCache(requestId, session);
  }

  @Override
  public void saveSession(String requestId, Session session) {

    // save session
    databaseService.getMainConnection().save(session);

    // update session cache
    cacheService.addSessionToCache(requestId, session);
  }

  @Override
  public void deleteAllExpired(String requestId, String applicationId) {

    // delete expired server sessions (expired more than one day) for logged like: expiryDate .....
    // +1Day ..... Now
    Query<Session> allOtherSessionsQuery =
        databaseService.getMainConnection().createQuery(Session.class);
    allOtherSessionsQuery.field("applicationId").equal(applicationId);
    allOtherSessionsQuery.field("expiryDate").lessThan((utilsService.getTimeStamp() - 86400L));

    Logger.of(this.getClass())
        .debug("deleteAllExpired: query: " + allOtherSessionsQuery.toString());

    databaseService.getMainConnection().delete(allOtherSessionsQuery);
  }
}
