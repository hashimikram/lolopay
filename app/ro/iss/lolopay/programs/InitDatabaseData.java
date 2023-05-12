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

public class InitDatabaseData {
  private static String mainAccountIdentifier;

  private static Application mainApplication;

  private static String accountLegalUserId = "";

  private static String accountWalletEURId = "CREDIT_EUR";

  private static String accountWalletEURProviderId = "";

  private static String accountWalletUSDId = "CREDIT_USD";

  private static String accountWalletUSDProviderId = "";

  private static String accountWalletGBPId = "CREDIT_GBP";

  private static String accountWalletGBPProviderId = "";

  private static String accountWalletZARId = "CREDIT_ZAR";

  private static String accountWalletZARProviderId = "";

  private static String accountWalletPLNId = "CREDIT_PLN";

  private static String accountWalletPLNProviderId = "";

  private static String accountWalletCHFId = "CREDIT_CHF";

  private static String accountWalletCHFProviderId = "";

  private static String accountWalletCardProviderEURId = "COMPANY_EUR";

  private static String accountWalletCardProviderUSDId = "COMPANY_USD";

  private static String accountWalletCardProviderGBPId = "COMPANY_GBP";

  public static void main(String[] args) {

    mainAccountIdentifier = "5989e241f5b0af2f948ba09f";

    switch (ConfigFactory.load().getString("application.environment")) {
      case "local":
        accountLegalUserId = "14053392";
        accountWalletEURProviderId = "14053477";
        accountWalletUSDProviderId = "14053570";
        accountWalletGBPProviderId = "14053546";
        accountWalletZARProviderId = "20075391";
        accountWalletPLNProviderId = "20075142";
        accountWalletCHFProviderId = "20082584";
        break;

      case "test":
        accountLegalUserId = "14053392";
        accountWalletEURProviderId = "14053477";
        accountWalletUSDProviderId = "14053570";
        accountWalletGBPProviderId = "14053546";
        accountWalletZARProviderId = "20075391";
        accountWalletPLNProviderId = "20075142";
        accountWalletCHFProviderId = "20082584";
        break;

      case "live":
        accountLegalUserId = "54601866";
        accountWalletEURProviderId = "54602230";
        accountWalletUSDProviderId = "54602239";
        accountWalletGBPProviderId = "54602190";
        accountWalletZARProviderId = "127675696";
        accountWalletPLNProviderId = "127675294";
        accountWalletCHFProviderId = "127674958";
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

    // create zero amount currencies
    Amount amountEUR = new Amount();
    amountEUR.setCurrency(CurrencyISO.EUR);
    amountEUR.setValue(0);

    Amount amountUSD = new Amount();
    amountUSD.setCurrency(CurrencyISO.USD);
    amountUSD.setValue(0);

    Amount amountGBP = new Amount();
    amountGBP.setCurrency(CurrencyISO.GBP);
    amountGBP.setValue(0);

    // create base records
    CompanyBankCardWallet companyBankCardWalletEUR = new CompanyBankCardWallet();
    companyBankCardWalletEUR.setId(accountWalletCardProviderEURId);
    companyBankCardWalletEUR.setBalance(amountEUR);
    companyBankCardWalletEUR.setCurrency(CurrencyISO.EUR);
    databaseService.getConnection(mainAccountIdentifier).save(companyBankCardWalletEUR);

    CompanyBankCardWallet companyBankCardWalletUSD = new CompanyBankCardWallet();
    companyBankCardWalletUSD.setId(accountWalletCardProviderUSDId);
    companyBankCardWalletUSD.setBalance(amountUSD);
    companyBankCardWalletUSD.setCurrency(CurrencyISO.USD);
    databaseService.getConnection(mainAccountIdentifier).save(companyBankCardWalletUSD);

    CompanyBankCardWallet companyBankCardWalletGBP = new CompanyBankCardWallet();
    companyBankCardWalletGBP.setId(accountWalletCardProviderGBPId);
    companyBankCardWalletGBP.setBalance(amountGBP);
    companyBankCardWalletGBP.setCurrency(CurrencyISO.GBP);
    databaseService.getConnection(mainAccountIdentifier).save(companyBankCardWalletGBP);
  }

  private static void createAccountSettings(DatabaseService databaseService) {

    List<String> accountWalletsIds = new ArrayList<>();
    accountWalletsIds.add(accountWalletEURId);
    accountWalletsIds.add(accountWalletUSDId);
    accountWalletsIds.add(accountWalletGBPId);
    accountWalletsIds.add(accountWalletZARId);
    accountWalletsIds.add(accountWalletPLNId);
    accountWalletsIds.add(accountWalletCHFId);

    // create dummy records
    AccountSettings demoRecord = new AccountSettings();
    demoRecord.setId("59ad742cf5b0af763a364c06");
    demoRecord.setAccountWalletIds(accountWalletsIds);
    demoRecord.updateAudit(getApplicationStamp());
    databaseService.getConnection(mainAccountIdentifier).save(demoRecord);
  }

  private static void createCompanyWallets(DatabaseService databaseService) {

    // create demo records
    Amount walletAmountEUR = new Amount();
    walletAmountEUR.setCurrency(CurrencyISO.EUR);
    walletAmountEUR.setValue(10000000);

    Amount zeroAmountEUR = new Amount();
    zeroAmountEUR.setCurrency(CurrencyISO.EUR);
    zeroAmountEUR.setValue(0);

    Wallet demoRecordEUR = new Wallet();
    demoRecordEUR.setId(accountWalletEURId);
    demoRecordEUR.setUserId(accountLegalUserId);
    demoRecordEUR.setDescription("Main EUR account wallet");
    demoRecordEUR.setType(WalletType.ACCOUNT);
    demoRecordEUR.setBalance(walletAmountEUR);
    demoRecordEUR.setBlockedBalance(zeroAmountEUR);
    demoRecordEUR.setCurrency(CurrencyISO.EUR);
    demoRecordEUR.setProviderId(accountWalletEURProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordEUR);

    Amount walletAmountGBP = new Amount();
    walletAmountGBP.setCurrency(CurrencyISO.GBP);
    walletAmountGBP.setValue(10000000);

    Amount zeroAmountGBP = new Amount();
    zeroAmountGBP.setCurrency(CurrencyISO.GBP);
    zeroAmountGBP.setValue(0);

    Wallet demoRecordGBP = new Wallet();
    demoRecordGBP.setId(accountWalletGBPId);
    demoRecordGBP.setUserId(accountLegalUserId);
    demoRecordGBP.setDescription("Main GBP account wallet");
    demoRecordGBP.setType(WalletType.ACCOUNT);
    demoRecordGBP.setBalance(walletAmountGBP);
    demoRecordGBP.setBlockedBalance(zeroAmountGBP);
    demoRecordGBP.setCurrency(CurrencyISO.GBP);
    demoRecordGBP.setProviderId(accountWalletGBPProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordGBP);

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

    Amount walletAmountZAR = new Amount();
    walletAmountZAR.setCurrency(CurrencyISO.ZAR);
    walletAmountZAR.setValue(0);

    Amount zeroAmountZAR = new Amount();
    zeroAmountZAR.setCurrency(CurrencyISO.ZAR);
    zeroAmountZAR.setValue(0);

    Wallet demoRecordZAR = new Wallet();
    demoRecordZAR.setId(accountWalletZARId);
    demoRecordZAR.setUserId(accountLegalUserId);
    demoRecordZAR.setDescription("Main ZAR account wallet");
    demoRecordZAR.setType(WalletType.ACCOUNT);
    demoRecordZAR.setBalance(walletAmountZAR);
    demoRecordZAR.setBlockedBalance(zeroAmountZAR);
    demoRecordZAR.setCurrency(CurrencyISO.ZAR);
    demoRecordZAR.setProviderId(accountWalletZARProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordZAR);

    Amount walletAmountCHF = new Amount();
    walletAmountCHF.setCurrency(CurrencyISO.CHF);
    walletAmountCHF.setValue(0);

    Amount zeroAmountCHF = new Amount();
    zeroAmountCHF.setCurrency(CurrencyISO.CHF);
    zeroAmountCHF.setValue(0);

    Wallet demoRecordCHF = new Wallet();
    demoRecordCHF.setId(accountWalletCHFId);
    demoRecordCHF.setUserId(accountLegalUserId);
    demoRecordCHF.setDescription("Main CHF account wallet");
    demoRecordCHF.setType(WalletType.ACCOUNT);
    demoRecordCHF.setBalance(walletAmountCHF);
    demoRecordCHF.setBlockedBalance(zeroAmountCHF);
    demoRecordCHF.setCurrency(CurrencyISO.CHF);
    demoRecordCHF.setProviderId(accountWalletCHFProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordCHF);

    Amount walletAmountPLN = new Amount();
    walletAmountPLN.setCurrency(CurrencyISO.PLN);
    walletAmountPLN.setValue(0);

    Amount zeroAmountPLN = new Amount();
    zeroAmountPLN.setCurrency(CurrencyISO.PLN);
    zeroAmountPLN.setValue(0);

    Wallet demoRecordPLN = new Wallet();
    demoRecordPLN.setId(accountWalletPLNId);
    demoRecordPLN.setUserId(accountLegalUserId);
    demoRecordPLN.setDescription("Main PLN account wallet");
    demoRecordPLN.setType(WalletType.ACCOUNT);
    demoRecordPLN.setBalance(walletAmountPLN);
    demoRecordPLN.setBlockedBalance(zeroAmountPLN);
    demoRecordPLN.setCurrency(CurrencyISO.PLN);
    demoRecordPLN.setProviderId(accountWalletPLNProviderId);
    databaseService.getConnection(mainAccountIdentifier).save(demoRecordPLN);
  }

  private static void createUser(DatabaseService databaseService) {

    // create dummy records
    User companyLegalUser = new User();
    companyLegalUser.setId(accountLegalUserId);
    companyLegalUser.setCompanyType(CompanyType.ORGANIZATION);
    companyLegalUser.setCompanyName("Moneymailme Ltd");
    companyLegalUser.setCountryOfResidence(CountryISO.MC);
    companyLegalUser.setNationality(CountryISO.RO);
    companyLegalUser.setFirstName("Mihai");
    companyLegalUser.setLastName("Ivascu");
    companyLegalUser.setBirthDate(571366861L);
    companyLegalUser.setCompanyEmail("support@moneymail.me");
    companyLegalUser.setEmail("support@moneymail.me");
    companyLegalUser.setCustomTag("Moneymailme Legal Account");

    // create bank account address
    Address address = new Address();
    address.setAddressLine1("Quai Jean Charles Rey, Nb. 2");
    address.setCity("Monte Carlo");
    address.setCountry(CountryISO.MC);
    address.setPostalCode("98000");
    companyLegalUser.setAddress(address);

    // create bank account address
    Address companyAddress = new Address();

    companyAddress.setAddressLine1("125 London Wall");
    companyAddress.setCity("London");
    companyAddress.setCountry(CountryISO.GB);
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
    mainApplication.setId("5989e246f5b0af2f948ba1d2");
    mainApplication.setApplicationId("m3Service");
    mainApplication.setApplicationName("M3 Service Application");
    mainApplication.setApplicationEmail("contact@moneymail.me");
    mainApplication.setApplicationPassword(
        "$2a$10$1uNYevoOrbkuXNcstrbU0u2v4yoKFedbPrGnfqphrO9hIUpmispP6");
    mainApplication.setApplicationStatus(ApplicationStatus.ACTIVE);

    switch (ConfigFactory.load().getString("application.environment")) {
      case "local":
        mainApplication.setApplicationHookUrl("http://localhost:9000/testHook");
        break;

      case "test":
        mainApplication.setApplicationHookUrl(
            "https://service.voxfinance.ro/notifications/hook/processLoloPayHooks");
        break;

      case "live":
        mainApplication.setApplicationHookUrl(
            "https://service.moneymailme.com/notifications/hook/processLoloPayHooks");
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
