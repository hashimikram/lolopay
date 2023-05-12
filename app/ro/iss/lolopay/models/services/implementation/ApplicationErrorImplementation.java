package ro.iss.lolopay.models.services.implementation;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.services.definition.CacheService;

@Singleton
public class ApplicationErrorImplementation implements ApplicationErrorService {
  @Inject private CacheService cacheService;

  @Override
  public String getErrorCode(String requestId, String errorKey) {

    return cacheService.getErrorCode(requestId, errorKey);
  }

  @Override
  public List<ApplicationError> getErrors(String requestId) {

    return cacheService.getErrorCodes(requestId);
  }
}
