package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.mongodb.WriteConcern;
import play.Logger;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.DepositCardService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class DepositCardImplementation implements DepositCardService {

  private final DatabaseService databaseService;

  @Inject LogService logService;

  @Inject UtilsService utilsService;

  @Inject
  public DepositCardImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public void saveDepositCard(String requestId, Account account, DepositCard depositCard) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(depositCard);
  }

  @Override
  public DepositCard getDepositCard(String requestId, Account account, String userId) {

    Map<String, String> filters = new HashMap<>();
    filters.put("userId", userId);

    // create query for request history
    return (DepositCard) databaseService.getRecord(requestId, account, filters, DepositCard.class);
  }

  @Override
  public DepositCard getDepositCardById(String requestId, Account account, String cardId) {

    Map<String, String> filters = new HashMap<>();
    filters.put("_id", cardId);

    // create query for request history
    return (DepositCard) databaseService.getRecord(requestId, account, filters, DepositCard.class);
  }

  @Override
  public DepositCard getDepositCardByProviderId(
      String requestId, Account account, String cardProviderId) {

    Map<String, String> filters = new HashMap<>();
    filters.put("providerId", cardProviderId);

    // create query for request history
    return (DepositCard) databaseService.getRecord(requestId, account, filters, DepositCard.class);
  }

  @Override
  public DepositCard updateDepositCardValidity(
      String requestId,
      Account account,
      Application application,
      DepositCard currentDepositCard,
      DepositCard providerDepositCard) {

    logService.info(requestId, "L", "START", "NO PARAMS");

    Map<String, String> filters = new HashMap<>();
    filters.put("id", currentDepositCard.getId());

    Map<String, Object> fieldsToUpdate = new HashMap<>();
    if (!currentDepositCard.getValidity().equals(providerDepositCard.getValidity())) {
      logService.info(
          requestId,
          "L",
          "providerDepositCard.getValidity",
          String.valueOf(providerDepositCard.getValidity()));
      fieldsToUpdate.put("validity", String.valueOf(providerDepositCard.getValidity()));
      currentDepositCard.setValidity(providerDepositCard.getValidity());
    }

    if (currentDepositCard.isActive() != providerDepositCard.isActive()) {
      logService.info(
          requestId,
          "L",
          "providerDepositCard.isActive",
          String.valueOf(providerDepositCard.isActive()));
      fieldsToUpdate.put("active", providerDepositCard.isActive());
      currentDepositCard.setActive(providerDepositCard.isActive());
    }

    if (fieldsToUpdate.size() > 0) {
      databaseService.updateRecord(
          requestId,
          account,
          application,
          filters,
          fieldsToUpdate,
          WriteConcern.UNACKNOWLEDGED,
          DepositCard.class);
    }

    return currentDepositCard;
  }

  @Override
  public DepositCard updateDepositCard(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      Map<String, ?> fieldsToUpdate) {

    Map<String, String> filters = new HashMap<>();
    filters.put("id", depositCard.getId());

    databaseService.updateRecord(
        requestId,
        account,
        application,
        filters,
        fieldsToUpdate,
        WriteConcern.UNACKNOWLEDGED,
        DepositCard.class);
    return depositCard;
  }
}
