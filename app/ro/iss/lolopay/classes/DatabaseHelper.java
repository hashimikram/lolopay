package ro.iss.lolopay.classes;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.typesafe.config.ConfigFactory;
import play.Logger;

public class DatabaseHelper {
  /**
   * Delete database, to be used when recreate database is on
   *
   * @param databaseName
   */
  public static void cleanDatabase() {

    Logger.of(DatabaseHelper.class.getName()).debug("cleanDatabase: start cleaning ...");

    // create list to store replica servers
    List<ServerAddress> replicaSet = new ArrayList<ServerAddress>();

    // create list to store replica set server connection details
    List<MongoCredential> credentials = new ArrayList<MongoCredential>();

    // set server 1 address and port
    ServerAddress replicaSetServer1 =
        new ServerAddress(
            ConfigFactory.load().getString("mongodb.server1.address"),
            ConfigFactory.load().getInt("mongodb.server1.port"));
    replicaSet.add(replicaSetServer1);

    if (ConfigFactory.load().hasPath("mongodb.server2.address")
        && ConfigFactory.load().hasPath("mongodb.server2.port")) {
      ServerAddress replicaSetServer2 =
          new ServerAddress(
              ConfigFactory.load().getString("mongodb.server2.address"),
              ConfigFactory.load().getInt("mongodb.server2.port"));
      replicaSet.add(replicaSetServer2);
    }

    if (ConfigFactory.load().hasPath("mongodb.server3.address")
        && ConfigFactory.load().hasPath("mongodb.server3.port")) {
      ServerAddress replicaSetServer3 =
          new ServerAddress(
              ConfigFactory.load().getString("mongodb.server3.address"),
              ConfigFactory.load().getInt("mongodb.server3.port"));
      replicaSet.add(replicaSetServer3);
    }

    // create credential object for this connection based on db username, db
    // name, and db username password
    MongoCredential credential =
        MongoCredential.createCredential(
            ConfigFactory.load().getString("mongodb.adminConnection.username"),
            ConfigFactory.load().getString("mongodb.adminConnection.dbname"),
            ConfigFactory.load().getString("mongodb.adminConnection.password").toCharArray());
    credentials.add(credential);

    // get main connection
    @SuppressWarnings("resource")
    MongoClient mongoClient = new MongoClient(replicaSet, credentials);

    // get main connection databases
    MongoIterable<String> allDatabases = mongoClient.listDatabaseNames();

    // iterate available databases
    for (String databaseName : allDatabases) {
      // skip all databases, just those which respect some local framework naming conventions
      if (!databaseName.startsWith("iss_lolopay")) {
        continue;
      }

      Logger.of(DatabaseHelper.class.getName())
          .debug("cleanDatabase: clean database <" + databaseName + ">");

      // connect to particular database
      MongoDatabase db = mongoClient.getDatabase(databaseName);

      // get all available collections
      MongoIterable<String> allDbCollections = db.listCollectionNames();

      // iterate available collections
      for (String collectionName : allDbCollections) {
        // drop specified connection
        db.getCollection(collectionName).drop();
        Logger.of(DatabaseHelper.class.getName())
            .debug("cleanDatabase: drop collection <" + collectionName + ">");
      }

      db.drop();
      Logger.of(DatabaseHelper.class.getName())
          .debug("cleanDatabase: drop databse <" + databaseName + ">");

      // check if we process main database - we do NOT delete users of framewok main database
      if (databaseName.equals(ConfigFactory.load().getString("mongodb.mainConnection.dbname"))) {
        continue;
      }

      // start delete associated users for this database
      Logger.of(DatabaseHelper.class.getName())
          .debug("cleanDatabase: start delete users for client databases ...");

      // create a new client to admin database
      MongoDatabase dbAdmin = mongoClient.getDatabase("admin");

      // get user collection
      MongoCollection<Document> adminUsersCollection = dbAdmin.getCollection("system.users");

      // create filter for deletion - all users of current database
      Bson filter = Filters.eq("db", databaseName);

      // create cursor for all db users
      FindIterable<Document> allSystemUsersToDelete = adminUsersCollection.find(filter);

      // iterate all users cursor
      for (Document systemUserDocument : allSystemUsersToDelete) {
        Logger.of(DatabaseHelper.class.getName())
            .debug(
                "cleanDatabase: will delete user  <"
                    + systemUserDocument.get("db").toString()
                    + " - "
                    + systemUserDocument.get("user").toString()
                    + ">");

        Bson filterDelete = Filters.eq("_id", systemUserDocument.get("_id"));
        adminUsersCollection.deleteOne(filterDelete);
      }
    }

    // do not delete admin / main users
    Logger.of(DatabaseHelper.class.getName()).debug("cleanDatabase: clean completed!");
  }
}
