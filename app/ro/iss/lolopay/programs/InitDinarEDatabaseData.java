package ro.iss.lolopay.programs;

import java.util.ArrayList;
import java.util.List;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.classes.ApplicationStatus;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.models.classes.CountryISO;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.classes.WalletType;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.ApplicationActivity;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

public class InitDinarEDatabaseData {

  private static String mainAccountIdentifier;

  private static Application mainApplication;

  private static String accountLegalUserId = "";

  private static String accountWalletIQDId = "CREDIT_IQD";

  private static String accountWalletIQDProviderId = "";

  private static String accountWalletUSDId = "CREDIT_USD";

  private static String accountWalletUSDProviderId = "";

  private static String accountWalletCardProviderUSDId = "COMPANY_USD";

  private static String accountWalletCardProviderIQDId = "COMPANY_IQD";

  public static void main(String[] args) {

    mainAccountIdentifier = "5c9b72b49c386c33eb2b1987";

    switch (ConfigFactory.load().getString("application.environment")) {
      case "local":
        accountLegalUserId = "00000000";
        accountWalletUSDProviderId = "00000002";
        accountWalletIQDProviderId = "00000003";
        break;

      case "test":
        accountLegalUserId = "11111111";
        accountWalletUSDProviderId = "11111112";
        accountWalletIQDProviderId = "11111113";
        break;
    }

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    // create main account
    createApplication(databaseService);

    // create application audit record
    createApplicationActivity(databaseService);

    // create company user
    createUser(databaseService);

    // create company wallets
    createCompanyWallets(databaseService);

    // create account settings
    createAccountSettings(databaseService);

    // create company wallet for bank cards on PFS
    createCompanyBankCardWallets(databaseService);

    System.out.println("Initial client data created");
  }

  private static void createCompanyBankCardWallets(DatabaseService databaseService) {

    Amount amountUSD = new Amount();
    amountUSD.setCurrency(CurrencyISO.USD);
    amountUSD.setValue(0);

    CompanyBankCardWallet companyBankCardWalletUSD = new CompanyBankCardWallet();
    companyBankCardWalletUSD.setId(accountWalletCardProviderUSDId);
    companyBankCardWalletUSD.setBalance(amountUSD);
    companyBankCardWalletUSD.setCurrency(CurrencyISO.USD);
    databaseService.getConnection(mainAccountIdentifier).save(companyBankCardWalletUSD);

    Amount amountIQD = new Amount();
    amountIQD.setCurrency(CurrencyISO.IQD);
    amountIQD.setValue(0);

    CompanyBankCardWallet companyBankCardWalletIQD = new CompanyBankCardWallet();
    companyBankCardWalletIQD.setId(accountWalletCardProviderIQDId);
    companyBankCardWalletIQD.setBalance(amountIQD);
    companyBankCardWalletIQD.setCurrency(CurrencyISO.IQD);
    databaseService.getConnection(mainAccountIdentifier).save(companyBankCardWalletIQD);
  }

  private static void createAccountSettings(DatabaseService databaseService) {

    List<String> accountWalletsIds = new ArrayList<>();
    accountWalletsIds.add(accountWalletUSDId);
    accountWalletsIds.add(accountWalletIQDId);

    // create dummy records
    AccountSettings demoRecord = new AccountSettings();
    demoRecord.setId("5ca1d3f29c386c33eb2b1989");
    demoRecord.setAccountWalletIds(accountWalletsIds);
    demoRecord.updateAudit(getApplicationStamp());
    databaseService.getConnection(mainAccountIdentifier).save(demoRecord);
  }

  private static void createCompanyWallets(DatabaseService databaseService) {

    Amount walletAmountUSD = new Amount();
    walletAmountUSD.setCurrency(CurrencyISO.USD);
    walletAmountUSD.setValue(10000000);

    Amount zeroAmountUSD = new Amount();
    zeroAmountUSD.setCurrency(CurrencyISO.USD);
    zeroAmountUSD.setValue(0);

    Wallet demoRecordUSD = new Wallet();
    demoRecordUSD.setId(accountWalletUSDId);
    demoRecordUSD.setUserId(accountLegalUserId);
    demoRecordUSD.setDescription("Main USD account wallet");
    demoRecordUSD.setType(WalletType.ACCOUNT);
    demoRecordUSD.setBalance(walletAmountUSD);
    demoRecordUSD.setBlockedBalance(zeroAmountUSD);
    demoRecordUSD.setCurrency(CurrencyISO.USD);
    demoRecordUSD.setProviderId(accountWalletUSDProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordUSD);

    Amount walletAmountIQD = new Amount();
    walletAmountIQD.setCurrency(CurrencyISO.IQD);
    walletAmountIQD.setValue(10000000);

    Amount zeroAmountIQD = new Amount();
    zeroAmountIQD.setCurrency(CurrencyISO.IQD);
    zeroAmountIQD.setValue(0);

    Wallet demoRecordIQD = new Wallet();
    demoRecordIQD.setId(accountWalletIQDId);
    demoRecordIQD.setUserId(accountLegalUserId);
    demoRecordIQD.setDescription("Main IQD account wallet");
    demoRecordIQD.setType(WalletType.ACCOUNT);
    demoRecordIQD.setBalance(walletAmountIQD);
    demoRecordIQD.setBlockedBalance(zeroAmountIQD);
    demoRecordIQD.setCurrency(CurrencyISO.IQD);
    demoRecordIQD.setProviderId(accountWalletIQDProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordIQD);
  }

