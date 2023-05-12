package ro.iss.lolopay.services.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.classes.ConnectionDetails;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Counter;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;

@Singleton
public class DatabaseImplementation implements DatabaseService {

  LogService logService;

  /** A pool of connections which will store all application connections */
  private final HashMap<String, Datastore> connectionPool;

  private Morphia morphiaInstance;

  private final Config config = ConfigFactory.load();

  /** Database singleton created */
  @Inject
  public DatabaseImplementation(LogService logService) {

    this.connectionPool = new HashMap<String, Datastore>();
    this.morphiaInstance = new Morphia();
    this.logService = logService;
  }

  /**
   * Return main database connection
   *
   * @return
   */
  @Override
  public Datastore getMainConnection() {

    return getConnection("main");
  }

  /**
   * Get one db connection based on identifier
   *
   * @param connectionIdentifier
   * @return
   */
  @Override
  public Datastore getConnection(String connectionIdentifier) {

    return connectionPool.computeIfAbsent(
        connectionIdentifier,
        (cIdKey) -> {
          return createConnection(cIdKey);
        });
  }

  /**
   * Create and update a new counter for a sequence
   *
   * @param collection Collection name to create sequence for
   * @return
   */
  @Override
  public int getNext(String collection) {

    // create query for counter identification
    Query<Counter> allCountersQuery = getMainConnection().createQuery(Counter.class);
    allCountersQuery.filter("tableName =", collection);

    // create update rule for table
    UpdateOperations<Counter> updateOp =
        getMainConnection().createUpdateOperations(Counter.class).inc("counter");

    // call find and modify counter per query
    Counter seq = getMainConnection().findAndModify(allCountersQuery, updateOp);

    // create a sequence record for your collection if not found
    if (seq == null) {
      seq = new Counter();
      seq.setTableName(collection);
      seq.setCounter(101);
      getMainConnection().save(seq);
    }

    return seq.getCounter();
  }

  @Override
  public String getMongoId() {

    ObjectId id = new ObjectId();
    return id.toString();
  }

  /**
   * Create database, to be used when new Account is created
   *
   * @param databaseName
   * @param databaseUsername
   * @param databaseUsernamePassword
   */
  @Override
  public void createDatabase(
      String databaseName, String databaseUsername, String databaseUsernamePassword) {

    // get connection details for administrator connection
    ConnectionDetails connectionDetails = getConnectionDetails("admin");

    // get main connection
    MongoClient mongoClient = getMongoClient(connectionDetails);

    MongoDatabase db = mongoClient.getDatabase(databaseName);

    final BasicDBObject dbAdmin = new BasicDBObject("role", "dbAdmin").append("db", databaseName);
    final BasicDBObject dbOwner = new BasicDBObject("role", "dbOwner").append("db", databaseName);
    final BasicDBObject read = new BasicDBObject("role", "read").append("db", databaseName);
    final BasicDBObject readWrite =
        new BasicDBObject("role", "readWrite").append("db", databaseName);
    final BasicDBObject userAdmin =
        new BasicDBObject("role", "userAdmin").append("db", databaseName);

    List<BasicDBObject> listRoles = new ArrayList<BasicDBObject>();
    listRoles.add(dbAdmin);
    listRoles.add(dbOwner);
    listRoles.add(read);
    listRoles.add(readWrite);
    listRoles.add(userAdmin);

    final BasicDBObject createUserCommand =
        new BasicDBObject("createUser", databaseUsername)
            .append("pwd", databaseUsernamePassword)
            .append("roles", listRoles);

    db.runCommand(createUserCommand);
  }

  /** Get provider object */
  @Override
  public Morphia getMorphia() {

    // create a new Morphia instance each time this method is call
    // we need a new instance to have a fresh connection on each mapping
    this.morphiaInstance = new Morphia();

    return this.morphiaInstance;
  }

