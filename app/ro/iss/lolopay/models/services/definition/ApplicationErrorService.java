package ro.iss.lolopay.models.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.models.services.implementation.ApplicationErrorImplementation;

@ImplementedBy(ApplicationErrorImplementation.class)
public interface ApplicationErrorService {
  public String getErrorCode(String requestId, String errorKey);

  public List<ApplicationError> getErrors(String requestId);
}
