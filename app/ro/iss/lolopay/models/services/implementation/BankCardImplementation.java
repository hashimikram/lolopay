package ro.iss.lolopay.models.services.implementation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.mongodb.WriteConcern;
import play.Logger;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankCardService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class BankCardImplementation implements BankCardService {
  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public BankCardImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public BankCard getBankCard(String requestId, Account account, String bankCardId) {

    return (BankCard) databaseService.getRecord(requestId, account, bankCardId, BankCard.class);
  }

  @Override
  public BankCard getBankCardByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (BankCard) databaseService.getRecord(requestId, account, filters, BankCard.class);
  }

  @Override
  public void saveBankCard(String requestId, Account account, BankCard bankCard) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(bankCard);
  }

  @Override
  public void updateBankCard(
      String requestId,
      Account account,
      Application application,
      Map<String, Object> bankCardFilters,
      Map<String, Object> bankCardFields,
      WriteConcern writeConcern) {

    Logger.of(this.getClass()).debug("updateBankCard: start call db service");

    // update
    databaseService.updateRecord(
        requestId,
        account,
        application,
        bankCardFilters,
        bankCardFields,
        writeConcern,
        BankCard.class);
  }

  /**
   * Returns long value of last second of yesterday, if input parameter is today
   *
   * @param timestamp
   */
  @Override
  public long getYesterdayLastSecond(long timestamp) {

    LocalDate today = LocalDate.now(); // e.x. 2017-01-30
    LocalDate timestampLocalDate =
        Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("GMT")).toLocalDate();

    // check if timestamp is today and if true return yesterday
    if (today.equals(timestampLocalDate)) {
      LocalDate yesterday = LocalDate.now(ZoneId.of("GMT")).minusDays(1);
      ZonedDateTime zonedYesterdayDateTime =
          LocalDateTime.of(yesterday, LocalTime.MAX).atZone(ZoneId.of("GMT"));
      return zonedYesterdayDateTime.toInstant().toEpochMilli() / 1000L;
    }
    return timestamp;
  }
}
