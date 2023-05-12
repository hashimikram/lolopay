package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.PayOutService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class PayOutImplementation implements PayOutService {
  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public PayOutImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public PayOut getPayOut(String requestId, Account account, String payOutId) {

    // return result
    return (PayOut) databaseService.getRecord(requestId, account, payOutId, PayOut.class);
  }

  @Override
  public void savePayOut(String requestId, Account account, PayOut payOut) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(payOut);
  }

  @Override
  public PayOut getPayOutByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (PayOut) databaseService.getRecord(requestId, account, filters, PayOut.class);
  }
}
