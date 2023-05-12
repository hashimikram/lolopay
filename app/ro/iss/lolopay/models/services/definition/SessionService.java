package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.models.services.implementation.SessionImplementation;

@ImplementedBy(SessionImplementation.class)
public interface SessionService {
  public Session getSession(String requestId, String sessionId);

  public void deleteSession(String requestId, Session session);

  public void saveSession(String requestId, Session session);

  public void deleteAllExpired(String requestId, String applicationId);
}
