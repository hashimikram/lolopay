package ro.iss.lolopay.models.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.ApplicationImplementation;

@ImplementedBy(ApplicationImplementation.class)
public interface ApplicationService {
  public Application getApplication(String requestId, Account account, String applicationId);

  public Application getApplicationByApplicationId(
      String requestId, Account account, String readableApplicationId);

  public void saveApplication(String requestId, Account account, Application application);

  public void updateApplicationDetails(
      String requestId, Account account, Application user, String remoteAddress, String userAgent);

  public void updateApplicationDetailsLogin(String requestId, Account account, Application user);

  public void updateApplicationDetailsRefresh(String requestId, Account account, Application user);

  public List<Application> getApplicationsForAccount(
      String requestId, Account account, int limitResults);
}
