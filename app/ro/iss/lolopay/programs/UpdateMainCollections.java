package ro.iss.lolopay.programs;

import java.util.ArrayList;
import java.util.Arrays;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class UpdateMainCollections {
  private static void createCollection(String className) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    Class<?> clazz;
    try {
      // create account in the system
      databaseService.getMorphia().mapPackage(className);

      clazz = Class.forName(className);
      Object collection = clazz.newInstance();

      databaseService.getMainConnection().save(collection);
      databaseService.getMainConnection().ensureIndexes();
      databaseService.getMainConnection().delete(collection);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {

    // define collection classes
    ArrayList<String> collectionClasses =
        new ArrayList<String>(
            Arrays.asList(
                "ro.iss.lolopay.models.main.Account",
                "ro.iss.lolopay.models.main.ApplicationError",
                "ro.iss.lolopay.models.main.Callback",
                "ro.iss.lolopay.models.main.Counter",
                "ro.iss.lolopay.models.main.FailedCallback",
                "ro.iss.lolopay.models.main.ProcessedCallback",
                "ro.iss.lolopay.models.main.RequestHistory",
                "ro.iss.lolopay.models.main.Session"));

    // iterate each class and create collection
    for (String collectionClass : collectionClasses) {
      createCollection(collectionClass);
    }

    // show message
    System.out.println("Update Completed");
  }
}
