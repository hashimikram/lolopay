package ro.iss.lolopay.programs;

import java.util.ArrayList;
import java.util.List;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.classes.FinancialProvider;
import ro.iss.lolopay.models.classes.ProviderDetail;
import ro.iss.lolopay.models.classes.ProviderOperation;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.ApplicationError;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class InitMainDinarData {

  public static void main(String[] args) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    // create main account
    createMainAccount(databaseService);

    // create application errors
    createApplicationErrors(databaseService);
  }

  private static void createMainAccount(DatabaseService databaseService) {

    System.out.println("createMainAccount start");

    List<ProviderDetail> providerDetails = new ArrayList<ProviderDetail>();
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEUSER");
    addFinancialProvider(providerDetails, "INTERNAL", "SAVEUSER");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATELEGALUSER");
    addFinancialProvider(providerDetails, "INTERNAL", "SAVELEGALUSER");
    addFinancialProvider(providerDetails, "INTERNAL", "GETUSER");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEWALLET");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEDOCUMENT");
    addFinancialProvider(providerDetails, "INTERNAL", "CERATEDOCUMENTPAGE");
    addFinancialProvider(providerDetails, "INTERNAL", "SUBMITDOCUMENT");
    addFinancialProvider(providerDetails, "INTERNAL", "GETDOCUMENT");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEUBODECLARATION");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEUBO");
    addFinancialProvider(providerDetails, "INTERNAL", "SUBMITUBODECLARATION");
    addFinancialProvider(providerDetails, "INTERNAL", "UPDATEUBO");
    addFinancialProvider(providerDetails, "INTERNAL", "VIEWUBODECLARATION");
    addFinancialProvider(providerDetails, "INTERNAL", "VIEWUBO");
    addFinancialProvider(providerDetails, "INTERNAL", "LISTUBODECLARATIONS");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEBANKACCOUNT");
    addFinancialProvider(providerDetails, "INTERNAL", "DEACTIVATEBANKACCOUNT");
    addFinancialProvider(providerDetails, "INTERNAL", "PAYIN");
    addFinancialProvider(providerDetails, "INTERNAL", "PAYOUT");
    addFinancialProvider(providerDetails, "INTERNAL", "TRANSFER");
    addFinancialProvider(providerDetails, "INTERNAL", "REFUND");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATEBANKCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "UPGRADEBANKCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "CHANGECARDSTATUS");
    addFinancialProvider(providerDetails, "INTERNAL", "ADDCARDCURRENCY");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARDWALLET");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARDWALLETTRANSACTIONS");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARDNUMBER");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARDEXPDATE");
    addFinancialProvider(providerDetails, "INTERNAL", "GETCARDCVV");
    addFinancialProvider(providerDetails, "INTERNAL", "TRANSFERTOCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "SENDCARDPIN");
    addFinancialProvider(providerDetails, "INTERNAL", "LOCKUNLOCKCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "TRANSFERFROMCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "UPDATEBANKCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "EXECUTEBANKPAYMENT");
    addFinancialProvider(providerDetails, "INTERNAL", "GETFXQUOTE");
    addFinancialProvider(providerDetails, "INTERNAL", "FXTRADE");
    addFinancialProvider(providerDetails, "INTERNAL", "REPLACECARD");
    addFinancialProvider(providerDetails, "INTERNAL", "CREATECARDREGISTRATION");
    addFinancialProvider(providerDetails, "INTERNAL", "UPDATECARDREGISTRATION");
    addFinancialProvider(providerDetails, "INTERNAL", "DIRECTPAYIN");
    addFinancialProvider(providerDetails, "INTERNAL", "GETDEPOSITCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "DEACTIVATEDEPOSITCARD");
    addFinancialProvider(providerDetails, "INTERNAL", "GETUSERSDISPUTES");

    Account mainAccount = new Account();
    mainAccount.setId("5c9b72b49c386c33eb2b1987");
    mainAccount.setAccountName("DINARE LTD");
    mainAccount.setAccountRegistrationNumber("1234567");
    mainAccount.setAccountAddressLine1("123 Main Street");
    mainAccount.setAccountAddressCity("Baghdad");
    mainAccount.setAccountAddressCountry("IQ");
    mainAccount.setAccountEmail("contact@dinare.com");
    mainAccount.setDatabaseName("iss_lolopay_dinare");
    mainAccount.setDatabaseUsername("dinareadmin");
    mainAccount.setDatabasePassword("U$cqbDz7N6p3");
    mainAccount.setAccountId("dinare");
    mainAccount.setAccountLogo("");
    mainAccount.setAccountTaxId("");
    mainAccount.setAccountAddressLine2("");
    mainAccount.setAccountAddressCounty("Baghdad");
    mainAccount.setProviderDetails(providerDetails);
    mainAccount.updateAudit(getApplicationStamp());
    databaseService.getMainConnection().save(mainAccount);

    System.out.println("createMainAccount end");
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

  private static ApplicationStamp getApplicationStamp() {

    ApplicationStamp systemApplication = new ApplicationStamp();
    systemApplication.setApplicationName("CORE SYSTEM");
    systemApplication.setApplicationEmail(ConfigFactory.load().getString("application.email"));

    return systemApplication;
  }

  private static void addFinancialProvider(
      List<ProviderDetail> providerDetails, String financialProvider, String operation) {

    ProviderDetail providerDetail = new ProviderDetail();
    providerDetail.setFinancialProvider(FinancialProvider.valueOf(financialProvider));
    providerDetail.setProviderOperation(ProviderOperation.valueOf(operation));

    providerDetails.add(providerDetail);
  }
}
