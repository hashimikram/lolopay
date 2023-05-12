package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.implementation.NotificationImplementation;

@ImplementedBy(NotificationImplementation.class)
public interface NotificationService {
  public void notifyClient(
      String requestId,
      Account account,
      Application application,
      NotificationType notificationType,
      String resourceId);

  public void notifySelf(
      String requestId,
      Account account,
      Application application,
      NotificationType notificationType,
      String resourceId);
}
