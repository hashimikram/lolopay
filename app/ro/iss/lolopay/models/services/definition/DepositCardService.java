package ro.iss.lolopay.models.services.definition;

import java.util.Map;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.DepositCardImplementation;

@ImplementedBy(DepositCardImplementation.class)
public interface DepositCardService {
  public void saveDepositCard(String requestId, Account account, DepositCard depositCard);

  public DepositCard getDepositCard(String requestId, Account account, String userId);

  public DepositCard getDepositCardById(String requestId, Account account, String cardId);

  public DepositCard getDepositCardByProviderId(
      String requestId, Account account, String cardProviderId);

  public DepositCard updateDepositCardValidity(
      String requestId,
      Account account,
      Application application,
      DepositCard currentDepositCard,
      DepositCard providerDepositCard);

  public DepositCard updateDepositCard(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      Map<String, ?> fieldsToUpdate);
}
