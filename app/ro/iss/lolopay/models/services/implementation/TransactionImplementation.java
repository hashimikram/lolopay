package ro.iss.lolopay.models.services.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.Query;
import com.mongodb.WriteConcern;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.TransactionService;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;

@Singleton
public class TransactionImplementation implements TransactionService {
  private final DatabaseService databaseService;

  private final LogService logService;

  /** Log singleton creation moment */
  @Inject
  public TransactionImplementation(DatabaseService databaseService, LogService logService) {

    this.databaseService = databaseService;
    this.logService = logService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public PaginatedList getTransactionsPerWallet(
      String requestId, Account account, String walletId, int page, int pageSize) {

    Query<? extends TableCollection> query =
        databaseService.getConnection(account.getId().toString()).createQuery(Transaction.class);

    // define criteria list for wallet id
    List<Criteria> orWalletCriteriaList = new ArrayList<Criteria>();
    orWalletCriteriaList.add(query.criteria("debitedWalletId").equal(walletId));
    orWalletCriteriaList.add(query.criteria("creditedWalletId").equal(walletId));

    // build final query: (debitedWalletId = ? OR creditedWalletId = ?)
    query.or(orWalletCriteriaList.toArray(new Criteria[] {}));

    // order
    query.order("-createdAt");

    // return result
    return databaseService.getRecords(requestId, query, page, pageSize);
  }

  @Override
  public PaginatedList getTransactionsPerWalletNoFee(
      String requestId, Account account, String walletId, int page, int pageSize, String sort) {

    Query<? extends TableCollection> query =
        databaseService.getConnection(account.getId().toString()).createQuery(Transaction.class);

    // define criteria list for type
    List<Criteria> orTypeCriteriaList = new ArrayList<Criteria>();
    orTypeCriteriaList.add(query.criteria("type").equal(TransactionType.PAYIN));
    orTypeCriteriaList.add(query.criteria("type").equal(TransactionType.PAYOUT));
    orTypeCriteriaList.add(query.criteria("type").equal(TransactionType.TRANSFER));

    // define criteria list for wallet id
    List<Criteria> orWalletCriteriaList = new ArrayList<Criteria>();
    orWalletCriteriaList.add(query.criteria("debitedWalletId").equal(walletId));
    orWalletCriteriaList.add(query.criteria("creditedWalletId").equal(walletId));

    // build final query: (debitedWalletId = ? OR creditedWalletId = ?) AND ((type = PAYIN) OR (type
    // = PAYOUT) OR (type = TRANSFER))
    query.and(
        query.or(orWalletCriteriaList.toArray(new Criteria[] {})),
        query.or(orTypeCriteriaList.toArray(new Criteria[] {})));

    if (StringUtils.isNotBlank(sort)) {
      logService.debug(requestId, "IN", "sort", sort);
      String[] sortCols = sort.split(":");
      if (sortCols.length == 2) {
        String colName = sortCols[0];
        String ordering = sortCols[1];
        String orderByCrit = ("desc".equalsIgnoreCase(ordering) ? "-" : "") + colName;
        if (!"createdAt".equalsIgnoreCase(colName)) {
          // add also createdAt as second ordering criteria, if not already present
          orderByCrit += ",-createdAt";
        }
        // logService.debug(requestId, "TransactionImplementation", "getTransactionsPerWalletNoFee",
        // "L", "orderByCrit", orderByCrit);
        query.order(orderByCrit);
      }
    } else {
      // set default query sort order
      query.order("-createdAt");
    }

    // return result
    return databaseService.getRecords(requestId, query, page, pageSize);
  }

  @Override
  public void updateTransaction(
      String requestId,
      Account account,
      Application application,
      Map<String, Object> transactionFilters,
      Map<String, Object> transactionFields) {

    Logger.of(this.getClass()).debug("updateTransaction: start call db service");
    // update
    databaseService.updateRecord(
        requestId,
        account,
        application,
        transactionFilters,
        transactionFields,
        WriteConcern.UNACKNOWLEDGED,
        Transaction.class);
  }

  @Override
  public Transaction getTransaction(String requestId, Account account, String transactionId) {

    // return result
    return (Transaction)
        databaseService.getRecord(requestId, account, transactionId, Transaction.class);
  }
}
