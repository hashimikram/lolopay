package ro.iss.lolopay.programs;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class InitRemoveBankCardFields {
  private static LogService logService;

  private static DatabaseService databaseService;

  private static Datastore datastore;

  private static Account account;

  public static void main(String[] args) {

    logService = new LogImplementation();

    databaseService = new DatabaseImplementation(logService);

    account = databaseService.getMainConnection().createQuery(Account.class).asList().get(0);
    datastore = databaseService.getConnection(account.getId().toString());
    removeBankCardField();
    deleteAllTransactions();
  }

  private static void removeBankCardField() {

    Query<BankCard> query = datastore.createQuery(BankCard.class).disableValidation();
    query.or(
        query.criteria("retrievedTransactionsDate").exists(),
        query.criteria("transactionDates").exists());

    UpdateOperations<BankCard> operations =
        datastore.createUpdateOperations(BankCard.class).disableValidation();
    operations.unset("retrievedTransactionsDate");
    operations.unset("transactionDates");

    UpdateOptions updateOptions = new UpdateOptions();
    updateOptions.bypassDocumentValidation(true);
    updateOptions.multi(true);

    datastore.update(query, operations, updateOptions);
  }

  private static void deleteAllTransactions() {

    Query<BankCardTransaction> query =
        datastore.createQuery(BankCardTransaction.class).disableValidation();
    datastore.delete(query);
  }
}
