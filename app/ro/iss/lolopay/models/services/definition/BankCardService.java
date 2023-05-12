package ro.iss.lolopay.models.services.definition;

import java.util.Map;
import com.google.inject.ImplementedBy;
import com.mongodb.WriteConcern;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.BankCardImplementation;

@ImplementedBy(BankCardImplementation.class)
public interface BankCardService {
  public BankCard getBankCard(String requestId, Account account, String bankCardId);

  public BankCard getBankCardByProviderId(String requestId, Account account, String providerId);

  public void saveBankCard(String requestId, Account account, BankCard bankCard);

  public void updateBankCard(
      String requestId,
      Account account,
      Application application,
      Map<String, Object> transactionFilters,
      Map<String, Object> transactionFields,
      WriteConcern writeConcern);

  public long getYesterdayLastSecond(long timestamp);
}
