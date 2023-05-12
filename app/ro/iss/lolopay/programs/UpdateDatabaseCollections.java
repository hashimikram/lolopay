package ro.iss.lolopay.programs;

import java.util.ArrayList;
import java.util.Arrays;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class UpdateDatabaseCollections {
  private static String mainAccountIdentifier;

  public static void main(String[] args) {

    // createClientDatabase
    mainAccountIdentifier = "5989e241f5b0af2f948ba09f";

    // define collection classes
    ArrayList<String> collectionClasses =
        new ArrayList<String>(
            Arrays.asList( //
                "ro.iss.lolopay.models.database.AccountSettings", //
                "ro.iss.lolopay.models.database.Application", //
                "ro.iss.lolopay.models.database.ApplicationActivity", //
                "ro.iss.lolopay.models.database.BankAccount", //
                "ro.iss.lolopay.models.database.BankCard", //
                "ro.iss.lolopay.models.database.BankCardWallet", //
                "ro.iss.lolopay.models.database.DepositCard", //
                "ro.iss.lolopay.models.database.CompanyBankCardTransaction", //
                "ro.iss.lolopay.models.database.CompanyBankCardWallet", //
                "ro.iss.lolopay.models.database.Document", //
                "ro.iss.lolopay.models.database.UboDeclaration", //
                "ro.iss.lolopay.models.database.Transaction", //
                "ro.iss.lolopay.models.database.User", //
                "ro.iss.lolopay.models.database.Wallet")); //

    // iterate each class and create collection
    for (String collectionClass : collectionClasses) {
      createCollection(collectionClass);
    }

    // show message
    System.out.println("Update Client Database Completed");
  }

  @SuppressWarnings("unused")
  private static void createClientDatabase(String dbName, String dbUsername, String dbPassword) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    // create database for new account
    databaseService.createDatabase(dbName, dbUsername, dbPassword);
  }

  private static void createCollection(String className) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    Class<?> clazz;
    try {
      // create account in the system
      databaseService.getMorphia().mapPackage(className);

      clazz = Class.forName(className);
      Object collection = clazz.newInstance();

      databaseService.getConnection(mainAccountIdentifier).save(collection);
      databaseService.getConnection(mainAccountIdentifier).ensureIndexes();
      databaseService.getConnection(mainAccountIdentifier).delete(collection);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
