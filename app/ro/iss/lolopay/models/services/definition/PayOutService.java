package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.PayOutImplementation;

@ImplementedBy(PayOutImplementation.class)
public interface PayOutService {
  public PayOut getPayOut(String requestId, Account account, String payOutId);

  public PayOut getPayOutByProviderId(String requestId, Account account, String providerId);

  public void savePayOut(String requestId, Account account, PayOut payOut);
}
