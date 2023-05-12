package ro.iss.lolopay.services.definition;

import java.util.List;
import java.util.Map;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import com.google.inject.ImplementedBy;
import com.mongodb.WriteConcern;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;

@ImplementedBy(DatabaseImplementation.class)
public interface DatabaseService {
  /**
   * Get Morphia connection object
   *
   * @return
   */
  public Morphia getMorphia();

  /**
   * Get a connection Datastore to main database
   *
   * @return
   */
  public Datastore getMainConnection();

  /**
   * Get a connection Datastore to an account database
   *
   * @param connectionIdentifier
   * @return
   */
  public Datastore getConnection(String connectionIdentifier);

  /**
   * Get a new sequence per specified collection
   *
   * @param collection
   * @return
   */
  public int getNext(String collection);

  /**
   * Generate new MongoDb id
   *
   * @return
   */
  public String getMongoId();

  /**
   * Create a new database
   *
   * @param databaseName
   * @param databaseUsername
   * @param databaseUsernamePassword
   */
  public void createDatabase(
      String databaseName, String databaseUsername, String databaseUsernamePassword);

  /**
   * Get one record by id from an account database
   *
   * @param requestId
   * @param account
   * @param recordId
   * @param modelClass
   * @return
   */
  public TableCollection getRecord(
      String requestId,
      Account account,
      String recordId,
      Class<? extends TableCollection> modelClass);

  /**
   * Get one record by a list of filters from an account database
   *
   * @param requestId
   * @param account
   * @param filters
   * @param modelClass
   * @return
   */
  public TableCollection getRecord(
      String requestId,
      Account account,
      Map<String, ?> filters,
      Class<? extends TableCollection> modelClass);

  /**
   * Get one record by id from main database
   *
   * @param requestId
   * @param recordId
   * @param modelClass
   * @return
   */
  public TableCollection getMainRecord(
      String requestId, String recordId, Class<? extends TableCollection> modelClass);

  /**
   * Get one record by a list of filters from main database
   *
   * @param requestId
   * @param filters
   * @param modelClass
   * @return
   */
  public TableCollection getMainRecord(
      String requestId, Map<String, ?> filters, Class<? extends TableCollection> modelClass);

  /**
   * Get a list of records by a list of filters from an account database
   *
   * @param requestId
   * @param account
   * @param filters
   * @param queryFieldOperator
   * @param page
   * @param pageSize
   * @param modelClass
   * @return
   */
  public PaginatedList getRecords(
      String requestId,
      Account account,
      Map<String, ?> filters,
      QueryFieldOperator queryFieldOperator,
      int page,
      int pageSize,
      Class<? extends TableCollection> modelClass);

  /**
   * Get a list of records by a query
   *
   * @param requestId
   * @param query
   * @param page
   * @param pageSize
   * @return
   */
  public PaginatedList getRecords(
      String requestId, Query<? extends TableCollection> query, int page, int pageSize);

  /**
   * Get all records from a collection from an account database
   *
   * @param requestId
   * @param account
   * @param modelClass
   * @return
   */
  public List<? extends TableCollection> getAllRecords(
      String requestId, Account account, Class<? extends TableCollection> modelClass);

  /**
   * Get all records who match a list of database id's from an account database
   *
   * @param requestId
   * @param account
   * @param listOfIds
   * @param modelClass
   * @return
   */
  public List<? extends TableCollection> getAllRecords(
      String requestId,
      Account account,
      List<String> listOfIds,
      Class<? extends TableCollection> modelClass);

  /**
   * Get all records from a collection within main database
   *
   * @param requestId
   * @param modelClass
   * @return
   */
  public List<? extends TableCollection> getMainAllRecords(
      String requestId, Class<? extends TableCollection> modelClass);

  /**
   * Get all records from a collection within main database based on a query
   *
   * @param requestId
   * @param query
   * @return
   */
  public List<? extends TableCollection> getMainAllRecords(
      String requestId, Query<? extends TableCollection> query);

  /**
   * Update one record from an account collection based on provided filters. Only provided fields
   * are updated
   *
   * @param requestId
   * @param account
   * @param application
   * @param filters
   * @param fieldsToUpdate
   * @param writeConcern
   * @param modelClass
   */
  @SuppressWarnings("rawtypes")
  public void updateRecord(
      String requestId,
      Account account,
      Application application,
      Map<String, ?> filters,
      Map<String, ?> fieldsToUpdate,
      WriteConcern writeConcern,
      Class modelClass);
}
