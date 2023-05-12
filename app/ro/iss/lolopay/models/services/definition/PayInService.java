package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.PayInImplementation;

@ImplementedBy(PayInImplementation.class)
public interface PayInService {
  public PayIn getPayIn(String requestId, Account account, String payInId);

  public PayIn getPayInByProviderId(String requestId, Account account, String providerId);

  public void savePayIn(String requestId, Account account, PayIn payIn);
}