  private static void createUser(DatabaseService databaseService) {

    // create dummy records
    User companyLegalUser = new User();
    companyLegalUser.setId(accountLegalUserId);
    companyLegalUser.setCompanyType(CompanyType.ORGANIZATION);
    companyLegalUser.setCompanyName("Dinare Ltd");
    companyLegalUser.setCountryOfResidence(CountryISO.IQ);
    companyLegalUser.setNationality(CountryISO.IQ);
    companyLegalUser.setFirstName("Mihai");
    companyLegalUser.setLastName("Ivascu");
    companyLegalUser.setBirthDate(571366861L);
    companyLegalUser.setCompanyEmail("contact@dinare.com");
    companyLegalUser.setEmail("contact@dinare.com");
    companyLegalUser.setCustomTag("Dinare Legal Account");

    // create bank account address
    Address address = new Address();
    address.setAddressLine1("123 Main Street");
    address.setCity("Baghdad");
    address.setCountry(CountryISO.IQ);
    address.setPostalCode("98000");
    companyLegalUser.setAddress(address);

    // create bank account address
    Address companyAddress = new Address();

    companyAddress.setAddressLine1("123 Main Street");
    companyAddress.setCity("Baghdad");
    companyAddress.setCountry(CountryISO.IQ);
    companyAddress.setPostalCode("000000");
    companyLegalUser.setCompanyAddress(companyAddress);

    companyLegalUser.setMobilePhone("");
    companyLegalUser.setType(UserType.LEGAL);
    companyLegalUser.setKycLevel(KYCLevel.VERIFIED);
    companyLegalUser.setProviderId(accountLegalUserId);

    databaseService.getConnection(mainAccountIdentifier).save(companyLegalUser);
  }

  private static void createApplicationActivity(DatabaseService databaseService) {

    UtilsService utilsService = new UtilsImplementation();

    // create new user for the main account
    ApplicationActivity newApplicationActivity = new ApplicationActivity();
    newApplicationActivity.setId(mainApplication.getId());
    newApplicationActivity.setLastActivity(utilsService.getCurrentTimeMiliseconds() / 1000L);
    newApplicationActivity.setLastUsedIp("127.0.0.1");
    newApplicationActivity.setLastLogIn(0L);
    newApplicationActivity.setLastRefresh(0L);
    newApplicationActivity.setLastUserAgent("Create Script");
    databaseService.getConnection(mainAccountIdentifier).save(newApplicationActivity);
  }

  private static void createApplication(DatabaseService databaseService) {

    // create app in the system
    databaseService.getMorphia().mapPackage("ro.iss.lolopay.models.database.Application");

    // create new app for the main account
    mainApplication = new Application();
    mainApplication.setId("5ca1d0769c386c33eb2b1988");
    mainApplication.setApplicationId("dinareservice");
    mainApplication.setApplicationName("DinarE Service Application");
    mainApplication.setApplicationEmail("contact@dinare.com");
    mainApplication.setApplicationPassword(
        "$2a$10$1uNYevoOrbkuXNcstrbU0u2v4yoKFedbPrGnfqphrO9hIUpmispP6");
    mainApplication.setApplicationStatus(ApplicationStatus.ACTIVE);

    switch (ConfigFactory.load().getString("application.environment")) {
      case "local":
        mainApplication.setApplicationHookUrl("http://localhost:9000/testHook");
        break;

      case "test":
        mainApplication.setApplicationHookUrl(
            "https://service.voxfinance.ro/dinarenotifications/hook/processLoloPayHooks");
        break;
    }

    mainApplication.updateAudit(getApplicationStamp());
    databaseService.getConnection(mainAccountIdentifier).save(mainApplication);

    // ensure indexes
    databaseService.getConnection(mainAccountIdentifier).ensureIndexes();
  }

  private static ApplicationStamp getApplicationStamp() {

    ApplicationStamp systemApplication = new ApplicationStamp();
    systemApplication.setApplicationName("CORE SYSTEM");
    systemApplication.setApplicationEmail(ConfigFactory.load().getString("application.email"));

    return systemApplication;
  }
}
