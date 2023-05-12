package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class PayInImplementation implements PayInService {
  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public PayInImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public PayIn getPayIn(String requestId, Account account, String payInId) {

    // return result
    return (PayIn) databaseService.getRecord(requestId, account, payInId, PayIn.class);
  }

  @Override
  public void savePayIn(String requestId, Account account, PayIn payIn) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(payIn);
  }

  @Override
  public PayIn getPayInByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (PayIn) databaseService.getRecord(requestId, account, filters, PayIn.class);
  }
}
