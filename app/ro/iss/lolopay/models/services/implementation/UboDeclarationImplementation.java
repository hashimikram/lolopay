package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.UboService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class UboDeclarationImplementation implements UboService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public UboDeclarationImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public UboDeclaration getUboDeclaration(String requestId, Account account, String declarationId) {

    return (UboDeclaration)
        databaseService.getRecord(requestId, account, declarationId, UboDeclaration.class);
  }

  @Override
  public UboDeclaration getUboDeclarationByProviderId(
      String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (UboDeclaration)
        databaseService.getRecord(requestId, account, filters, UboDeclaration.class);
  }

  @Override
  public void saveUboDeclaration(String requestId, Account account, UboDeclaration uboDeclaration) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(uboDeclaration);
  }
}
