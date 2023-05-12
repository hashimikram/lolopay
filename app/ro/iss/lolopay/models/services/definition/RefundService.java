package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.RefundImplementation;

@ImplementedBy(RefundImplementation.class)
public interface RefundService {
  public Refund getRefund(String requestId, Account account, String refundId);

  public Refund getRefundByProviderId(String requestId, Account account, String providerId);

  public void saveRefund(String requestId, Account account, Refund refund);

  public PaginatedList getRefunds(String requestId, Account account, int page, int pageSize);
}
