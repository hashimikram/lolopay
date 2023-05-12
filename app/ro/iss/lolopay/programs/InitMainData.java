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

public class InitMainData {

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
    addFinancialProvider(providerDetails, "MANGO", "CREATEUSER");
    addFinancialProvider(providerDetails, "MANGO", "SAVEUSER");
    addFinancialProvider(providerDetails, "MANGO", "CREATELEGALUSER");
    addFinancialProvider(providerDetails, "MANGO", "SAVELEGALUSER");
    addFinancialProvider(providerDetails, "MANGO", "GETUSER");
    addFinancialProvider(providerDetails, "MANGO", "CREATEWALLET");
    addFinancialProvider(providerDetails, "MANGO", "CREATEDOCUMENT");
    addFinancialProvider(providerDetails, "MANGO", "CERATEDOCUMENTPAGE");
    addFinancialProvider(providerDetails, "MANGO", "SUBMITDOCUMENT");
    addFinancialProvider(providerDetails, "MANGO", "GETDOCUMENT");
    addFinancialProvider(providerDetails, "MANGO", "CREATEUBODECLARATION");
    addFinancialProvider(providerDetails, "MANGO", "CREATEUBO");
    addFinancialProvider(providerDetails, "MANGO", "SUBMITUBODECLARATION");
    addFinancialProvider(providerDetails, "MANGO", "UPDATEUBO");
    addFinancialProvider(providerDetails, "MANGO", "VIEWUBODECLARATION");
    addFinancialProvider(providerDetails, "MANGO", "VIEWUBO");
    addFinancialProvider(providerDetails, "MANGO", "LISTUBODECLARATIONS");
    addFinancialProvider(providerDetails, "MANGO", "CREATEBANKACCOUNT");
    addFinancialProvider(providerDetails, "MANGO", "DEACTIVATEBANKACCOUNT");
    addFinancialProvider(providerDetails, "MANGO", "PAYIN");
    addFinancialProvider(providerDetails, "MANGO", "PAYOUT");
    addFinancialProvider(providerDetails, "MANGO", "TRANSFER");
    addFinancialProvider(providerDetails, "MANGO", "REFUND");
    addFinancialProvider(providerDetails, "PFS", "CREATEBANKCARD");
    addFinancialProvider(providerDetails, "PFS", "GETCARD");
    addFinancialProvider(providerDetails, "PFS", "UPGRADEBANKCARD");
    addFinancialProvider(providerDetails, "PFS", "CHANGECARDSTATUS");
    addFinancialProvider(providerDetails, "PFS", "ADDCARDCURRENCY");
    addFinancialProvider(providerDetails, "PFS", "GETCARDWALLET");
    addFinancialProvider(providerDetails, "PFS", "GETCARDWALLETTRANSACTIONS");
    addFinancialProvider(providerDetails, "PFS", "GETCARDNUMBER");
    addFinancialProvider(providerDetails, "PFS", "GETCARDEXPDATE");
    addFinancialProvider(providerDetails, "PFS", "GETCARDCVV");
    addFinancialProvider(providerDetails, "PFS", "TRANSFERTOCARD");
    addFinancialProvider(providerDetails, "PFS", "SENDCARDPIN");
    addFinancialProvider(providerDetails, "PFS", "LOCKUNLOCKCARD");
    addFinancialProvider(providerDetails, "PFS", "TRANSFERFROMCARD");
    addFinancialProvider(providerDetails, "PFS", "UPDATEBANKCARD");
    addFinancialProvider(providerDetails, "PFS", "EXECUTEBANKPAYMENT");
    addFinancialProvider(providerDetails, "PFS", "GETFXQUOTE");
    addFinancialProvider(providerDetails, "PFS", "FXTRADE");
    addFinancialProvider(providerDetails, "PFS", "REPLACECARD");
    addFinancialProvider(providerDetails, "MANGO", "CREATECARDREGISTRATION");
    addFinancialProvider(providerDetails, "MANGO", "UPDATECARDREGISTRATION");
    addFinancialProvider(providerDetails, "MANGO", "DIRECTPAYIN");
    addFinancialProvider(providerDetails, "MANGO", "GETDEPOSITCARD");
    addFinancialProvider(providerDetails, "MANGO", "DEACTIVATEDEPOSITCARD");
    addFinancialProvider(providerDetails, "MANGO", "GETUSERSDISPUTES");
    addFinancialProvider(providerDetails, "MANGO", "GETWALLET");

    Account mainAccount = new Account();
    mainAccount.setId("5989e241f5b0af2f948ba09f");
    mainAccount.setAccountId("moneymailme");
    mainAccount.setAccountName("MONEYMAILME LTD");
    mainAccount.setAccountLogo("");
    mainAccount.setAccountRegistrationNumber("09878049");
    mainAccount.setAccountTaxId("");
    mainAccount.setAccountAddressLine1("125 London Wall");
    mainAccount.setAccountAddressLine2("");
    mainAccount.setAccountAddressCity("London");
    mainAccount.setAccountAddressCounty("London");
    mainAccount.setAccountAddressCountry("UK");
    mainAccount.setAccountEmail("contact@moneymail.me");
    mainAccount.setDatabaseName("iss_lolopay_moneymailme");
    mainAccount.setDatabaseUsername("m3dbadmin");
    mainAccount.setDatabasePassword("m3DBPass123");
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
