package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.query.Query;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.RefundService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class RefundImplementation implements RefundService {
  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public RefundImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public Refund getRefund(String requestId, Account account, String refundId) {

    // return result
    return (Refund) databaseService.getRecord(requestId, account, refundId, Refund.class);
  }

  @Override
  public Refund getRefundByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (Refund) databaseService.getRecord(requestId, account, filters, Refund.class);
  }

  @Override
  public void saveRefund(String requestId, Account account, Refund refund) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(refund);
  }

  @Override
  public PaginatedList getRefunds(String requestId, Account account, int page, int pageSize) {

    Query<? extends TableCollection> query =
        databaseService.getConnection(account.getId().toString()).createQuery(Refund.class);

    query.criteria("nature").equal(TransactionNature.REFUND);

    // order
    query.order("-createdAt");

    // return result
    return databaseService.getRecords(requestId, query, page, pageSize);
  }
}
