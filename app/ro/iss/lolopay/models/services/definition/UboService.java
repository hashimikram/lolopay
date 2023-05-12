package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.UboDeclarationImplementation;

@ImplementedBy(UboDeclarationImplementation.class)
public interface UboService {
  public UboDeclaration getUboDeclaration(String requestId, Account account, String declarationId);

  public UboDeclaration getUboDeclarationByProviderId(
      String requestId, Account account, String providerId);

  public void saveUboDeclaration(String requestId, Account account, UboDeclaration uboDeclaration);
}
