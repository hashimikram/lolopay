package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import play.libs.Json;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.TransferService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;

@Singleton
public class TransferImplementation implements TransferService {
  private final DatabaseService databaseService;

  @Inject LogService logService;

  /** Log singleton creation moment */
  @Inject
  public TransferImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public Transfer getTransfer(String requestId, Account account, String transferId) {

    // return result
    return (Transfer) databaseService.getRecord(requestId, account, transferId, Transfer.class);
  }

  @Override
  public Transfer getTransferByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (Transfer) databaseService.getRecord(requestId, account, filters, Transfer.class);
  }

  @Override
  public void saveTransfer(String requestId, Account account, Transfer transfer) {

    logService.debug(requestId, "L", "transfer", Json.toJson(transfer).toString());

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(transfer);
  }
}
