package ro.iss.lolopay.programs;

import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class InitErrorsData {

  public static void main(String[] args) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    // create application errors
    createApplicationErrors(databaseService);
  }

  private static void createApplicationErrors(DatabaseService databaseService) {

    System.out.println("createApplicationErrors start");

    databaseService
        .getMainConnection()
        .delete(databaseService.getMainConnection().createQuery(ApplicationError.class));
    System.out.println("createApplicationErrors deleted previous application errors");

    int lastCategory = 0;
    int lastErrorIndex = 1;
    String lastGroup = "";

    java.lang.reflect.Field[] errorMessage = ErrorMessage.class.getDeclaredFields();

    for (java.lang.reflect.Field field : errorMessage) {
      String[] nameParts = field.getName().split("_");

      String currentGroup = nameParts[0].concat(nameParts[1]);

      if (!lastGroup.equals(currentGroup)) {
        lastCategory++;
        lastErrorIndex = 1;
      }

      // generate error code
      String generatedCode = "E" + lastCategory + "x" + lastErrorIndex;

      // update
      ApplicationError demoRecord = new ApplicationError();
      demoRecord.setErrorCode(generatedCode);
      demoRecord.setErrorKey(field.getName());
      databaseService.getMainConnection().save(demoRecord);

      // increase counters
      lastGroup = currentGroup;
      lastErrorIndex++;
    }

    System.out.println("createApplicationErrors end");
  }
}