  /** Get one record by record id */
  @Override
  public TableCollection getRecord(
      String requestId,
      Account account,
      String recordId,
      Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "recordId", recordId);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass);

    // add field filter
    query.field("id").equal(recordId);

    // find options - one record only
    FindOptions findOptions = new FindOptions();
    findOptions.skip(0);
    findOptions.limit(1);

    logService.debug(requestId, "L", "query", query);

    query.get(findOptions);

    // execute query
    List<? extends TableCollection> queryResult = query.asList(findOptions);

    // if nothing is find return null
    if (queryResult.size() != 1) return null;

    logService.debug(requestId, "L", "queryResult", queryResult.size());

    // return result
    return queryResult.get(0);
  }

  @Override
  public TableCollection getRecord(
      String requestId,
      Account account,
      Map<String, ?> filters,
      Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "filters", filters);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass);

    // add field filter
    Iterator<?> it = filters.entrySet().iterator();
    while (it.hasNext()) {
      // get filter from map
      @SuppressWarnings("unchecked")
      Map.Entry<String, ?> pair = (Entry<String, ?>) it.next();

      // log filters
      logService.debug(requestId, "L", pair.getKey(), pair.getValue());

      // add filter
      query.field(pair.getKey()).equal(pair.getValue());
    }

    // find options - one record only
    FindOptions findOptions = new FindOptions();
    findOptions.skip(0);
    findOptions.limit(1);

    logService.debug(requestId, "L", "query", query);

    // execute query
    List<? extends TableCollection> queryResult = query.asList(findOptions);

    // if nothing is find return null
    if (queryResult.size() != 1) return null;

    logService.debug(requestId, "L", "queryResult", queryResult.size());

    // return result
    return queryResult.get(0);
  }

  @Override
  public TableCollection getMainRecord(
      String requestId, String recordId, Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "recordId", recordId);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query = getMainConnection().createQuery(modelClass);

    // add field filter
    query.field("id").equal(recordId);

    // find options - one record only
    FindOptions findOptions = new FindOptions();
    findOptions.skip(0);
    findOptions.limit(1);

    logService.debug(requestId, "L", "query", query);

    // execute query
    List<? extends TableCollection> queryResult = query.asList(findOptions);

    // if nothing is find return null
    if (queryResult.size() != 1) return null;

    logService.debug(requestId, "L", "queryResult", queryResult.size());

    // return result
    return queryResult.get(0);
  }

  @Override
  public TableCollection getMainRecord(
      String requestId, Map<String, ?> filters, Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "filters", filters);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query = getMainConnection().createQuery(modelClass);

    // add field filter
    Iterator<?> it = filters.entrySet().iterator();
    while (it.hasNext()) {
      // get filter from map
      @SuppressWarnings("unchecked")
      Map.Entry<String, ?> pair = (Entry<String, ?>) it.next();

      // log filters
      logService.debug(requestId, "L", pair.getKey(), pair.getValue());

      // add filter
      query.field(pair.getKey()).equal(pair.getValue());
    }

    // find options - one record only
    FindOptions findOptions = new FindOptions();
    findOptions.skip(0);
    findOptions.limit(1);

    logService.debug(requestId, "L", "query", query);

    // execute query
    List<? extends TableCollection> queryResult = query.asList(findOptions);

    // if nothing is find return null
    if (queryResult.size() != 1) return null;

    logService.debug(requestId, "L", "queryResult", queryResult.size());

    // return result
    return queryResult.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public PaginatedList getRecords(
      String requestId,
      Account account,
      Map<String, ?> filters,
      QueryFieldOperator queryFieldOperator,
      int page,
      int pageSize,
      Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "filters", filters);
    logService.debug(requestId, "IN", "queryFieldOperator", queryFieldOperator);
    logService.debug(requestId, "IN", "page", page);
    logService.debug(requestId, "IN", "pageSize", pageSize);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass);

    // define criteria list
    List<Criteria> criteriaList = new ArrayList<Criteria>();

    // add field filter
    if ((filters != null) && (filters.size() > 0)) {
      Iterator<?> it = filters.entrySet().iterator();
      while (it.hasNext()) {
        // get filter from map
        Map.Entry<String, ?> pair = (Entry<String, ?>) it.next();

        // log filters
        logService.debug(requestId, "L", pair.getKey(), pair.getValue());

        // add filter
        criteriaList.add(query.criteria(pair.getKey()).equal(pair.getValue()));
      }
    }

    // if we have any criteria in the filters
    if (criteriaList.size() > 0) {
      if (queryFieldOperator.equals(QueryFieldOperator.AND)) {
        query.and(criteriaList.toArray(new Criteria[] {}));
      } else if (queryFieldOperator.equals(QueryFieldOperator.OR)) {
        query.or(criteriaList.toArray(new Criteria[] {}));
      }
    }
    logService.debug(requestId, "L", "query", query);

    return executeQuery(requestId, query, page, pageSize);
  }

  @Override
  public PaginatedList getRecords(
      String requestId, Query<? extends TableCollection> query, int page, int pageSize) {

    logService.debug(requestId, "IN", "query", query);
    logService.debug(requestId, "IN", "page", page);
    logService.debug(requestId, "IN", "pageSize", pageSize);

    return executeQuery(requestId, query, page, pageSize);
  }

  @Override
  public List<? extends TableCollection> getAllRecords(
      String requestId, Account account, Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass).order("-createdAt");

    logService.debug(requestId, "L", "query", query);

    // execute and return return result
    return query.asList();
  }

  @Override
  public List<? extends TableCollection> getAllRecords(
      String requestId,
      Account account,
      List<String> listOfIds,
      Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "listOfIds", listOfIds);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass).order("-createdAt");

    // add filters
    query.field("id").in(listOfIds);

    logService.debug(requestId, "L", "query", query);

    // execute and return return result
    return query.asList();
  }

  @Override
  public List<? extends TableCollection> getMainAllRecords(
      String requestId, Class<? extends TableCollection> modelClass) {

    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create query
    Query<? extends TableCollection> query =
        getMainConnection().createQuery(modelClass).order("-createdAt");

    logService.debug(requestId, "L", "query", query);

    // execute and return return result
    return query.asList();
  }

  @Override
  public List<? extends TableCollection> getMainAllRecords(
      String requestId, Query<? extends TableCollection> query) {

    logService.debug(requestId, "IN", "query", query);

    return executeQuery(requestId, query);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void updateRecord(
      String requestId,
      Account account,
      Application application,
      Map<String, ?> filters,
      Map<String, ?> fieldsToUpdate,
      WriteConcern writeConcern,
      Class modelClass) {

    logService.debug(requestId, "IN", "filters", filters);
    logService.debug(requestId, "IN", "fieldsToUpdate", fieldsToUpdate);
    logService.debug(requestId, "IN", "writeConcern", writeConcern);
    logService.debug(requestId, "IN", "modelClass", modelClass.getSimpleName());

    // create update operation - we do not need full overwrite of record
    UpdateOperations<TableCollection> updateOperation =
        getConnection(account.getId().toString()).createUpdateOperations(modelClass);

    // iterate fields to update and enhance update operation
    if ((fieldsToUpdate != null) && (fieldsToUpdate.size() > 0)) {
      Iterator<?> it = fieldsToUpdate.entrySet().iterator();
      while (it.hasNext()) {
        // get filter from map
        Map.Entry<String, ?> pair = (Entry<String, ?>) it.next();

        // log filters
        logService.debug(requestId, "L", "update-" + pair.getKey(), pair.getValue());

        // add filter
        updateOperation.set(pair.getKey(), pair.getValue());
      }
    }

    // add extra audit fields
    updateOperation.set("updatedAt", (System.currentTimeMillis() / 1000L));
    updateOperation.set("updatedBy", application.getApplicationStamp());

    // create query
    Query<TableCollection> query =
        getConnection(account.getId().toString()).createQuery(modelClass);

    // add field filter
    if ((filters != null) && (filters.size() > 0)) {
      Iterator<?> it = filters.entrySet().iterator();
      while (it.hasNext()) {
        // get filter from map
        Map.Entry<String, ?> pair = (Entry<String, ?>) it.next();

        // log filters
        logService.debug(requestId, "L", "filter-" + pair.getKey(), pair.getValue());

        // add filter
        query.field(pair.getKey()).equal(pair.getValue());
      }
    }

    logService.debug(requestId, "L", "query", query);

    // update records on specified fields
    // UpdateResults updateResults = getConnection(account.getId().toString()).update(query,
    // updateOperation, false, writeConcern);
    getConnection(account.getId().toString()).update(query, updateOperation, false, writeConcern);
  }

  private List<? extends TableCollection> executeQuery(
      String requestId, Query<? extends TableCollection> query) {

    logService.debug(requestId, "IN", "query", query);

    int allowedNumberOfRecords = 0;

    // test limit
    if (config.hasPath("application.maxRecordsToBeRetrievedFromDB")) {
      allowedNumberOfRecords = config.getInt("application.maxRecordsToBeRetrievedFromDB");
    } else {
      allowedNumberOfRecords = 100;
    }

    // find options
    FindOptions findOptions = new FindOptions();
    findOptions.limit(allowedNumberOfRecords);

    logService.debug(requestId, "L", "query", query);

    // execute query
    List<? extends TableCollection> resultList = query.asList(findOptions);

    logService.debug(requestId, "L", "resultList", resultList.size());

    // return paginated full result
    return resultList;
  }

  private PaginatedList executeQuery(
      String requestId, Query<? extends TableCollection> query, int page, int pageSize) {

    logService.debug(requestId, "IN", "query", query);
    logService.debug(requestId, "IN", "page", page);
    logService.debug(requestId, "IN", "pageSize", pageSize);

    // check if negative values for page and page size
    if (page < 1) {
      page = 1;
    }

    int allowedNumberOfRecords = 0;

    // test limit
    if (config.hasPath("application.maxRecordsToBeRetrievedFromDB")) {
      allowedNumberOfRecords = config.getInt("application.maxRecordsToBeRetrievedFromDB");
    } else {
      allowedNumberOfRecords = 100;
    }

    if ((pageSize > allowedNumberOfRecords) || (pageSize < 0)) {
      pageSize = allowedNumberOfRecords;
    }

    // find options
    FindOptions findOptions = new FindOptions();
    findOptions.skip((page - 1) * pageSize);
    findOptions.limit(pageSize);

    logService.debug(requestId, "L", "query", query);

    // execute query
    List<? extends TableCollection> resultList = query.asList(findOptions);

    logService.debug(requestId, "L", "resultList", resultList.size());

    PaginatedList paginatedList = new PaginatedList();

    // set page number with the same value
    paginatedList.setPage(Long.valueOf(page));

    // set page size with the actual number of records, no matter what is the maximum specified sie
    // sent in parameters
    paginatedList.setPageSize(Long.valueOf(resultList.size()));

    // calculate total records of the query
    paginatedList.setTotalRecords(query.count());

    // calculate total pages
    // Double totalPages = Math.ceil(((double) query.count() / (double) pageSize));
    // paginatedList.setTotalPages(totalPages.longValue());
    paginatedList.setTotalPages(Long.valueOf((query.count() / pageSize) + 1));

    // execute and return return result
    paginatedList.setList(resultList);

    // return paginated full result
    return paginatedList;
  }

  /**
   * Retrieve connection details based on identifier (main/admin are reserved, all the others are
   * searched within DB)
   *
   * @param connectionIdentifier
   * @return
   */
  private ConnectionDetails getConnectionDetails(String connectionIdentifier) {

    String mongodbServer1Address = config.getString("mongodb.server1.address");
    String mongodbServer2Address = config.getString("mongodb.server2.address");
    String mongodbServer3Address = config.getString("mongodb.server3.address");

    ConnectionDetails connectionDetails = new ConnectionDetails();
    connectionDetails.setServer1Address(mongodbServer1Address);
    connectionDetails.setServer1Port(config.getInt("mongodb.server1.port"));

    if (!mongodbServer2Address.isEmpty() && !mongodbServer2Address.equals(mongodbServer1Address)) {
      connectionDetails.setServer2Address(config.getString("mongodb.server2.address"));
      connectionDetails.setServer2Port(config.getInt("mongodb.server2.port"));
    }

    if (!mongodbServer3Address.isEmpty() && !mongodbServer3Address.equals(mongodbServer1Address)) {
      connectionDetails.setServer3Address(config.getString("mongodb.server3.address"));
      connectionDetails.setServer3Port(config.getInt("mongodb.server3.port"));
    }

    if (connectionIdentifier.equals("main")) {
      return getMainConnectionDetails(connectionDetails);
    } else if (connectionIdentifier.equals("admin")) {
      return getAdminConnectionDetails(connectionDetails);
    } else {
      return getClientConnectionDetails(connectionIdentifier, connectionDetails);
    }
  }

  /**
   * Return main database connection details
   *
   * @return
   */
  private ConnectionDetails getMainConnectionDetails(ConnectionDetails connectionDetails) {

    connectionDetails.setDatabaseName(config.getString("mongodb.mainConnection.dbname"));
    connectionDetails.setDatabaseUsername(config.getString("mongodb.mainConnection.username"));
    connectionDetails.setDatabaseUsernamePassword(
        config.getString("mongodb.mainConnection.password"));

    return connectionDetails;
  }

  /**
   * Return admin database connection details
   *
   * @return
   */
  private ConnectionDetails getAdminConnectionDetails(ConnectionDetails connectionDetails) {

    // create a connection details object in order to retrieve a mongoDb
    // connection to database

    connectionDetails.setDatabaseName(config.getString("mongodb.adminConnection.dbname"));
    connectionDetails.setDatabaseUsername(config.getString("mongodb.adminConnection.username"));
    connectionDetails.setDatabaseUsernamePassword(
        config.getString("mongodb.adminConnection.password"));
    return connectionDetails;
  }

  /**
   * Retrieve client specific connection details by searching in Account table
   *
   * @param clientId
   * @return
   */
  private ConnectionDetails getClientConnectionDetails(
      String clientId, ConnectionDetails connectionDetails) {

    // create a connection details object in order to retrieve a mongoDb
    // connection to database

    // define query to retrieve account information
    FindOptions fo = new FindOptions();
    fo.skip(0);
    fo.limit(1);

    // define the query in old fashion way
    Query<Account> accounts = getMainConnection().createQuery(Account.class);
    accounts.filter("id = ", clientId);

    // execute query to retrieve client database details
    List<Account> accountsList = accounts.asList(fo);

    // create response and get his connection details
    if (accountsList.size() > 0) {
      connectionDetails.setDatabaseName(accountsList.get(0).getDatabaseName());
      connectionDetails.setDatabaseUsername(accountsList.get(0).getDatabaseUsername());
      connectionDetails.setDatabaseUsernamePassword(accountsList.get(0).getDatabasePassword());
    } else {
      // Logger.of(this.getClass()).error("getAdminConnectionDetails: account NOT found: " +
      // clientId);
    }

    return connectionDetails;
  }

  /**
   * Create a MongoDb connection client
   *
   * @param connectionDetails
   * @return
   */
  private MongoClient getMongoClient(ConnectionDetails connectionDetails) {

    // create list to store replica servers
    List<ServerAddress> replicaSet = new ArrayList<ServerAddress>();

    // create list to store replica set server connection details
    List<MongoCredential> credentials = new ArrayList<MongoCredential>();

    // set server 1 address and port
    ServerAddress replicaSetServer1 =
        new ServerAddress(
            connectionDetails.getServer1Address(), connectionDetails.getServer1Port());
    replicaSet.add(replicaSetServer1);

    // if configuration exists for server 2 as well
    if (!connectionDetails.getServer2Address().equals("")
        && connectionDetails.getServer2Port() != 0) {
      ServerAddress replicaSetServer2 =
          new ServerAddress(
              connectionDetails.getServer2Address(), connectionDetails.getServer2Port());
      replicaSet.add(replicaSetServer2);
    }

    // if configuration exists for server 3 as well
    if (!connectionDetails.getServer3Address().equals("")
        && connectionDetails.getServer3Port() != 0) {
      ServerAddress replicaSetServer3 =
          new ServerAddress(
              connectionDetails.getServer3Address(), connectionDetails.getServer3Port());
      replicaSet.add(replicaSetServer3);
    }

    // create credential object for this connection based on db username, db
    // name, and db username password
    MongoCredential credential =
        MongoCredential.createCredential(
            connectionDetails.getDatabaseUsername(),
            connectionDetails.getDatabaseName(),
            connectionDetails.getDatabaseUsernamePassword().toCharArray());
    credentials.add(credential);

    MongoClientOptions.Builder mongoClientOptionsBuilder = new MongoClientOptions.Builder();
    mongoClientOptionsBuilder.connectTimeout(config.getInt("mongodb.client.connectionTimeout"));

    // get connection
    return new MongoClient(replicaSet, credentials, mongoClientOptionsBuilder.build());
  }

  /**
   * Create connection based on some identifier (main/admin are reserved)
   *
   * @param connectionIdentifier
   * @return
   */
  private Datastore createConnection(String connectionIdentifier) {

    // get connection details for administrator connection
    ConnectionDetails connectionDetails = getConnectionDetails(connectionIdentifier);

    // get admin connection
    MongoClient mongoClient = getMongoClient(connectionDetails);

    // create and return data store
    return this.morphiaInstance.createDatastore(mongoClient, connectionDetails.getDatabaseName());
  }
}
