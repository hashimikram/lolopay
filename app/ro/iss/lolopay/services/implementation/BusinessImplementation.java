package ro.iss.lolopay.services.implementation;

import com.typesafe.config.ConfigFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import ro.iss.lolopay.classes.CardRegistration;
import ro.iss.lolopay.classes.ExecuteCardWalletsTrade;
import ro.iss.lolopay.classes.FxQuote;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.BankAccountType;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.BankCardType;
import ro.iss.lolopay.models.classes.Birthplace;
import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CardUserInfo;
import ro.iss.lolopay.models.classes.CardUserInfoEmploymentStatus;
import ro.iss.lolopay.models.classes.CardUserInfoEstate;
import ro.iss.lolopay.models.classes.CardUserInfoMonthlyIncome;
import ro.iss.lolopay.models.classes.CardUserInfoOccupation;
import ro.iss.lolopay.models.classes.CardUserInfoPurpose;
import ro.iss.lolopay.models.classes.CardValidity;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.models.classes.CountryISO;
import ro.iss.lolopay.models.classes.CultureCode;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.DepositAccountType;
import ro.iss.lolopay.models.classes.DocumentRejectReason;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.DocumentType;
import ro.iss.lolopay.models.classes.ExecutionType;
import ro.iss.lolopay.models.classes.FeeModel;
import ro.iss.lolopay.models.classes.IncomeRange;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.PaymentType;
import ro.iss.lolopay.models.classes.ProviderOperation;
import ro.iss.lolopay.models.classes.RefundReasonType;
import ro.iss.lolopay.models.classes.SecureMode;
import ro.iss.lolopay.models.classes.SecurityInfo;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.classes.TransactionNature;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.classes.UboDeclarationRefusedReasonType;
import ro.iss.lolopay.models.classes.UboDeclarationStatus;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.BankAccountCA;
import ro.iss.lolopay.models.database.BankAccountGB;
import ro.iss.lolopay.models.database.BankAccountIBAN;
import ro.iss.lolopay.models.database.BankAccountOTHER;
import ro.iss.lolopay.models.database.BankAccountUS;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transaction;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.AccountSettingsService;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.models.services.definition.BankAccountService;
import ro.iss.lolopay.models.services.definition.BankCardService;
import ro.iss.lolopay.models.services.definition.BankCardTransactionsService;
import ro.iss.lolopay.models.services.definition.BankCardWalletService;
import ro.iss.lolopay.models.services.definition.DepositCardService;
import ro.iss.lolopay.models.services.definition.DocumentService;
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.models.services.definition.PayOutService;
import ro.iss.lolopay.models.services.definition.RefundService;
import ro.iss.lolopay.models.services.definition.TransferService;
import ro.iss.lolopay.models.services.definition.UboService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.models.services.definition.WalletService;
import ro.iss.lolopay.requests.RequestAddBankCardCurrency;
import ro.iss.lolopay.requests.RequestAmount;
import ro.iss.lolopay.requests.RequestBankCardTransfer;
import ro.iss.lolopay.requests.RequestBankPayment;
import ro.iss.lolopay.requests.RequestCardUserInfo;
import ro.iss.lolopay.requests.RequestCreateAVSDirectPayIn;
import ro.iss.lolopay.requests.RequestCreateBankAccount_CA;
import ro.iss.lolopay.requests.RequestCreateBankAccount_GB;
import ro.iss.lolopay.requests.RequestCreateBankAccount_IBAN;
import ro.iss.lolopay.requests.RequestCreateBankAccount_OTHER;
import ro.iss.lolopay.requests.RequestCreateBankAccount_US;
import ro.iss.lolopay.requests.RequestCreateBankCard;
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestCreateDirectPayIn;
import ro.iss.lolopay.requests.RequestCreateDocument;
import ro.iss.lolopay.requests.RequestCreateLegalUser;
import ro.iss.lolopay.requests.RequestCreateNaturalUser;
import ro.iss.lolopay.requests.RequestCreateWallet;
import ro.iss.lolopay.requests.RequestExecuteCardWalletsTrade;
import ro.iss.lolopay.requests.RequestFxQuote;
import ro.iss.lolopay.requests.RequestRefundPayIn;
import ro.iss.lolopay.requests.RequestSaveLegalUser;
import ro.iss.lolopay.requests.RequestSaveNaturalUser;
import ro.iss.lolopay.requests.RequestUbo;
import ro.iss.lolopay.requests.RequestUpdateBankCard;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;
import ro.iss.lolopay.requests.RequestUpgradeBankCard;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.NotificationService;
import ro.iss.lolopay.services.definition.ProviderService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class BusinessImplementation implements BusinessService {
  @Inject ProviderFactory providerFactory;

  @Inject UtilsService utilsService;

  @Inject UserService userService;

  @Inject TransferService transferService;

  @Inject RefundService refundService;

  @Inject PayOutService payOutService;

  @Inject PayInService payInService;

  @Inject AccountSettingsService accountSettingsService;

  @Inject WalletService walletService;

  @Inject NotificationService notificationService;

  @Inject BankAccountService bankAccountService;

  @Inject DocumentService documentService;

  @Inject UboService uboService;

  @Inject ApplicationErrorService applicationErrorService;

  @Inject BankCardService bankCardService;

  @Inject BankCardWalletService bankCardWalletService;

  @Inject LogService logService;

  @Inject BankCardTransactionsService bankCardTransactionsService;

  @Inject DepositCardService depositCardService;

  @Override
  public User anonymizeUser(String requestId, Account account, Application application, User user) {

    // delete user documents
    documentService.deleteUserDocuments(requestId, account, user.getId());

    // delete wallet
    walletService.deleteWallet(requestId, account, user.getId());

    // delete user
    userService.deleteUser(requestId, account, user.getId());
    return user;
  }

  @Override
  public User createNaturalUser(
      String requestId,
      Account account,
      Application application,
      RequestCreateNaturalUser requestCreateNaturalUser)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(
        requestId, "IN", "requestCreateNaturalUser.address", requestCreateNaturalUser.getAddress());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.birthDate",
        requestCreateNaturalUser.getBirthDate());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.countryOfResidence",
        requestCreateNaturalUser.getCountryOfResidence());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.customTag",
        requestCreateNaturalUser.getCustomTag());
    logService.debug(
        requestId, "IN", "requestCreateNaturalUser.email", requestCreateNaturalUser.getEmail());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.firstName",
        requestCreateNaturalUser.getFirstName());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.incomeRange",
        requestCreateNaturalUser.getIncomeRange());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.lastName",
        requestCreateNaturalUser.getLastName());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.mobilePhone",
        requestCreateNaturalUser.getMobilePhone());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.nationality",
        requestCreateNaturalUser.getNationality());
    logService.debug(
        requestId,
        "IN",
        "requestCreateNaturalUser.occupation",
        requestCreateNaturalUser.getOccupation());

    // create natural user object
    User newUser = new User();
    newUser.setEmail(requestCreateNaturalUser.getEmail());
    newUser.setFirstName(requestCreateNaturalUser.getFirstName());
    newUser.setLastName(requestCreateNaturalUser.getLastName());
    newUser.setBirthDate(requestCreateNaturalUser.getBirthDate());
    newUser.setNationality(CountryISO.valueOf(requestCreateNaturalUser.getNationality()));
    newUser.setCountryOfResidence(
        CountryISO.valueOf(requestCreateNaturalUser.getCountryOfResidence()));
    newUser.setCustomTag(requestCreateNaturalUser.getCustomTag());
    newUser.setMobilePhone(requestCreateNaturalUser.getMobilePhone());
    newUser.setKycLevel(KYCLevel.STANDARD);
    newUser.setType(UserType.NATURAL);
    newUser.setTempDiacriticsSolved(true);

    if (requestCreateNaturalUser.getAddress() != null) {
      // create address
      Address address = new Address();

      address.setAddressLine1(requestCreateNaturalUser.getAddress().getAddressLine1());
      address.setAddressLine2(requestCreateNaturalUser.getAddress().getAddressLine2());
      address.setCity(requestCreateNaturalUser.getAddress().getCity());
      address.setCounty(requestCreateNaturalUser.getAddress().getCounty());
      address.setCountry(CountryISO.valueOf(requestCreateNaturalUser.getAddress().getCountry()));
      address.setPostalCode(requestCreateNaturalUser.getAddress().getPostalCode());

      newUser.setAddress(address);
    }

    newUser.setId(ObjectId.get().toString());
    newUser.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);
    newUser.updateAudit(application.getApplicationStamp());

    try {
      // user requires to be created in sync mode since there are very quick operations executed
      // after that, like create wallet
      createProviderNaturalUser(requestId, account, application, newUser);
      logService.debug(requestId, "L", "provider", "created");

      // save natural user only if there are no surprises
      userService.saveUser(requestId, account, newUser);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    logService.debug(requestId, "OUT", "newUser", newUser);
    return newUser;
  }

  @Override
  public void saveNaturalUser(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestSaveNaturalUser requestSaveNaturalUser)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "requestSaveNaturalUser.id", requestSaveNaturalUser.getId());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.email", requestSaveNaturalUser.getEmail());
    logService.debug(
        requestId,
        "IN",
        "requestSaveNaturalUser.mobilePhone",
        requestSaveNaturalUser.getMobilePhone());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.firstName", requestSaveNaturalUser.getFirstName());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.lastName", requestSaveNaturalUser.getLastName());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.birthDate", requestSaveNaturalUser.getBirthDate());
    logService.debug(
        requestId,
        "IN",
        "requestSaveNaturalUser.nationality",
        requestSaveNaturalUser.getNationality());
    logService.debug(
        requestId,
        "IN",
        "requestSaveNaturalUser.countryOfResidence",
        requestSaveNaturalUser.getCountryOfResidence());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.customTag", requestSaveNaturalUser.getCustomTag());
    logService.debug(
        requestId,
        "IN",
        "requestSaveNaturalUser.occupation",
        requestSaveNaturalUser.getOccupation());
    logService.debug(
        requestId,
        "IN",
        "requestSaveNaturalUser.incomeRange",
        requestSaveNaturalUser.getIncomeRange());
    logService.debug(
        requestId, "IN", "requestSaveNaturalUser.address", requestSaveNaturalUser.getAddress());

    // set user email only if email data exists
    if ((requestSaveNaturalUser.getEmail() != null)
        && !requestSaveNaturalUser.getEmail().equals("")) {
      user.setEmail(requestSaveNaturalUser.getEmail());
    }

    // set user firstName only if firstName exists
    if ((requestSaveNaturalUser.getFirstName() != null)
        && !requestSaveNaturalUser.getFirstName().equals("")) {
      user.setFirstName(requestSaveNaturalUser.getFirstName());
    }

    // set user lastName only if lastName exists
    if ((requestSaveNaturalUser.getLastName() != null)
        && !requestSaveNaturalUser.getLastName().equals("")) {
      user.setLastName(requestSaveNaturalUser.getLastName());
    }

    // set user birthDate only if birthDate exists
    if (requestSaveNaturalUser.getBirthDate() != null) {
      user.setBirthDate(requestSaveNaturalUser.getBirthDate());
    }

    // set user nationality only if nationality exists
    if (requestSaveNaturalUser.getNationality() != null) {
      user.setNationality(CountryISO.valueOf(requestSaveNaturalUser.getNationality()));
    }

    // set user countryOfResidence only if countryOfResidence exists
    if (requestSaveNaturalUser.getCountryOfResidence() != null) {
      user.setCountryOfResidence(
          CountryISO.valueOf(requestSaveNaturalUser.getCountryOfResidence()));
    }

    // set user customTag only if customTag exists
    if ((requestSaveNaturalUser.getCustomTag() != null)
        && !requestSaveNaturalUser.getCustomTag().equals("")) {
      user.setCustomTag(requestSaveNaturalUser.getCustomTag());
    }

    // set user occupation only if occupation exists
    if ((requestSaveNaturalUser.getOccupation() != null)
        && !requestSaveNaturalUser.getOccupation().equals("")) {
      user.setOccupation(requestSaveNaturalUser.getOccupation());
    }

    // set user incomeRange only if incomeRange exists
    if (requestSaveNaturalUser.getIncomeRange() != null) {
      user.setIncomeRange(IncomeRange.valueOf(requestSaveNaturalUser.getIncomeRange()));
    }

    // set user address only if address exists
    if (requestSaveNaturalUser.getAddress() != null) {
      // create address
      Address address = new Address();

      address.setAddressLine1(requestSaveNaturalUser.getAddress().getAddressLine1());
      address.setAddressLine2(requestSaveNaturalUser.getAddress().getAddressLine2());
      address.setCity(requestSaveNaturalUser.getAddress().getCity());
      address.setCounty(requestSaveNaturalUser.getAddress().getCounty());
      address.setCountry(CountryISO.valueOf(requestSaveNaturalUser.getAddress().getCountry()));
      address.setPostalCode(requestSaveNaturalUser.getAddress().getPostalCode());

      user.setAddress(address);
    }

    // set user mobilePhone only if mobilePhone exists
    if ((requestSaveNaturalUser.getMobilePhone() != null)
        && !requestSaveNaturalUser.getMobilePhone().equals("")) {
      user.setMobilePhone(requestSaveNaturalUser.getMobilePhone());
    }

    try {
      // attempt user save
      saveProviderNaturalUser(requestId, account, application, user);
      logService.debug(requestId, "L", "provider", "saved");

      // save user
      userService.saveUser(requestId, account, user);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public User createLegalUser(
      String requestId,
      Account account,
      Application application,
      RequestCreateLegalUser requestCreateLegalUser)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.address", requestCreateLegalUser.getAddress());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.birthDate", requestCreateLegalUser.getBirthDate());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.companyAddress",
        requestCreateLegalUser.getCompanyAddress());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.companyEmail",
        requestCreateLegalUser.getCompanyEmail());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.companyName",
        requestCreateLegalUser.getCompanyName());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.companyType",
        requestCreateLegalUser.getCompanyType());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.countryOfResidence",
        requestCreateLegalUser.getCountryOfResidence());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.customTag", requestCreateLegalUser.getCustomTag());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.email", requestCreateLegalUser.getEmail());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.firstName", requestCreateLegalUser.getFirstName());
    logService.debug(
        requestId, "IN", "requestCreateLegalUser.lastName", requestCreateLegalUser.getLastName());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.mobilePhone",
        requestCreateLegalUser.getMobilePhone());
    logService.debug(
        requestId,
        "IN",
        "requestCreateLegalUser.nationality",
        requestCreateLegalUser.getNationality());

    // create legal user object
    User newLegalUser = new User();
    newLegalUser.setCompanyType(CompanyType.valueOf(requestCreateLegalUser.getCompanyType()));
    newLegalUser.setCompanyName(requestCreateLegalUser.getCompanyName());
    newLegalUser.setCountryOfResidence(
        CountryISO.valueOf(requestCreateLegalUser.getCountryOfResidence()));
    newLegalUser.setNationality(CountryISO.valueOf(requestCreateLegalUser.getNationality()));
    newLegalUser.setFirstName(requestCreateLegalUser.getFirstName());
    newLegalUser.setLastName(requestCreateLegalUser.getLastName());
    newLegalUser.setBirthDate(requestCreateLegalUser.getBirthDate());
    newLegalUser.setCompanyEmail(requestCreateLegalUser.getCompanyEmail());
    newLegalUser.setEmail(requestCreateLegalUser.getEmail());
    newLegalUser.setCustomTag(requestCreateLegalUser.getCustomTag());
    newLegalUser.setCompanyRegistrationNumber(
        requestCreateLegalUser.getCompanyRegistrationNumber());
    newLegalUser.setTempDiacriticsSolved(true);

    if (requestCreateLegalUser.getAddress() != null) {
      // create bank account address
      Address address = new Address();

      address.setAddressLine1(requestCreateLegalUser.getAddress().getAddressLine1());
      address.setAddressLine2(requestCreateLegalUser.getAddress().getAddressLine2());
      address.setCity(requestCreateLegalUser.getAddress().getCity());
      address.setCounty(requestCreateLegalUser.getAddress().getCounty());
      address.setCountry(CountryISO.valueOf(requestCreateLegalUser.getAddress().getCountry()));
      address.setPostalCode(requestCreateLegalUser.getAddress().getPostalCode());

      newLegalUser.setAddress(address);
    }

    if (requestCreateLegalUser.getCompanyAddress() != null) {
      // create bank account address
      Address companyAddress = new Address();

      companyAddress.setAddressLine1(requestCreateLegalUser.getCompanyAddress().getAddressLine1());
      companyAddress.setAddressLine2(requestCreateLegalUser.getCompanyAddress().getAddressLine2());
      companyAddress.setCity(requestCreateLegalUser.getCompanyAddress().getCity());
      companyAddress.setCounty(requestCreateLegalUser.getCompanyAddress().getCounty());
      companyAddress.setCountry(
          CountryISO.valueOf(requestCreateLegalUser.getCompanyAddress().getCountry()));
      companyAddress.setPostalCode(requestCreateLegalUser.getCompanyAddress().getPostalCode());

      newLegalUser.setCompanyAddress(companyAddress);
    }
    newLegalUser.setMobilePhone(requestCreateLegalUser.getMobilePhone());
    newLegalUser.setType(UserType.LEGAL);
    newLegalUser.setKycLevel(KYCLevel.STANDARD);
    newLegalUser.updateAudit(application.getApplicationStamp());
    newLegalUser.setId(ObjectId.get().toString());
    newLegalUser.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);

    try {
      // register user to provider
      createProviderLegalUser(requestId, account, application, newLegalUser);
      logService.debug(requestId, "L", "provider", "created");

      // save legal user
      userService.saveUser(requestId, account, newLegalUser);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    logService.debug(requestId, "OUT", "newLegalUser", newLegalUser);
    return newLegalUser;
  }

  @Override
  public void saveLegalUser(
      String requestId,
      Account account,
      Application application,
      User legalUser,
      RequestSaveLegalUser requestSaveLegalUser)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "requestSaveLegalUser.id", requestSaveLegalUser.getId());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.companyType", requestSaveLegalUser.getCompanyType());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.companyName", requestSaveLegalUser.getCompanyName());
    logService.debug(
        requestId,
        "IN",
        "requestSaveLegalUser.countryOfResidence",
        requestSaveLegalUser.getCountryOfResidence());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.nationality", requestSaveLegalUser.getNationality());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.firstName", requestSaveLegalUser.getFirstName());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.lastName", requestSaveLegalUser.getLastName());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.birthDate", requestSaveLegalUser.getBirthDate());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.mobilePhone", requestSaveLegalUser.getMobilePhone());
    logService.debug(
        requestId,
        "IN",
        "requestSaveLegalUser.companyEmail",
        requestSaveLegalUser.getCompanyEmail());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.email", requestSaveLegalUser.getEmail());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.customTag", requestSaveLegalUser.getCustomTag());
    logService.debug(
        requestId, "IN", "requestSaveLegalUser.address", requestSaveLegalUser.getAddress());
    logService.debug(
        requestId,
        "IN",
        "requestSaveLegalUser.companyAddress",
        requestSaveLegalUser.getCompanyAddress());

    // test if companyType has value before we update the records
    if (requestSaveLegalUser.getCompanyType() != null) {
      legalUser.setCompanyType(CompanyType.valueOf(requestSaveLegalUser.getCompanyType()));
    }

    // test if company name has value before we update the records
    if ((requestSaveLegalUser.getCompanyName() != null)
        && !requestSaveLegalUser.getCompanyName().equals("")) {
      legalUser.setCompanyName(requestSaveLegalUser.getCompanyName());
    }

    // test if country has value before we update the records
    if (requestSaveLegalUser.getCountryOfResidence() != null) {
      legalUser.setCountryOfResidence(
          CountryISO.valueOf(requestSaveLegalUser.getCountryOfResidence()));
    }

    // test if nationality has value before we update the records
    if (requestSaveLegalUser.getNationality() != null) {
      legalUser.setNationality(CountryISO.valueOf(requestSaveLegalUser.getNationality()));
    }

    // test if first name has value before we update the records
    if ((requestSaveLegalUser.getFirstName() != null)
        && !requestSaveLegalUser.getFirstName().equals("")) {
      legalUser.setFirstName(requestSaveLegalUser.getFirstName());
    }

    // test if last name has value before we update the records
    if ((requestSaveLegalUser.getLastName() != null)
        && !requestSaveLegalUser.getLastName().equals("")) {
      legalUser.setLastName(requestSaveLegalUser.getLastName());
    }

    // test if birth date has value before we update the records
    if ((requestSaveLegalUser.getBirthDate() != null)
        && (requestSaveLegalUser.getBirthDate() > 0)) {
      legalUser.setBirthDate(requestSaveLegalUser.getBirthDate());
    }

    // test if mobile phone has value before we update the records
    if ((requestSaveLegalUser.getMobilePhone() != null)
        && !requestSaveLegalUser.getMobilePhone().equals("")) {
      legalUser.setMobilePhone(requestSaveLegalUser.getMobilePhone());
    }

    // test if companyEmail has value before we update the records
    if ((requestSaveLegalUser.getCompanyEmail() != null)
        && !requestSaveLegalUser.getCompanyEmail().equals("")) {
      legalUser.setCompanyEmail(requestSaveLegalUser.getCompanyEmail());
    }

    // test if email has value before we update the records
    if ((requestSaveLegalUser.getEmail() != null) && !requestSaveLegalUser.getEmail().equals("")) {
      legalUser.setEmail(requestSaveLegalUser.getEmail());
    }

    // test if custom tag has value before we update the records
    if ((requestSaveLegalUser.getCustomTag() != null)
        && !requestSaveLegalUser.getCustomTag().equals("")) {
      legalUser.setCustomTag(requestSaveLegalUser.getCustomTag());
    }

    if (requestSaveLegalUser.getCompanyRegistrationNumber() != null
        && !requestSaveLegalUser.getCompanyRegistrationNumber().equals("")) {
      legalUser.setCompanyRegistrationNumber(requestSaveLegalUser.getCompanyRegistrationNumber());
    }

    // test if address has value before we update the records
    if (requestSaveLegalUser.getAddress() != null) {
      // create address
      Address address = new Address();

      address.setAddressLine1(requestSaveLegalUser.getAddress().getAddressLine1());
      address.setAddressLine2(requestSaveLegalUser.getAddress().getAddressLine2());
      address.setCity(requestSaveLegalUser.getAddress().getCity());
      address.setCounty(requestSaveLegalUser.getAddress().getCounty());
      address.setCountry(CountryISO.valueOf(requestSaveLegalUser.getAddress().getCountry()));
      address.setPostalCode(requestSaveLegalUser.getAddress().getPostalCode());

      legalUser.setAddress(address);
    }

    // test if companyAddress has value before we update the records
    if (requestSaveLegalUser.getCompanyAddress() != null) {
      // create address
      Address companyAddress = new Address();

      companyAddress.setAddressLine1(requestSaveLegalUser.getCompanyAddress().getAddressLine1());
      companyAddress.setAddressLine2(requestSaveLegalUser.getCompanyAddress().getAddressLine2());
      companyAddress.setCity(requestSaveLegalUser.getCompanyAddress().getCity());
      companyAddress.setCounty(requestSaveLegalUser.getCompanyAddress().getCounty());
      companyAddress.setCountry(
          CountryISO.valueOf(requestSaveLegalUser.getCompanyAddress().getCountry()));
      companyAddress.setPostalCode(requestSaveLegalUser.getCompanyAddress().getPostalCode());

      legalUser.setCompanyAddress(companyAddress);
    }

    try {
      // attempt user save
      saveProviderLegalUser(requestId, account, application, legalUser);
      logService.debug(requestId, "L", "provider", "saved");

      // save legal user
      userService.saveUser(requestId, account, legalUser);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }
  }

  @Override
  public void updateUserBasicDataFromProvider(
      String requestId, Account account, Application application, User user) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETUSER);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // check KYC level of user at provider, see if it changed
    ProviderResponse providerResponse = providerService.getProviderUser(requestId, user);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // update status
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // update natural user first name
      if (providerResponse.getProviderData("firstName") != null) {
        user.setFirstName(providerResponse.getProviderData("firstName").toString());
      }

      // update natural user last name
      if (providerResponse.getProviderData("lastName") != null) {
        user.setLastName(providerResponse.getProviderData("lastName").toString());
      }

      // update natural user occupation
      if (providerResponse.getProviderData("occupation") != null) {
        user.setOccupation(providerResponse.getProviderData("occupation").toString());
      }

      // update natural user address line 1
      if (providerResponse.getProviderData("addressLine1") != null) {
        if (user.getAddress() != null) {
          user.getAddress()
              .setAddressLine1(providerResponse.getProviderData("addressLine1").toString());
        }
      }

      // update natural user address line 2
      if (providerResponse.getProviderData("addressLine2") != null) {
        if (user.getAddress() != null) {
          user.getAddress()
              .setAddressLine2(providerResponse.getProviderData("addressLine2").toString());
        }
      }

      // update natural user address city
      if (providerResponse.getProviderData("city") != null) {
        if (user.getAddress() != null) {
          user.getAddress().setCity(providerResponse.getProviderData("city").toString());
        }
      }

      // update natural user address county
      if (providerResponse.getProviderData("county") != null) {
        if (user.getAddress() != null) {
          user.getAddress().setCounty(providerResponse.getProviderData("county").toString());
        }
      }

      // update KYCLevel
      if (user.getType() == UserType.LEGAL) {
        if (providerResponse.getProviderData("kycLevel") != null) {
          user.setKycLevel(
              KYCLevel.valueOf(providerResponse.getProviderData("kycLevel").toString()));
        }
      }

      // set action of update as performed
      user.setTempDiacriticsSolved(true);

      // update audit
      user.updateAudit(application.getApplicationStamp());

      // save user
      userService.saveUser(requestId, account, user);
      logService.debug(requestId, "L", "dbservice", "saved");
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void updateUserKyc(String requestId, Account account, Application application, User user)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETUSER);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // check KYC level of user at provider, see if it changed
    ProviderResponse providerResponse = providerService.getProviderUser(requestId, user);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // update status
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // update if KYC changed
      user.setKycLevel((KYCLevel) providerResponse.getProviderData("kycLevel"));
      logService.debug(requestId, "L", "kyc", providerResponse.getProviderData("kycLevel"));

      // update audit
      user.updateAudit(application.getApplicationStamp());

      // save user
      userService.saveUser(requestId, account, user);
      logService.debug(requestId, "L", "dbservice", "saved");
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void updateDocumentStatus(
      String requestId, Account account, Application application, User user, Document document)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(requestId, "IN", "document.id", document.getId());
    logService.debug(requestId, "IN", "document.status", document.getStatus());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETDOCUMENT);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // check KYC document status at provider, see if it changed
    ProviderResponse providerResponse =
        providerService.getProviderDocument(requestId, user, document);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // if status was changed to REGULARs
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // update document fieldsOF
      document.setStatus((DocumentStatus) providerResponse.getProviderData("status"));
      logService.debug(requestId, "L", "status", providerResponse.getProviderData("status"));

      if (providerResponse.getProviderData("rejectionReasonMessage") != null) {
        document.setRejectionReasonMessage(
            providerResponse.getProviderData("rejectionReasonMessage").toString());
      } else {
        document.setRejectionReasonMessage("");
      }
      // TODO: the cast to enum might fail
      document.setRejectionReasonType(
          (DocumentRejectReason) providerResponse.getProviderData("rejectionReasonType"));
      logService.debug(
          requestId,
          "L",
          "rejectionReasonType",
          providerResponse.getProviderData("rejectionReasonType"));

      // update document audit
      document.updateAudit(application.getApplicationStamp());

      // save document
      documentService.saveDocument(requestId, account, document);
      logService.debug(requestId, "L", "dbservice", "saved");
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void updateUboDeclarationStatus(
      String requestId,
      Account account,
      Application application,
      User user,
      UboDeclaration uboDeclaration)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(requestId, "IN", "document.id", uboDeclaration.getId());
    logService.debug(requestId, "IN", "document.status", uboDeclaration.getStatus());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.VIEWUBODECLARATION);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // check KYC document status at provider, see if it changed
    ProviderResponse providerResponse =
        providerService.getProviderUboDeclaration(requestId, user, uboDeclaration);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    logService.debug(
        requestId,
        "L",
        "processedDate",
        providerResponse.getProviderData("processedDate").toString());

    // if status was changed to REGULARs
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {

      logService.debug(requestId, "L", "status", providerResponse.getProviderData("status"));
      logService.debug(requestId, "L", "reason", providerResponse.getProviderData("reason"));

      uboDeclaration.setProcessedDate(
          Integer.valueOf(providerResponse.getProviderData("processedDate").toString()));
      uboDeclaration.setCreatedAt(
          Long.valueOf(providerResponse.getProviderData("createdAt").toString()));
      uboDeclaration.setStatus(
          EnumUtils.getEnum(
              UboDeclarationStatus.class, providerResponse.getProviderData("status").toString()));
      uboDeclaration.setMessage(
          StringUtils.defaultString((String) providerResponse.getProviderData("message")));
      uboDeclaration.setReason(
          EnumUtils.getEnum(
              UboDeclarationRefusedReasonType.class,
              providerResponse.getProviderData("reason").toString()));

      // update document audit
      uboDeclaration.updateAudit(application.getApplicationStamp());

      logService.debug(requestId, "L", "uboDeclaration", uboDeclaration);

      // save document
      uboService.saveUboDeclaration(requestId, account, uboDeclaration);
      logService.debug(requestId, "L", "dbservice", "saved");
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public BankAccountIBAN createBankAccountIBAN(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_IBAN requestCreateBankAccountIBAN)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountIBAN.iban",
        requestCreateBankAccountIBAN.getIban());
    logService.debug(
        requestId, "IN", "requestCreateBankAccountIBAN.bic", requestCreateBankAccountIBAN.getBic());

    // create bank account address
    Address bankAccountAddress = new Address();
    bankAccountAddress.setAddressLine1(
        requestCreateBankAccountIBAN.getOwnerAddress().getAddressLine1());
    bankAccountAddress.setAddressLine2(
        requestCreateBankAccountIBAN.getOwnerAddress().getAddressLine2());
    bankAccountAddress.setCity(requestCreateBankAccountIBAN.getOwnerAddress().getCity());
    bankAccountAddress.setCounty(requestCreateBankAccountIBAN.getOwnerAddress().getCounty());
    bankAccountAddress.setCountry(
        CountryISO.valueOf(requestCreateBankAccountIBAN.getOwnerAddress().getCountry()));
    bankAccountAddress.setPostalCode(
        requestCreateBankAccountIBAN.getOwnerAddress().getPostalCode());

    // create bank account
    BankAccountIBAN bankAccount = new BankAccountIBAN();
    bankAccount.setUserId(requestCreateBankAccountIBAN.getUserId());
    bankAccount.setOwnerAddress(bankAccountAddress);
    bankAccount.setOwnerName(requestCreateBankAccountIBAN.getOwnerName());
    bankAccount.setCustomTag(requestCreateBankAccountIBAN.getCustomTag());

    // IBAN specific fields
    // update data for IBAN - remove everything which is not letter or digit and upper case the IBAN
    // string
    bankAccount.setType(BankAccountType.IBAN);
    bankAccount.setIban(
        requestCreateBankAccountIBAN.getIban().replaceAll("[^A-Za-z0-9]+", "").toUpperCase());
    bankAccount.setBic(requestCreateBankAccountIBAN.getBic());

    // update audit
    bankAccount.setActive(true);
    bankAccount.updateAudit(application.getApplicationStamp());

    bankAccount.setId(ObjectId.get().toString());
    bankAccount.setCreatedAt(utilsService.getTimeStamp());

    try {
      // register bank account to provider
      createProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "created");

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "bankAccount", bankAccount);

    // return response
    return bankAccount;
  }

  @Override
  public BankAccountUS createBankAccountUS(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_US requestCreateBankAccountUS)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(
        requestId,
        "IN",
        "RequestCreateBankAccount_US.accountNumber",
        requestCreateBankAccountUS.getAccountNumber());
    logService.debug(
        requestId, "IN", "RequestCreateBankAccount_US.aba", requestCreateBankAccountUS.getAba());
    logService.debug(
        requestId,
        "IN",
        "RequestCreateBankAccount_US.depositAccountType",
        requestCreateBankAccountUS.getDepositAccountType());

    // create bank account address
    Address bankAccountAddress = new Address();
    bankAccountAddress.setAddressLine1(
        requestCreateBankAccountUS.getOwnerAddress().getAddressLine1());
    bankAccountAddress.setAddressLine2(
        requestCreateBankAccountUS.getOwnerAddress().getAddressLine2());
    bankAccountAddress.setCity(requestCreateBankAccountUS.getOwnerAddress().getCity());
    bankAccountAddress.setCounty(requestCreateBankAccountUS.getOwnerAddress().getCounty());
    bankAccountAddress.setCountry(
        CountryISO.valueOf(requestCreateBankAccountUS.getOwnerAddress().getCountry()));
    bankAccountAddress.setPostalCode(requestCreateBankAccountUS.getOwnerAddress().getPostalCode());

    // create bank account
    BankAccountUS bankAccount = new BankAccountUS();
    bankAccount.setUserId(requestCreateBankAccountUS.getUserId());
    bankAccount.setOwnerAddress(bankAccountAddress);
    bankAccount.setOwnerName(requestCreateBankAccountUS.getOwnerName());
    bankAccount.setCustomTag(requestCreateBankAccountUS.getCustomTag());

    // US specific fields
    bankAccount.setType(BankAccountType.US);
    bankAccount.setAccountNumber(requestCreateBankAccountUS.getAccountNumber());
    bankAccount.setAba(requestCreateBankAccountUS.getAba());
    bankAccount.setDepositAccountType(
        DepositAccountType.valueOf(requestCreateBankAccountUS.getDepositAccountType()));

    bankAccount.setId(ObjectId.get().toString());
    bankAccount.setCreatedAt(utilsService.getTimeStamp());

    // update audit
    bankAccount.setActive(true);
    bankAccount.updateAudit(application.getApplicationStamp());

    try {
      // register bank account to provider
      createProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "created");

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "bankAccount", bankAccount);

    // return response
    return bankAccount;
  }

  @Override
  public BankAccountGB createBankAccountGB(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_GB requestCreateBankAccountGB)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountGB.accountNumber",
        requestCreateBankAccountGB.getAccountNumber());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountGB.sortCode",
        requestCreateBankAccountGB.getSortCode());

    // create bank account address
    Address bankAccountAddress = new Address();
    bankAccountAddress.setAddressLine1(
        requestCreateBankAccountGB.getOwnerAddress().getAddressLine1());
    bankAccountAddress.setAddressLine2(
        requestCreateBankAccountGB.getOwnerAddress().getAddressLine2());
    bankAccountAddress.setCity(requestCreateBankAccountGB.getOwnerAddress().getCity());
    bankAccountAddress.setCounty(requestCreateBankAccountGB.getOwnerAddress().getCounty());
    bankAccountAddress.setCountry(
        CountryISO.valueOf(requestCreateBankAccountGB.getOwnerAddress().getCountry()));
    bankAccountAddress.setPostalCode(requestCreateBankAccountGB.getOwnerAddress().getPostalCode());

    // create bank account
    BankAccountGB bankAccount = new BankAccountGB();
    bankAccount.setUserId(requestCreateBankAccountGB.getUserId());
    bankAccount.setOwnerAddress(bankAccountAddress);
    bankAccount.setOwnerName(requestCreateBankAccountGB.getOwnerName());
    bankAccount.setCustomTag(requestCreateBankAccountGB.getCustomTag());

    // update data for GB
    bankAccount.setType(BankAccountType.GB);
    bankAccount.setSortCode(requestCreateBankAccountGB.getSortCode());
    bankAccount.setAccountNumber(requestCreateBankAccountGB.getAccountNumber());

    bankAccount.setId(ObjectId.get().toString());
    bankAccount.setCreatedAt(utilsService.getTimeStamp());

    // update audit
    bankAccount.setActive(true);
    bankAccount.updateAudit(application.getApplicationStamp());

    try {
      // register bank account to provider
      createProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "created");

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "bankAccount", bankAccount);

    // return response
    return bankAccount;
  }

  @Override
  public BankAccountCA createBankAccountCA(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_CA requestCreateBankAccountCA)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountCA.accountNumber",
        requestCreateBankAccountCA.getAccountNumber());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountCA.bankName",
        requestCreateBankAccountCA.getBankName());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountCA.branchCode",
        requestCreateBankAccountCA.getBranchCode());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountCA.institutionNumber",
        requestCreateBankAccountCA.getInstitutionNumber());

    // create bank account address
    Address bankAccountAddress = new Address();
    bankAccountAddress.setAddressLine1(
        requestCreateBankAccountCA.getOwnerAddress().getAddressLine1());
    bankAccountAddress.setAddressLine2(
        requestCreateBankAccountCA.getOwnerAddress().getAddressLine2());
    bankAccountAddress.setCity(requestCreateBankAccountCA.getOwnerAddress().getCity());
    bankAccountAddress.setCounty(requestCreateBankAccountCA.getOwnerAddress().getCounty());
    bankAccountAddress.setCountry(
        CountryISO.valueOf(requestCreateBankAccountCA.getOwnerAddress().getCountry()));
    bankAccountAddress.setPostalCode(requestCreateBankAccountCA.getOwnerAddress().getPostalCode());

    // create bank account
    BankAccountCA bankAccount = new BankAccountCA();
    bankAccount.setUserId(requestCreateBankAccountCA.getUserId());
    bankAccount.setOwnerAddress(bankAccountAddress);
    bankAccount.setOwnerName(requestCreateBankAccountCA.getOwnerName());
    bankAccount.setCustomTag(requestCreateBankAccountCA.getCustomTag());

    // update data for CA
    bankAccount.setType(BankAccountType.CA);
    bankAccount.setBranchCode(requestCreateBankAccountCA.getBranchCode());
    bankAccount.setInstitutionNumber(requestCreateBankAccountCA.getInstitutionNumber());
    bankAccount.setAccountNumber(requestCreateBankAccountCA.getAccountNumber());
    bankAccount.setBankName(requestCreateBankAccountCA.getBankName());

    bankAccount.setId(ObjectId.get().toString());
    bankAccount.setCreatedAt(utilsService.getTimeStamp());

    // update audit
    bankAccount.setActive(true);
    bankAccount.updateAudit(application.getApplicationStamp());

    try {
      // register bank account to provider
      createProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "created");

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "bankAccount", bankAccount);

    // return response
    return bankAccount;
  }

  @Override
  public BankAccountOTHER createBankAccountOTHER(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_OTHER requestCreateBankAccountOTHER)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountOTHER.country",
        requestCreateBankAccountOTHER.getCountry());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountOTHER.bic",
        requestCreateBankAccountOTHER.getBic());
    logService.debug(
        requestId,
        "IN",
        "requestCreateBankAccountOTHER.accountNumber",
        requestCreateBankAccountOTHER.getAccountNumber());

    // create bank account address
    Address bankAccountAddress = new Address();
    bankAccountAddress.setAddressLine1(
        requestCreateBankAccountOTHER.getOwnerAddress().getAddressLine1());
    bankAccountAddress.setAddressLine2(
        requestCreateBankAccountOTHER.getOwnerAddress().getAddressLine2());
    bankAccountAddress.setCity(requestCreateBankAccountOTHER.getOwnerAddress().getCity());
    bankAccountAddress.setCounty(requestCreateBankAccountOTHER.getOwnerAddress().getCounty());
    bankAccountAddress.setCountry(
        CountryISO.valueOf(requestCreateBankAccountOTHER.getOwnerAddress().getCountry()));
    bankAccountAddress.setPostalCode(
        requestCreateBankAccountOTHER.getOwnerAddress().getPostalCode());

    // create bank account
    BankAccountOTHER bankAccount = new BankAccountOTHER();
    bankAccount.setUserId(requestCreateBankAccountOTHER.getUserId());
    bankAccount.setOwnerAddress(bankAccountAddress);
    bankAccount.setOwnerName(requestCreateBankAccountOTHER.getOwnerName());
    bankAccount.setCustomTag(requestCreateBankAccountOTHER.getCustomTag());

    // update data for OTHER
    bankAccount.setType(BankAccountType.OTHER);
    bankAccount.setCountry(CountryISO.valueOf(requestCreateBankAccountOTHER.getCountry()));
    bankAccount.setBic(requestCreateBankAccountOTHER.getBic());
    bankAccount.setAccountNumber(requestCreateBankAccountOTHER.getAccountNumber());

    bankAccount.setId(ObjectId.get().toString());
    bankAccount.setCreatedAt(utilsService.getTimeStamp());

    // update audit
    bankAccount.setActive(true);
    bankAccount.updateAudit(application.getApplicationStamp());

    try {
      // register bank account to provider
      createProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "created");

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "bankAccount", bankAccount);

    // return response
    return bankAccount;
  }

  @Override
  public void deactivateBankAccount(
      String requestId,
      Account account,
      Application application,
      User user,
      BankAccount bankAccount)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user.id", user.getId());
    logService.debug(requestId, "IN", "bankAccount.id", bankAccount.getId());
    logService.debug(requestId, "IN", "bankAccount.providerId", bankAccount.getProviderId());

    try {
      // register bank de activation to provider
      deactivateProviderBankAccount(requestId, account, application, user, bankAccount);
      logService.debug(requestId, "L", "provider", "deactivated");

      // update audit
      bankAccount.updateAudit(application.getApplicationStamp());

      // save bank account
      bankAccountService.saveBankAccount(requestId, account, bankAccount);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public Wallet createWallet(
      String requestId,
      Account account,
      Application application,
      User owner,
      RequestCreateWallet requestCreateWallet)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(
        requestId, "IN", "requestCreateWallet.userId", requestCreateWallet.getUserId());
    logService.debug(
        requestId, "IN", "requestCreateWallet.description", requestCreateWallet.getDescription());
    logService.debug(
        requestId, "IN", "requestCreateWallet.currency", requestCreateWallet.getCurrency());
    logService.debug(
        requestId, "IN", "requestCreateWallet.customTag", requestCreateWallet.getCustomTag());

    // create wallet
    Wallet newWallet = new Wallet();
    newWallet.setUserId(requestCreateWallet.getUserId());
    newWallet.setDescription(requestCreateWallet.getDescription());
    newWallet.setCustomTag(requestCreateWallet.getCustomTag());
    newWallet.setCurrency(CurrencyISO.valueOf(requestCreateWallet.getCurrency()));

    // create default balance
    Amount balance = new Amount();
    balance.setCurrency(CurrencyISO.valueOf(requestCreateWallet.getCurrency()));
    balance.setValue(0);

    Amount balanceBlocked = new Amount();
    balanceBlocked.setCurrency(CurrencyISO.valueOf(requestCreateWallet.getCurrency()));
    balanceBlocked.setValue(0);

    // update balance and audit
    newWallet.setBalance(balance);
    newWallet.setBlockedBalance(balanceBlocked);
    newWallet.updateAudit(application.getApplicationStamp());
    newWallet.setId(ObjectId.get().toString());
    newWallet.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);

    try {
      // register wallet to provider
      createProviderWallet(requestId, account, application, owner, newWallet);

      // return wallet response
      walletService.saveWallet(requestId, account, newWallet);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "newWallet", newWallet);

    // return response
    return newWallet;
  }

  @Override
  public Document createDocument(
      String requestId,
      Account account,
      Application application,
      User owner,
      RequestCreateDocument requestCreateDocument)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(
        requestId, "IN", "requestCreateDocument.userId", requestCreateDocument.getUserId());
    logService.debug(
        requestId, "IN", "requestCreateDocument.userId", requestCreateDocument.getType());
    logService.debug(
        requestId, "IN", "requestCreateDocument.customTag", requestCreateDocument.getCustomTag());

    // create document
    Document newDocument = new Document();
    newDocument.setCustomTag(requestCreateDocument.getCustomTag());
    newDocument.setUserId(requestCreateDocument.getUserId());
    newDocument.setType(DocumentType.valueOf(requestCreateDocument.getType()));
    newDocument.setStatus(DocumentStatus.CREATED);

    newDocument.setId(ObjectId.get().toString());
    newDocument.setCreatedAt(utilsService.getTimeStamp());

    // create document audit
    newDocument.updateAudit(application.getApplicationStamp());

    try {
      // register document to provider
      createProviderDocument(requestId, account, application, owner, newDocument);

      // save document
      documentService.saveDocument(requestId, account, newDocument);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "newDocument", newDocument);

    // return document
    return newDocument;
  }

  @Override
  public void createDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String file)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(requestId, "IN", "document.id", document.getId());
    logService.debug(requestId, "IN", "file", file.substring(0, 50));

    // TODO implement AWS bucket save and instead of dummy file name, use S3 bucket links - CAN be
    // done later, because functionality already exists on M3 service
    String fileName = UUID.randomUUID().toString();

    // get current document pages
    List<String> currentDocumentPages = document.getPages();

    // if no page created
    if (currentDocumentPages == null) {
      currentDocumentPages = new ArrayList<String>();
    }

    // add the new page to list
    currentDocumentPages.add(fileName);

    // attach pages to document
    document.setPages(currentDocumentPages);

    // create document audit
    document.updateAudit(application.getApplicationStamp());

    try {
      // start save page to provider - we need it to be sync since there are several pages sent at
      // once
      createProviderDocumentPage(requestId, account, application, owner, document, file);
      logService.debug(requestId, "L", "provider", "created");

      // save document
      documentService.saveDocument(requestId, account, document);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public void submitDocument(
      String requestId, Account account, Application application, Document document)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "document.id", document.getId());

    // update document audit
    document.updateAudit(application.getApplicationStamp());

    try {
      // submit document for provider approval
      submitProviderDocument(requestId, account, application, document);
      logService.debug(requestId, "L", "provider", "submitted");

      // save document
      documentService.saveDocument(requestId, account, document);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public UboDeclaration createUboDeclaration(
      String requestId, Account account, Application application, User owner) {

    String userId = owner.getId();
    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", userId);

    // create document
    UboDeclaration newDocument = new UboDeclaration();
    newDocument.setUserId(userId);
    newDocument.setId(ObjectId.get().toString());
    newDocument.setCreatedAt(utilsService.getTimeStamp());

    // create document audit
    newDocument.updateAudit(application.getApplicationStamp());

    try {
      // register document to provider
      createProviderUboDeclaration(requestId, account, application, owner, newDocument);

      // save document
      uboService.saveUboDeclaration(requestId, account, newDocument);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "newDocument", newDocument);

    // return document
    return newDocument;
  }

  @Override
  public void submitUboDeclaration(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(requestId, "IN", "uboDeclaration.id", uboDeclaration.getId());

    // update document audit
    uboDeclaration.updateAudit(application.getApplicationStamp());

    try {
      // submit document for provider approval
      submitProviderUboDeclaration(requestId, account, application, owner, uboDeclaration);
      logService.debug(requestId, "L", "provider", "submitted");

      // save document
      uboService.saveUboDeclaration(requestId, account, uboDeclaration);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public Ubo createUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      RequestUbo requestUbo) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(requestId, "IN", "userId", uboDeclaration.getUserId());
    logService.debug(requestId, "IN", "ubo declaration Id", uboDeclaration.getId());

    Ubo ubo = new Ubo();

    Address address = new Address();
    address.setAddressLine1(requestUbo.getAddress().getAddressLine1());
    address.setAddressLine2(requestUbo.getAddress().getAddressLine2());
    address.setCity(requestUbo.getAddress().getCity());
    address.setCounty(requestUbo.getAddress().getCounty());
    address.setCountry(CountryISO.valueOf(requestUbo.getAddress().getCountry()));
    address.setPostalCode(requestUbo.getAddress().getPostalCode());
    ubo.setAddress(address);

    ubo.setBirthday(requestUbo.getBirthday());

    Birthplace birthPlace = new Birthplace();
    birthPlace.setCity(requestUbo.getBirthplace().getCity());
    birthPlace.setCountry(CountryISO.valueOf(requestUbo.getBirthplace().getCountry()));
    ubo.setBirthplace(birthPlace);

    ubo.setFirstName(requestUbo.getFirstName());
    ubo.setLastName(requestUbo.getLastName());
    ubo.setNationality(CountryISO.valueOf(requestUbo.getNationality()));

    uboDeclaration.updateAudit(application.getApplicationStamp());

    try {
      createProviderUbo(requestId, account, application, owner, uboDeclaration, ubo);
      uboDeclaration.addUbo(ubo);
      // save ubo declaration in db
      uboService.saveUboDeclaration(requestId, account, uboDeclaration);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "ubo", ubo);

    return ubo;
  }

  @Override
  public Ubo updateUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      String uboId,
      RequestUbo requestUbo) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "owner.id", owner.getId());
    logService.debug(requestId, "IN", "userId", uboDeclaration.getUserId());
    logService.debug(requestId, "IN", "ubo declaration Id", uboDeclaration.getId());
    logService.debug(requestId, "IN", "uboId", uboId);

    Ubo ubo = new Ubo();
    ubo.setProviderId(uboId);

    Address address = new Address();
    address.setAddressLine1(requestUbo.getAddress().getAddressLine1());
    address.setAddressLine2(requestUbo.getAddress().getAddressLine2());
    address.setCity(requestUbo.getAddress().getCity());
    address.setCounty(requestUbo.getAddress().getCounty());
    address.setCountry(CountryISO.valueOf(requestUbo.getAddress().getCountry()));
    address.setPostalCode(requestUbo.getAddress().getPostalCode());
    ubo.setAddress(address);

    ubo.setBirthday(requestUbo.getBirthday());

    Birthplace birthPlace = new Birthplace();
    birthPlace.setCity(requestUbo.getBirthplace().getCity());
    birthPlace.setCountry(CountryISO.valueOf(requestUbo.getBirthplace().getCountry()));
    ubo.setBirthplace(birthPlace);

    ubo.setFirstName(requestUbo.getFirstName());
    ubo.setLastName(requestUbo.getLastName());
    ubo.setNationality(CountryISO.valueOf(requestUbo.getNationality()));

    uboDeclaration.updateAudit(application.getApplicationStamp());

    try {
      updateProviderUbo(requestId, account, application, owner, uboDeclaration, ubo);
      uboDeclaration.updateUbo(ubo);
      // save ubo declaration in db
      uboService.saveUboDeclaration(requestId, account, uboDeclaration);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "ubo", ubo);

    return ubo;
  }

  @Override
  public DepositCard deactivateDepositCard(
      String requestId,
      Account sessionAccount,
      Application sessionApplication,
      DepositCard depositCard) {

    logService.debug(requestId, "IN", "account", sessionAccount.getId());
    logService.debug(requestId, "IN", "user", sessionApplication.getApplicationId());
    logService.debug(requestId, "IN", "depositCard", depositCard.getId());

    try {
      // submit document for provider approval
      deactivateProviderDepositCard(requestId, sessionAccount, sessionApplication, depositCard);
      logService.debug(requestId, "L", "provider", "submitted");

      Map<String, Object> fields = new HashMap<>();
      fields.put("validity", depositCard.getValidity());
      fields.put("active", depositCard.isActive());

      depositCardService.updateDepositCard(
          requestId, sessionAccount, sessionApplication, depositCard, fields);
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");

    return depositCard;
  }

  private void deactivateProviderDepositCard(
      String requestId,
      Account sessionAccount,
      Application sessionApplication,
      DepositCard depositCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.DEACTIVATEDEPOSITCARD);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.deactivateProviderDepositCard(requestId, depositCard);
    logService.debug(requestId, "L", "providerResponse", Json.toJson(providerResponse));

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // update validity for card
      depositCard.setValidity(
          CardValidity.valueOf(String.valueOf(providerResponse.getProviderData("validity"))));
      depositCard.setActive(Boolean.valueOf(providerResponse.getProviderData("active").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  @Override
  public synchronized List<Transaction> createDirectPayIn(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      RequestCreateDirectPayIn requestCreateDirectPayIn)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "depositCard", Json.parse(depositCard.toString()).toString());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(
        requestId, "IN", "requestedAmount", requestCreateDirectPayIn.getAmount().getValue());
    logService.debug(
        requestId, "IN", "requestedFees", requestCreateDirectPayIn.getFees().getValue());
    logService.debug(requestId, "IN", "requestedFeeModel", requestCreateDirectPayIn.getFeeModel());
    logService.debug(requestId, "IN", "customTag", requestCreateDirectPayIn.getCustomTag());
    logService.debug(
        requestId, "IN", "returnURL", requestCreateDirectPayIn.getSecureModeReturnURL());
    logService.debug(requestId, "IN", "secureMode", requestCreateDirectPayIn.getSecureMode());
    logService.debug(
        requestId, "IN", "statementDescriptor", requestCreateDirectPayIn.getStatementDescriptor());

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // Initialise final variables to be used in async call to provider
    final Transfer payInFee;
    final PayIn payIn;
    final Wallet accountWallet;

    // create fee amount object
    Amount feesAmount = new Amount();
    feesAmount.setCurrency(CurrencyISO.valueOf(requestCreateDirectPayIn.getFees().getCurrency()));
    feesAmount.setValue(requestCreateDirectPayIn.getFees().getValue());

    // decide processing mode based on fee model and value
    if (feesAmount.getValue() > 0) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // transaction value is based on total minus fee. Fee are extracted from pay in amount
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(
          CurrencyISO.valueOf(requestCreateDirectPayIn.getAmount().getCurrency()));

      // based on fee model we calculate the actual pay in amount
      if ((requestCreateDirectPayIn.getFeeModel() == null)
          || requestCreateDirectPayIn.getFeeModel().equals(FeeModel.INCLUDED.toString())) {
        // fee is included in transfer amount
        transferAmount.setValue(requestCreateDirectPayIn.getAmount().getValue());
      } else {
        // fee is not included in transfer amount
        transferAmount.setValue(
            requestCreateDirectPayIn.getAmount().getValue() + feesAmount.getValue());
      }

      // register main transaction
      payIn =
          createDirectPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              requestCreateDirectPayIn);
      logService.debug(requestId, "L", "createPayInTransaction", payIn.getId());

      // set credit side
      accountWallet =
          accountSettingsService.getAccountWallet(requestId, account, feesAmount.getCurrency());

      // register fee transaction
      payInFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              creditWallet,
              accountWallet,
              feesAmount,
              requestCreateDirectPayIn.getCustomTag(),
              TransactionType.PAYIN_FEE,
              TransactionNature.REGULAR,
              payIn.getId().toString(),
              false);
      logService.debug(requestId, "L", "createNoFxTransfer", payInFee.getId());

      // update relations
      updatePayInRelation(requestId, account, application, payIn, payInFee.getId());
      logService.debug(requestId, "L", "updatePayInRelation", "updated");

      // add main transaction to response list
      result.add(payIn);

      // add fee transaction to response list
      result.add(payInFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      accountWallet = null;
      payInFee = null;

      // if we have no fees or fee model is NOT_INCLUDED
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(
          CurrencyISO.valueOf(requestCreateDirectPayIn.getAmount().getCurrency()));
      transferAmount.setValue(requestCreateDirectPayIn.getAmount().getValue());

      // register main transaction
      payIn =
          createDirectPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              requestCreateDirectPayIn);
      logService.debug(requestId, "L", "createPayInTransaction", payIn.getId());

      // add main transaction to response list
      result.add(payIn);
    }

    // instantiate flow step 2 - call provider to register transaction
    createProviderDirectPayIn(
        requestId,
        account,
        application,
        depositCard,
        user,
        creditWallet,
        accountWallet,
        payIn,
        payInFee);
    logService.debug(requestId, "L", "createProviderPayIn", "created");

    // output
    logService.debug(requestId, "OUT", "result", result);

    return result;
  }

  @Override
  public synchronized List<Transaction> createAVSDirectPayIn(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      RequestCreateAVSDirectPayIn requestCreateAVSDirectPayIn)
      throws GenericRestException {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "depositCard", Json.parse(depositCard.toString()).toString());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(
        requestId, "IN", "requestedAmount", requestCreateAVSDirectPayIn.getAmount().getValue());
    logService.debug(
        requestId, "IN", "requestedFees", requestCreateAVSDirectPayIn.getFees().getValue());
    logService.debug(
        requestId, "IN", "requestedFeeModel", requestCreateAVSDirectPayIn.getFeeModel());
    logService.debug(requestId, "IN", "customTag", requestCreateAVSDirectPayIn.getCustomTag());
    logService.debug(
        requestId, "IN", "returnURL", requestCreateAVSDirectPayIn.getSecureModeReturnURL());
    logService.debug(requestId, "IN", "secureMode", requestCreateAVSDirectPayIn.getSecureMode());
    logService.debug(
        requestId,
        "IN",
        "statementDescriptor",
        requestCreateAVSDirectPayIn.getStatementDescriptor());

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // Initialise final variables to be used in async call to provider
    final Transfer payInFee;
    final PayIn payIn;
    final Wallet accountWallet;

    // create fee amount object
    Amount feesAmount = new Amount();
    feesAmount.setCurrency(
        CurrencyISO.valueOf(requestCreateAVSDirectPayIn.getFees().getCurrency()));
    feesAmount.setValue(requestCreateAVSDirectPayIn.getFees().getValue());

    // decide processing mode based on fee model and value
    if (feesAmount.getValue() > 0) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // transaction value is based on total minus fee. Fee are extracted from pay in amount
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(
          CurrencyISO.valueOf(requestCreateAVSDirectPayIn.getAmount().getCurrency()));

      // based on fee model we calculate the actual pay in amount
      if ((requestCreateAVSDirectPayIn.getFeeModel() == null)
          || requestCreateAVSDirectPayIn.getFeeModel().equals(FeeModel.INCLUDED.toString())) {
        // fee is included in transfer amount
        transferAmount.setValue(requestCreateAVSDirectPayIn.getAmount().getValue());
      } else {
        // fee is not included in transfer amount
        transferAmount.setValue(
            requestCreateAVSDirectPayIn.getAmount().getValue() + feesAmount.getValue());
      }

      // register main transaction
      payIn =
          createDirectPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              requestCreateAVSDirectPayIn);
      logService.debug(requestId, "L", "createPayInTransaction", payIn.getId());

      // set credit side
      accountWallet =
          accountSettingsService.getAccountWallet(requestId, account, feesAmount.getCurrency());

      // register fee transaction
      payInFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              creditWallet,
              accountWallet,
              feesAmount,
              requestCreateAVSDirectPayIn.getCustomTag(),
              TransactionType.PAYIN_FEE,
              TransactionNature.REGULAR,
              payIn.getId().toString(),
              false);
      logService.debug(requestId, "L", "createNoFxTransfer", payInFee.getId());

      // update relations
      updatePayInRelation(requestId, account, application, payIn, payInFee.getId());
      logService.debug(requestId, "L", "updatePayInRelation", "updated");

      // add main transaction to response list
      result.add(payIn);

      // add fee transaction to response list
      result.add(payInFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      accountWallet = null;
      payInFee = null;

      // if we have no fees or fee model is NOT_INCLUDED
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(
          CurrencyISO.valueOf(requestCreateAVSDirectPayIn.getAmount().getCurrency()));
      transferAmount.setValue(requestCreateAVSDirectPayIn.getAmount().getValue());

      // register main transaction
      payIn =
          createDirectPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              requestCreateAVSDirectPayIn);
      logService.debug(requestId, "L", "createPayInTransaction", Json.toJson(payIn).toString());

      // add main transaction to response list
      result.add(payIn);
    }

    // instantiate flow step 2 - call provider to register transaction
    createProviderDirectPayIn(
        requestId,
        account,
        application,
        depositCard,
        user,
        creditWallet,
        accountWallet,
        payIn,
        payInFee);
    logService.debug(requestId, "L", "createProviderPayIn", "created");

    // output
    logService.debug(requestId, "OUT", "result", result);

    return result;
  }

  @Override
  public synchronized List<Transaction> createPayIn(
      String requestId,
      Account account,
      Application application,
      User user,
      Wallet creditWallet,
      RequestAmount requestedAmount,
      RequestAmount requestedFees,
      String requestedFeeModel,
      String customTag,
      String returnURL,
      String cardType,
      String secureMode,
      String culture,
      String templateURL,
      String statementDescriptor) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "requestedAmount", requestedAmount.getValue());
    logService.debug(requestId, "IN", "requestedFees", requestedFees);
    logService.debug(requestId, "IN", "requestedFeeModel", requestedFeeModel);
    logService.debug(requestId, "IN", "customTag", customTag);
    logService.debug(requestId, "IN", "returnURL", returnURL);
    logService.debug(requestId, "IN", "cardType", cardType);
    logService.debug(requestId, "IN", "secureMode", secureMode);
    logService.debug(requestId, "IN", "culture", culture);
    logService.debug(requestId, "IN", "templateURL", templateURL);
    logService.debug(requestId, "IN", "statementDescriptor", statementDescriptor);

    // Initialise final variables to be used in async call to provider
    final Transfer payInFee;
    final PayIn payIn;
    final Wallet accountWallet;

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // create fee amount object
    Amount feesAmount = new Amount();
    feesAmount.setCurrency(CurrencyISO.valueOf(requestedFees.getCurrency()));
    feesAmount.setValue(requestedFees.getValue());

    // decide processing mode based on fee model and value
    if (feesAmount.getValue() > 0) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // transaction value is based on total minus fee. Fee are extracted from pay in amount
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));

      // based on fee model we calculate the actual pay in amount
      if ((requestedFeeModel == null) || requestedFeeModel.equals(FeeModel.INCLUDED.toString())) {
        // fee is included in transfer amount
        transferAmount.setValue(requestedAmount.getValue());
      } else {
        // fee is not included in transfer amount
        transferAmount.setValue(requestedAmount.getValue() + feesAmount.getValue());
      }

      // register main transaction
      payIn =
          createPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              customTag,
              returnURL,
              CardType.valueOf(cardType),
              SecureMode.valueOf(secureMode),
              CultureCode.valueOf(culture),
              templateURL,
              statementDescriptor);
      logService.debug(requestId, "L", "createPayInTransaction", payIn.getId());

      // set credit side
      accountWallet =
          accountSettingsService.getAccountWallet(requestId, account, feesAmount.getCurrency());

      // register fee transaction
      payInFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              creditWallet,
              accountWallet,
              feesAmount,
              customTag,
              TransactionType.PAYIN_FEE,
              TransactionNature.REGULAR,
              payIn.getId().toString(),
              false);
      logService.debug(requestId, "L", "createNoFxTransfer", payInFee.getId());

      // update relations
      updatePayInRelation(requestId, account, application, payIn, payInFee.getId());
      logService.debug(requestId, "L", "updatePayInRelation", "updated");

      // add main transaction to response list
      result.add(payIn);

      // add fee transaction to response list
      result.add(payInFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      accountWallet = null;
      payInFee = null;

      // if we have no fees or fee model is NOT_INCLUDED
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));
      transferAmount.setValue(requestedAmount.getValue());

      // register main transaction
      payIn =
          createPayInTransaction(
              requestId,
              account,
              application,
              creditWallet,
              transferAmount,
              customTag,
              returnURL,
              CardType.valueOf(cardType),
              SecureMode.valueOf(secureMode),
              CultureCode.valueOf(culture),
              templateURL,
              statementDescriptor);
      logService.debug(requestId, "L", "createPayInTransaction", payIn.getId());

      // add main transaction to response list
      result.add(payIn);
    }

    // instantiate flow step 2 - call provider to register transaction
    createProviderPayIn(
        requestId, account, application, user, creditWallet, accountWallet, payIn, payInFee);
    logService.debug(requestId, "L", "createProviderPayIn", "created");

    // output
    logService.debug(requestId, "OUT", "result", result);

    return result;
  }

  @Override
  public synchronized List<Transaction> createTransfer(
      String requestId,
      Account account,
      Application application,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet,
      RequestAmount requestedAmount,
      RequestAmount requestedFees,
      String requestedFeeModel,
      String customTag) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "creditUser", creditUser.getId());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "requestedAmount", requestedAmount.getValue());
    logService.debug(requestId, "IN", "requestedFees", requestedFees);
    logService.debug(requestId, "IN", "requestedFeeModel", requestedFeeModel);
    logService.debug(requestId, "IN", "customTag", customTag);

    // Initialise final variables to be used in async call to provider
    final Transfer transferFee;
    final Transfer transfer;
    final Wallet accountWallet;

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // create fee amount object
    Amount feesAmount = new Amount();
    feesAmount.setCurrency(CurrencyISO.valueOf(requestedFees.getCurrency()));
    feesAmount.setValue(requestedFees.getValue());

    // decide processing mode based on fee model and value
    if (feesAmount.getValue() > 0) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // transaction value is based on total minus fee. Fee are extracted from transfer amount
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));

      // based on fee model we calculate the actual transfer amount
      if ((requestedFeeModel == null) || requestedFeeModel.equals(FeeModel.INCLUDED.toString())) {
        // fee is included in transfer amount
        transferAmount.setValue(requestedAmount.getValue() - feesAmount.getValue());
      } else {
        // fee is not included in transfer amount
        transferAmount.setValue(requestedAmount.getValue());
      }

      // register main transaction
      transfer =
          createNoFxTransfer(
              requestId,
              account,
              application,
              debitWallet,
              creditWallet,
              transferAmount,
              customTag,
              TransactionType.TRANSFER,
              TransactionNature.REGULAR,
              null,
              true);
      logService.debug(requestId, "L", "createNoFxTransfer", transfer.getId());

      // set credit side
      accountWallet =
          accountSettingsService.getAccountWallet(requestId, account, feesAmount.getCurrency());

      // register fee transaction
      transferFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              debitWallet,
              accountWallet,
              feesAmount,
              customTag,
              TransactionType.TRANSFER_FEE,
              TransactionNature.REGULAR,
              transfer.getId().toString(),
              true);
      logService.debug(requestId, "L", "createNoFxTransfer", transferFee.getId());

      // update relations
      updateTransferRelation(requestId, account, application, transfer, transferFee.getId());
      logService.debug(requestId, "L", "updateTransferRelation", "updated");

      // add main transaction to response list
      result.add(transfer);

      // add fee transaction to response list
      result.add(transferFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      // final variables do require a initialisation
      transferFee = null;

      // if we have no fees or fee model is NOT_INCLUDED
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));
      transferAmount.setValue(requestedAmount.getValue());

      // register main transaction
      transfer =
          createNoFxTransfer(
              requestId,
              account,
              application,
              debitWallet,
              creditWallet,
              transferAmount,
              customTag,
              TransactionType.TRANSFER,
              TransactionNature.REGULAR,
              null,
              true);
      logService.debug(requestId, "L", "createNoFxTransfer", Json.toJson(transfer).toString());

      // add main transaction to response list
      result.add(transfer);
    }

    // instantiate flow step 2 - call provider to register transaction
    CompletableFuture.supplyAsync(
        () ->
            createProviderTransfer(
                requestId,
                account,
                application,
                debitUser,
                debitWallet,
                creditUser,
                creditWallet,
                transfer,
                transferFee));
    logService.debug(requestId, "L", "createProviderTransfer", "startedAsync");

    // output
    logService.debug(requestId, "OUT", "result", result);

    // return and leave the provide transaction registration to be performed in background
    return result;
  }

  @Override
  public synchronized List<Transaction> createPayOut(
      String requestId,
      Account account,
      Application application,
      User debitUser,
      Wallet debitWallet,
      BankAccount bankAccount,
      RequestAmount requestedAmount,
      RequestAmount requestedFees,
      String requestedFeeModel,
      String customTag,
      String bankWireRef) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "bankAccount", bankAccount.getId());
    logService.debug(requestId, "IN", "requestedAmount", requestedAmount.getValue());
    logService.debug(requestId, "IN", "requestedFees", requestedFees);
    logService.debug(requestId, "IN", "requestedFeeModel", requestedFeeModel);
    logService.debug(requestId, "IN", "bankWireRef", bankWireRef);

    // Initialise final variables to be used in async call to provider
    final Transfer payOutFee;
    final PayOut payOut;
    final Wallet accountWallet;

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // create fee amount object
    Amount feesAmount = new Amount();
    feesAmount.setCurrency(CurrencyISO.valueOf(requestedFees.getCurrency()));
    feesAmount.setValue(requestedFees.getValue());

    // decide processing mode based on fee model and value
    if (feesAmount.getValue() > 0) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // transaction value is based on total minus fee. Fee are extracted from pay out amount
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));

      // based on fee model we calculate the actual pay out amount
      if ((requestedFeeModel == null) || requestedFeeModel.equals(FeeModel.INCLUDED.toString())) {
        // fee is included in transfer amount
        transferAmount.setValue(requestedAmount.getValue() - feesAmount.getValue());
      } else {
        // fee is not included in transfer amount
        transferAmount.setValue(requestedAmount.getValue());
      }

      // register main transaction
      payOut =
          createPayOutTransaction(
              requestId,
              account,
              application,
              debitWallet,
              transferAmount,
              customTag,
              PaymentType.BANK_WIRE,
              bankAccount.getId().toString(),
              bankWireRef);
      logService.debug(requestId, "L", "createPayOutTransaction", payOut.getId());

      // set credit side
      accountWallet =
          accountSettingsService.getAccountWallet(requestId, account, feesAmount.getCurrency());

      // register fee transaction
      payOutFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              debitWallet,
              accountWallet,
              feesAmount,
              customTag,
              TransactionType.PAYOUT_FEE,
              TransactionNature.REGULAR,
              payOut.getId().toString(),
              true);
      logService.debug(requestId, "L", "createPayOutTransaction", payOutFee.getId());

      // update relations
      updatePayOutRelation(requestId, account, application, payOut, payOutFee.getId());
      logService.debug(requestId, "L", "updatePayOutRelation", "updated");

      // add main transaction to response list
      result.add(payOut);

      // add fee transaction to response list
      result.add(payOutFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      payOutFee = null;

      // if we have no fees or fee model is NOT_INCLUDED
      Amount transferAmount = new Amount();
      transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));
      transferAmount.setValue(requestedAmount.getValue());

      // register main transaction
      payOut =
          createPayOutTransaction(
              requestId,
              account,
              application,
              debitWallet,
              transferAmount,
              customTag,
              PaymentType.BANK_WIRE,
              bankAccount.getId().toString(),
              bankWireRef);
      logService.debug(requestId, "L", "createPayOutTransaction", payOut.getId());

      // add main transaction to response list
      result.add(payOut);
    }

    // instantiate flow step 2 - call provider to register transaction
    CompletableFuture.supplyAsync(
        () ->
            createProviderPayOut(
                requestId,
                account,
                application,
                bankAccount,
                debitUser,
                debitWallet,
                payOut,
                payOutFee));
    logService.debug(requestId, "L", "createProviderPayOut", "startedAsync");

    // output
    logService.debug(requestId, "OUT", "result", result);
    return result;
  }

  @Override
  public synchronized List<Transaction> createTransferRefund(
      String requestId,
      Account account,
      Application application,
      User originalDebitUser,
      Wallet originalDebitWallet,
      User originalCreditUser,
      Wallet originalCreditWallet,
      Transfer originalTransaction,
      String customTag) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "originalDebitUser", originalDebitUser.getId());
    logService.debug(requestId, "IN", "originalDebitWallet", originalDebitWallet.getId());
    logService.debug(requestId, "IN", "originalCreditUser", originalCreditUser.getId());
    logService.debug(requestId, "IN", "originalCreditWallet", originalCreditWallet.getId());
    logService.debug(requestId, "IN", "originalTransaction", originalTransaction.getId());
    logService.debug(requestId, "IN", "customTag", customTag);

    // Initialise final variables to be used in async call to provider
    final Transfer refundFee;
    final Refund refund;
    final Transfer originalFeeTransaction;
    final Wallet accountWallet;

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // check if we have a fee transaction involved as well and there is a configuration to allow
    // refund of fee
    if ((originalTransaction.getRelatedTransactionId() != null)
        && (!originalTransaction.getRelatedTransactionId().equals(""))) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // register refund transaction
      refund =
          createNoFxRefundTransaction(
              requestId,
              account,
              application,
              originalDebitWallet,
              originalCreditWallet,
              originalTransaction,
              customTag);
      logService.debug(requestId, "L", "createNoFxRefundTransaction", refund.getId());

      // find original fee transaction
      originalFeeTransaction =
          transferService.getTransfer(
              requestId, account, originalTransaction.getRelatedTransactionId());

      // find the account wallet for fees
      accountWallet =
          accountSettingsService.getAccountWallet(
              requestId, account, originalFeeTransaction.getAmount().getCurrency());

      // register refund fee transaction
      refundFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              accountWallet,
              originalDebitWallet,
              originalFeeTransaction.getAmount(),
              customTag,
              TransactionType.TRANSFER_FEE,
              TransactionNature.REFUND,
              refund.getId().toString(),
              true);
      logService.debug(requestId, "L", "createNoFxRefundTransaction", refundFee.getId());

      // update relations
      updateRefundRelation(requestId, account, application, refund, refundFee.getId());
      logService.debug(requestId, "L", "updateRefundRelation", "updated");

      // add refund transaction to response list
      result.add(refund);

      // add refund fee transaction to response list
      result.add(refundFee);
    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      // final variables do require a initializations
      accountWallet = null;
      refundFee = null;
      originalFeeTransaction = null;

      // if we have no fees or fee model is NOT_INCLUDED

      // register refund transaction
      refund =
          createNoFxRefundTransaction(
              requestId,
              account,
              application,
              originalDebitWallet,
              originalCreditWallet,
              originalTransaction,
              customTag);
      logService.debug(requestId, "L", "createNoFxRefundTransaction", refund.getId());

      // add refund transaction to response list
      result.add(refund);
    }

    // instantiate flow step 2 - call provider to register transaction
    CompletableFuture.supplyAsync(
        () ->
            createProviderTransferRefund(
                requestId,
                account,
                application,
                originalCreditWallet,
                originalDebitWallet,
                originalDebitUser,
                accountWallet,
                refund,
                refundFee,
                originalTransaction,
                originalFeeTransaction));
    logService.debug(requestId, "L", "createProviderTransferRefund", "startedAsync");

    // output
    logService.debug(requestId, "OUT", "result", result);
    return result;
  }

  @Override
  public synchronized List<Transaction> createPayInRefund(
      String requestId,
      Account account,
      Application application,
      RequestRefundPayIn formRefundPayIn,
      PayIn originalPayIn,
      Wallet originalCreditWallet,
      User originaCreditUser) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "originalPayIn", Json.toJson(originalPayIn).toString());
    logService.debug(requestId, "IN", "formRefundPayIn", Json.toJson(formRefundPayIn).toString());
    logService.debug(
        requestId, "IN", "originalCreditWallet", Json.toJson(originalCreditWallet).toString());

    // initialise final variables to be used in async call to provider
    final Transfer refundFee;
    final Refund refund;
    final Transfer originalFeeTransaction;
    final Wallet accountWallet;

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // check if we have a fee transaction involved as well and there is a configuration to allow
    // refund of fee
    if ((originalPayIn.getRelatedTransactionId() != null)
        && (!originalPayIn.getRelatedTransactionId().equals(""))) {
      logService.debug(requestId, "L", "fees", "AVAILABLE");

      // register refund transaction
      refund =
          createNoFxRefundPayIn(
              requestId,
              account,
              application,
              formRefundPayIn,
              originalPayIn,
              originalCreditWallet);
      logService.debug(requestId, "L", "refund", Json.toJson(refund).toString());

      // find original fee transaction
      originalFeeTransaction =
          transferService.getTransfer(requestId, account, originalPayIn.getRelatedTransactionId());
      logService.debug(
          requestId, "L", "originalFeeTransaction", Json.toJson(originalFeeTransaction).toString());

      // find the account wallet for fees
      accountWallet =
          accountSettingsService.getAccountWallet(
              requestId, account, originalFeeTransaction.getAmount().getCurrency());
      logService.debug(requestId, "L", "accountWallet", Json.toJson(accountWallet).toString());

      // register refund fee transaction
      refundFee =
          createNoFxTransfer(
              requestId,
              account,
              application,
              originalCreditWallet,
              accountWallet,
              originalFeeTransaction.getAmount(),
              originalPayIn.getCustomTag(),
              TransactionType.TRANSFER_FEE,
              TransactionNature.REFUND,
              refund.getId().toString(),
              true);
      logService.debug(requestId, "L", "refundFee", Json.toJson(refundFee).toString());

      // update relations
      updateRefundRelation(requestId, account, application, refund, refundFee.getId());
      logService.debug(requestId, "L", "updateRefundRelation", "updated");

      // add refund transaction to response list
      result.add(refund);

      // add refund fee transaction to response list
      result.add(refundFee);

    } else {
      logService.debug(requestId, "L", "fees", "NOT AVAILABLE");

      // final variables do require a initializations
      refundFee = null;
      originalFeeTransaction = null;
      accountWallet = null;

      // we have no fees or fee model is NOT_INCLUDED
      refund =
          createNoFxRefundPayIn(
              requestId,
              account,
              application,
              formRefundPayIn,
              originalPayIn,
              originalCreditWallet);
      logService.debug(requestId, "L", "createNoFxRefundPayIn", Json.toJson(refund).toString());

      // add refund transaction to response list
      result.add(refund);
    }

    // instantiate flow step 2 - call provider to register transaction
    CompletableFuture.supplyAsync(
        () ->
            createProviderPayInRefund(
                requestId,
                account,
                application,
                formRefundPayIn,
                originalPayIn,
                originalCreditWallet,
                originaCreditUser,
                refund,
                refundFee));

    logService.debug(requestId, "OUT", "result", Json.toJson(result).toString());
    return result;
  }

  @Override
  public synchronized void closePayIn(
      String requestId,
      Account account,
      Application application,
      PayIn payIn,
      Transfer payInFee,
      Wallet creditWallet,
      Wallet accountWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "payIn", payIn.getId());
    logService.debug(requestId, "IN", "payInFee", payInFee);
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "accountWallet", accountWallet);

    ProviderResponse providerResponse =
        getProviderPayInStatus(requestId, account, application, payIn.getProviderId());

    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // get transaction status
      TransactionStatus transactionStatus =
          (TransactionStatus) providerResponse.getProviderData("payInStatus");
      logService.debug(
          requestId, "L", "payInStatus", providerResponse.getProviderData("payInStatus"));

      // if transaction is complete OK
      if (transactionStatus.equals(TransactionStatus.SUCCEEDED)) {
        // accept pay in
        acceptPayIn(
            requestId,
            account,
            application,
            payIn,
            creditWallet,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString(),
            providerResponse.getProviderData("securityInfo").toString());
        logService.debug(requestId, "L", "acceptPayIn", "accepted");

        // if fees are applied
        if (payInFee != null) {
          // reject fee transaction - no more check for transaction existence
          acceptPayInFee(requestId, account, application, payInFee, creditWallet, accountWallet);
          logService.debug(requestId, "L", "acceptPayInFee", "accepted");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.PAYIN_NORMAL_SUCCEEDED,
            payIn.getId());
      }

      if (transactionStatus.equals(TransactionStatus.FAILED)) {
        // reject pay in
        rejectPayIn(
            requestId,
            account,
            application,
            payIn,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString());
        logService.debug(requestId, "L", "rejectPayIn", "rejected");

        // if fees are applied
        if (payInFee != null) {
          // reject fee transaction - no more check for transaction existence
          rejectTransfer(
              requestId,
              account,
              application,
              payInFee,
              creditWallet,
              providerResponse.getProviderData("resultCode").toString(),
              providerResponse.getProviderData("resultMessage").toString(),
              false);
          logService.debug(requestId, "L", "rejectTransfer", "rejected");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId, account, application, NotificationType.PAYIN_NORMAL_FAILED, payIn.getId());
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String payInProviderId) {

    // get financial provider
    ProviderService providerService = providerFactory.getProvider(account, ProviderOperation.PAYIN);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify transaction to provider
    ProviderResponse providerResponse =
        providerService.getProviderPayInStatus(requestId, account, application, payInProviderId);
    logService.debug(requestId, "L", "providerOperationStatus", Json.toJson(providerResponse));

    return providerResponse;
  }

  @Override
  public synchronized void closeTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      Transfer transferFee,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet,
      Wallet accountWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "transfer", transfer.getId());
    logService.debug(requestId, "IN", "transferFee", transferFee);
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "creditUser", creditUser.getId());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "accountWallet", accountWallet);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.TRANSFER);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify transaction to provider
    ProviderResponse providerResponse =
        providerService.getProviderTransferStatus(requestId, transfer.getProviderId());
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // get transaction status
      TransactionStatus transferStatus =
          (TransactionStatus) providerResponse.getProviderData("transferStatus");
      logService.debug(
          requestId, "L", "transferStatus", providerResponse.getProviderData("transferStatus"));

      // if transaction is complete OK
      if (transferStatus.equals(TransactionStatus.SUCCEEDED)) {
        // accept transaction
        acceptTransfer(requestId, account, application, transfer, debitWallet, creditWallet, true);
        logService.debug(requestId, "L", "acceptTransfer", "accepted");

        // if fees are applied
        if (transferFee != null) {
          // accept fee transaction as well
          acceptTransfer(
              requestId, account, application, transferFee, debitWallet, accountWallet, true);
          logService.debug(requestId, "L", "acceptTransfer", "accepted");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.TRANSFER_NORMAL_SUCCEEDED,
            transfer.getId());
      }

      if (transferStatus.equals(TransactionStatus.FAILED)) {

        // reject transaction
        rejectTransfer(
            requestId,
            account,
            application,
            transfer,
            debitWallet,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString(),
            true);
        logService.debug(requestId, "L", "rejectTransfer", "rejected");

        // if fees are applied
        if (transferFee != null) {
          // reject fee transaction - no more check for transaction existence
          rejectTransfer(
              requestId,
              account,
              application,
              transferFee,
              debitWallet,
              providerResponse.getProviderData("resultCode").toString(),
              providerResponse.getProviderData("resultMessage").toString(),
              true);
          logService.debug(requestId, "L", "rejectTransfer", "rejected");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.TRANSFER_NORMAL_FAILED,
            transfer.getId());
      }
    }
    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public void registerSettlement(
      String requestId, Account account, Application application, String settlementId) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "settlementId", settlementId);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.TRANSFER);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify settlement to provider
    ProviderResponse providerResponse =
        providerService.getProviderSettlement(requestId, settlementId);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // if pay out successfully retrieved from MangoPay and has the right status
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      if (providerResponse.getProviderData("Status") != null
          && providerResponse.getProviderData("Status").equals(TransactionStatus.SUCCEEDED)) {
        logService.debug(requestId, "L", "start", "process settlement");

        // find user wallet
        Wallet userWallet =
            walletService.getWalletByProviderId(
                requestId, account, providerResponse.getProviderData("DebitedWalletId").toString());
        logService.debug(requestId, "L", "userWallet", userWallet.getId());

        // get account wallet with the same currency as the transfer fee
        Wallet accountWallet =
            accountSettingsService.getAccountWallet(
                requestId,
                account,
                CurrencyISO.valueOf(
                    providerResponse.getProviderData("DebitedFundsCurrency").toString()));
        logService.debug(requestId, "L", "accountWallet", accountWallet.getId());

        // init some fee id
        String feeId = "";

        // check for fee
        if (providerResponse.getProviderData("FeesAmount") != null
            && !providerResponse.getProviderData("FeesAmount").toString().equals("")
            && !providerResponse.getProviderData("FeesAmount").toString().equals("0")) {
          // assign some fee id
          feeId = new ObjectId().toString();
          logService.debug(requestId, "L", "feeId", feeId);
        }

        Transfer settlement = new Transfer();
        settlement.setId(new ObjectId().toString());
        settlement.setProviderId(providerResponse.getProviderData("Id").toString());
        settlement.setCustomTag(
            (providerResponse.getProviderData("Tag") != null
                ? providerResponse.getProviderData("Tag").toString()
                : ""));
        settlement.setDebitedUserId(userWallet.getUserId());
        settlement.setDebitedWalletId(userWallet.getId());
        settlement.setCreditedUserId(accountWallet.getUserId());
        settlement.setCreditedWalletId(accountWallet.getId());

        // DebitedFundsAmount - we put the entire transaction which appear on the record
        Amount amount = new Amount();
        amount.setCurrency(
            CurrencyISO.valueOf(
                providerResponse.getProviderData("CreditedFundsCurrency").toString()));
        amount.setValue(Math.abs((int) providerResponse.getProviderData("CreditedFundsAmount")));
        settlement.setAmount(amount);
        logService.debug(requestId, "L", "amount", amount.getValue());

        settlement.setStatus((TransactionStatus) providerResponse.getProviderData("Status"));
        settlement.setResultCode(providerResponse.getProviderData("ResultCode").toString());
        settlement.setResultMessage(providerResponse.getProviderData("ResultMessage").toString());

        if (providerResponse.getProviderData("ExecutionDate") != null) {
          settlement.setExecutionDate(
              Long.valueOf(providerResponse.getProviderData("ExecutionDate").toString()));
        }

        settlement.setType(TransactionType.TRANSFER);
        settlement.setNature(TransactionNature.SETTLEMENT);

        // check for fee again
        if (!feeId.equals("")) {
          // we have fee
          settlement.setRelatedTransactionId(feeId);
        }

        // creation date
        settlement.setCreatedAt(
            Long.valueOf(providerResponse.getProviderData("CreationDate").toString()));

        logService.debug(requestId, "L", "transferService", "prepare save");

        // save withdraw refund record
        transferService.saveTransfer(requestId, account, settlement);
        logService.debug(requestId, "L", "settlement", settlement.getId());

        // update user balances as well
        logService.debug(
            requestId, "L", "userWallet balance before", userWallet.getBalance().getValue());
        updateWalletBalance(requestId, account, userWallet, -amount.getValue());
        logService.debug(
            requestId, "L", "userWallet balance after", userWallet.getBalance().getValue());

        // update user balances as well
        logService.debug(
            requestId, "L", "accountWallet balance before", accountWallet.getBalance().getValue());
        updateWalletBalance(requestId, account, accountWallet, +amount.getValue());
        logService.debug(
            requestId, "L", "accountWallet balance after", accountWallet.getBalance().getValue());

        logService.debug(requestId, "L", "settlement", "completed and balance updated");

        // process fee
        if (!feeId.equals("")) {
          // process fee as well
          Transfer settlementFee = new Transfer();
          settlementFee.setId(feeId);
          settlementFee.setProviderId(providerResponse.getProviderData("Id").toString());
          settlementFee.setCustomTag(
              (providerResponse.getProviderData("Tag") != null
                  ? providerResponse.getProviderData("Tag").toString()
                  : ""));
          settlementFee.setDebitedUserId(accountWallet.getUserId());
          settlementFee.setDebitedWalletId(accountWallet.getId());
          settlementFee.setCreditedUserId(userWallet.getUserId());
          settlementFee.setCreditedWalletId(userWallet.getId());

          // DebitedFundsAmount
          Amount amountFee = new Amount();
          amountFee.setCurrency(
              CurrencyISO.valueOf(providerResponse.getProviderData("FeesCurrency").toString()));
          amountFee.setValue(
              Math.abs(Integer.valueOf((int) providerResponse.getProviderData("FeesAmount"))));
          settlementFee.setAmount(amountFee);

          settlementFee.setStatus((TransactionStatus) providerResponse.getProviderData("Status"));
          settlementFee.setResultCode(providerResponse.getProviderData("ResultCode").toString());
          settlementFee.setResultMessage(
              providerResponse.getProviderData("ResultMessage").toString());

          if (providerResponse.getProviderData("ExecutionDate") != null) {
            settlementFee.setExecutionDate(
                Long.valueOf(providerResponse.getProviderData("ExecutionDate").toString()));
          }

          settlementFee.setType(TransactionType.TRANSFER_FEE);
          settlementFee.setNature(TransactionNature.SETTLEMENT);

          settlementFee.setRelatedTransactionId(settlement.getId());
          settlementFee.setCreatedAt(
              Long.valueOf(providerResponse.getProviderData("CreationDate").toString()));

          transferService.saveTransfer(requestId, account, settlementFee);

          // update balance for user wallet - get balance
          updateWalletBalance(requestId, account, userWallet, amountFee.getValue());

          // update balance for account wallet - add balance
          updateWalletBalance(requestId, account, accountWallet, -amountFee.getValue());
        }

        logService.debug(requestId, "L", "settlement", "start send hook");

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.TRANSFER_SETTLEMENT_SUCCEEDED,
            settlement.getId());
      } else {
        logService.error(requestId, "L", "error", "unacceptedStatus");
        logService.error(requestId, "L", "status", providerResponse.getProviderData("Status"));
        logService.error(
            requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

        GenericRestException gre = new GenericRestException();
        gre.setResponseErrors(providerResponse.getProviderErrors());
        throw gre;
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public void registerWithdrawRefund(
      String requestId, Account account, Application application, String providerRefundId) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "providerRefundId", providerRefundId);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REFUND);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify pay out to provider
    ProviderResponse providerResponse =
        providerService.getProviderWithdrawRefund(requestId, providerRefundId);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // if pay out successfully retrieved from MangoPay and has the right status
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      if (providerResponse.getProviderData("Status") != null
          && providerResponse.getProviderData("Status").equals(TransactionStatus.SUCCEEDED)) {
        logService.debug(requestId, "L", "start", "process transaction");

        // find user wallet
        Wallet userWallet =
            walletService.getWalletByProviderId(
                requestId,
                account,
                providerResponse.getProviderData("CreditedWalletId").toString());
        logService.debug(requestId, "L", "userWallet", userWallet.getId());

        // init some fee id
        String feeId = "";

        // check for fee
        if (providerResponse.getProviderData("FeesAmount") != null
            && !providerResponse.getProviderData("FeesAmount").toString().equals("")
            && !providerResponse.getProviderData("FeesAmount").toString().equals("0")) {
          // assign some fee id
          feeId = new ObjectId().toString();
          logService.debug(requestId, "L", "feeId", feeId);
        }

        Refund withdrawRefund = new Refund();
        withdrawRefund.setId(new ObjectId().toString());
        withdrawRefund.setProviderId(providerResponse.getProviderData("Id").toString());
        withdrawRefund.setCustomTag(
            (providerResponse.getProviderData("Tag") != null
                ? providerResponse.getProviderData("Tag").toString()
                : ""));
        withdrawRefund.setCreditedUserId(userWallet.getUserId());
        withdrawRefund.setCreditedWalletId(userWallet.getId());

        // DebitedFundsAmount - we put the entire transaction which appear on the record
        Amount amount = new Amount();
        amount.setCurrency(
            CurrencyISO.valueOf(
                providerResponse.getProviderData("DebitedFundsCurrency").toString()));
        amount.setValue(Math.abs((int) providerResponse.getProviderData("DebitedFundsAmount")));
        withdrawRefund.setAmount(amount);

        withdrawRefund.setStatus((TransactionStatus) providerResponse.getProviderData("Status"));
        withdrawRefund.setResultCode(providerResponse.getProviderData("ResultCode").toString());
        withdrawRefund.setResultMessage(
            providerResponse.getProviderData("ResultMessage").toString());

        if (providerResponse.getProviderData("ExecutionDate") != null) {
          withdrawRefund.setExecutionDate(
              Long.valueOf(providerResponse.getProviderData("ExecutionDate").toString()));
        }

        withdrawRefund.setType(TransactionType.PAYIN);
        withdrawRefund.setNature(TransactionNature.REFUND);

        // check for fee again
        if (!feeId.equals("")) {
          // we have fee
          withdrawRefund.setRelatedTransactionId(feeId);
        }

        // set initial transaction data
        withdrawRefund.setInitialTransactionId(
            transferService
                .getTransferByProviderId(
                    requestId,
                    account,
                    providerResponse.getProviderData("InitialTransactionId").toString())
                .getId());
        withdrawRefund.setInitialTransactionType(
            TransactionType.valueOf(
                providerResponse.getProviderData("InitialTransactionType").toString()));

        // creation date
        withdrawRefund.setCreatedAt(
            Long.valueOf(providerResponse.getProviderData("CreationDate").toString()));

        logService.debug(requestId, "L", "payInService", "prepare save");

        // save withdraw refund record
        refundService.saveRefund(requestId, account, withdrawRefund);
        logService.debug(requestId, "L", "withdrawRefund", withdrawRefund.getId());

        // update user balances as well
        updateWalletBalance(requestId, account, userWallet, amount.getValue());
        logService.debug(requestId, "L", "withdrawRefund", "completed and balance updated");

        // process fee
        if (!feeId.equals("")) {
          // get account wallet with the same currency as the transfer fee
          Wallet accountWallet =
              accountSettingsService.getAccountWallet(
                  requestId,
                  account,
                  CurrencyISO.valueOf(providerResponse.getProviderData("FeesCurrency").toString()));

          // process fee as well
          Transfer withdrawRefundFee = new Transfer();
          withdrawRefundFee.setId(feeId);
          withdrawRefundFee.setProviderId(providerResponse.getProviderData("Id").toString());
          withdrawRefundFee.setCustomTag(
              (providerResponse.getProviderData("Tag") != null
                  ? providerResponse.getProviderData("Tag").toString()
                  : ""));
          withdrawRefundFee.setDebitedUserId(accountWallet.getUserId());
          withdrawRefundFee.setDebitedWalletId(accountWallet.getId());
          withdrawRefundFee.setCreditedUserId(userWallet.getUserId());
          withdrawRefundFee.setCreditedWalletId(userWallet.getId());

          // DebitedFundsAmount
          Amount amountFee = new Amount();
          amountFee.setCurrency(
              CurrencyISO.valueOf(providerResponse.getProviderData("FeesCurrency").toString()));
          amountFee.setValue(
              Math.abs(Integer.valueOf((int) providerResponse.getProviderData("FeesAmount"))));
          withdrawRefundFee.setAmount(amountFee);

          withdrawRefundFee.setStatus(
              (TransactionStatus) providerResponse.getProviderData("Status"));
          withdrawRefundFee.setResultCode(
              providerResponse.getProviderData("ResultCode").toString());
          withdrawRefundFee.setResultMessage(
              providerResponse.getProviderData("ResultMessage").toString());

          if (providerResponse.getProviderData("ExecutionDate") != null) {
            withdrawRefundFee.setExecutionDate(
                Long.valueOf(providerResponse.getProviderData("ExecutionDate").toString()));
          }

          withdrawRefundFee.setType(TransactionType.PAYIN_FEE);
          withdrawRefundFee.setNature(TransactionNature.REFUND);

          withdrawRefundFee.setRelatedTransactionId(withdrawRefund.getId());
          withdrawRefundFee.setCreatedAt(
              Long.valueOf(providerResponse.getProviderData("CreationDate").toString()));

          transferService.saveTransfer(requestId, account, withdrawRefundFee);

          // update balance for user wallet - get balance
          updateWalletBalance(requestId, account, userWallet, amountFee.getValue());

          // update balance for account wallet - add balance
          updateWalletBalance(requestId, account, accountWallet, -amountFee.getValue());
        }

        logService.debug(requestId, "L", "withdrawRefund", "start send hook");

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.PAYOUT_REFUND_SUCCEEDED,
            withdrawRefund.getId());
      } else {
        logService.error(requestId, "L", "error", "unacceptedStatus");
        logService.error(requestId, "L", "status", providerResponse.getProviderData("Status"));
        logService.error(
            requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

        GenericRestException gre = new GenericRestException();
        gre.setResponseErrors(providerResponse.getProviderErrors());
        throw gre;
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public synchronized void closeDepositRefund(
      String requestId,
      Account account,
      Application application,
      String providerRefundId,
      Wallet userWallet,
      Refund payInRefund,
      Transfer transferFee,
      Wallet accountWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "providerRefundId", providerRefundId);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REFUND);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify pay out to provider
    ProviderResponse providerResponse =
        providerService.getProviderDepositRefund(requestId, providerRefundId);
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    // if pay out successfully retrieved from MangoPay and has the right status
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {

      TransactionStatus payInRefundStatus =
          (TransactionStatus) providerResponse.getProviderData("Status");

      if (payInRefundStatus.equals(TransactionStatus.SUCCEEDED)) {

        logService.debug(requestId, "L", "start", "process SUCCEEDED deposit refund");
        acceptPayInRefund(requestId, account, userWallet, payInRefund);

        // if fees are applied
        if (transferFee != null) {
          // accept fee transaction as well
          acceptTransfer(
              requestId, account, application, transferFee, userWallet, accountWallet, true);
          logService.debug(requestId, "L", "acceptTransferFee", "accepted");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.PAYIN_REFUND_SUCCEEDED,
            payInRefund.getId());
      }

      if (payInRefundStatus.equals(TransactionStatus.FAILED)) {
        logService.debug(requestId, "L", "start", "process FAILED deposit refund");

        rejectPayInRefund(requestId, account, userWallet, payInRefund);
        logService.debug(requestId, "L", "rejectRefund", "rejected");

        // if fees are applied
        if (transferFee != null) {
          // reject fee transaction - no more check for transaction existence
          rejectTransfer(
              requestId,
              account,
              application,
              transferFee,
              accountWallet,
              providerResponse.getProviderData("resultCode").toString(),
              providerResponse.getProviderData("resultMessage").toString(),
              true);
          logService.debug(requestId, "L", "rejectTransfer", "rejected");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.PAYIN_REFUND_FAILED,
            payInRefund.getId());
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public synchronized void closePayOut(
      String requestId,
      Account account,
      Application application,
      PayOut payOut,
      Transfer payOutFee,
      User debitUser,
      Wallet debitWallet,
      Wallet accountWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "payOut", payOut.getId());
    logService.debug(requestId, "IN", "payOutFee", payOutFee);
    logService.debug(requestId, "IN", "debitUser", debitUser);
    logService.debug(requestId, "IN", "debitWallet", debitWallet);
    logService.debug(requestId, "IN", "accountWallet", accountWallet);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.PAYOUT);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify pay out to provider
    ProviderResponse providerResponse =
        providerService.getProviderPayOutStatus(requestId, payOut.getProviderId());
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // get transaction status
      TransactionStatus payOutStatus =
          (TransactionStatus) providerResponse.getProviderData("payOutStatus");
      logService.debug(
          requestId, "L", "payOutStatus", providerResponse.getProviderData("payOutStatus"));

      // if transaction is complete OK
      if (payOutStatus.equals(TransactionStatus.SUCCEEDED)) {
        // accept pay in
        acceptPayOut(
            requestId,
            account,
            application,
            payOut,
            debitWallet,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString());
        logService.debug(requestId, "L", "acceptPayOut", "accepted");

        // if fees are applied
        if (payOutFee != null) {
          // reject fee transaction - no more check for transaction existence
          acceptTransfer(
              requestId, account, application, payOutFee, debitWallet, accountWallet, true);
          logService.debug(requestId, "L", "acceptTransfer", "accepted");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.PAYOUT_NORMAL_SUCCEEDED,
            payOut.getId());
      }

      if (payOutStatus.equals(TransactionStatus.FAILED)) {
        // reject pay out
        rejectPayOut(
            requestId,
            account,
            application,
            payOut,
            debitWallet,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString());
        logService.debug(requestId, "L", "rejectPayOut", "rejected");

        // if fees are applied
        if (payOutFee != null) {
          // reject fee transaction - no more check for transaction existence
          rejectTransfer(
              requestId,
              account,
              application,
              payOutFee,
              debitWallet,
              providerResponse.getProviderData("resultCode").toString(),
              providerResponse.getProviderData("resultMessage").toString(),
              true);
          logService.debug(requestId, "L", "rejectTransfer", "rejected");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId, account, application, NotificationType.PAYOUT_NORMAL_FAILED, payOut.getId());
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public synchronized void closeRefund(
      String requestId,
      Account account,
      Application application,
      Refund refund,
      Transfer refundFee,
      User originalCreditUser,
      Wallet originalCreditWallet,
      User originalDebitUser,
      Wallet originalDebitWallet,
      Wallet accountWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "refund", refund.getId());
    logService.debug(requestId, "IN", "refundFee", refundFee);
    logService.debug(requestId, "IN", "originalCreditUser", originalCreditUser.getId());
    logService.debug(requestId, "IN", "originalCreditWallet", originalCreditWallet.getId());
    logService.debug(requestId, "IN", "originalDebitUser", originalDebitUser.getId());
    logService.debug(requestId, "IN", "originalDebitWallet", originalDebitWallet.getId());
    logService.debug(requestId, "IN", "accountWallet", accountWallet);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REFUND);
    logService.debug(requestId, "L", "provider", providerService.getClass().getSimpleName());

    // verify transaction to provider
    ProviderResponse providerResponse =
        providerService.getProviderTransferRefundStatus(requestId, refund.getProviderId());
    logService.debug(
        requestId, "L", "providerOperationStatus", providerResponse.getProviderOperationStatus());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // get transaction status
      TransactionStatus transferRefundStatus =
          (TransactionStatus) providerResponse.getProviderData("transferRefundStatus");
      logService.debug(
          requestId,
          "L",
          "transferRefundStatus",
          providerResponse.getProviderData("transferRefundStatus"));

      // if transaction is complete OK
      if (transferRefundStatus.equals(TransactionStatus.SUCCEEDED)) {
        // accept transaction
        acceptRefund(
            requestId,
            account,
            application,
            refund,
            originalCreditWallet,
            originalDebitWallet,
            true);
        logService.debug(requestId, "L", "acceptRefund", "accepted");

        // if fees are applied
        if (refundFee != null) {
          // accept fee transaction as well
          acceptTransfer(
              requestId, account, application, refundFee, accountWallet, originalDebitWallet, true);
          logService.debug(requestId, "L", "acceptTransfer", "accepted");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.TRANSFER_REFUND_SUCCEEDED,
            refund.getId());
      }

      if (transferRefundStatus.equals(TransactionStatus.FAILED)) {
        rejectRefund(
            requestId,
            account,
            application,
            refund,
            originalCreditWallet,
            providerResponse.getProviderData("resultCode").toString(),
            providerResponse.getProviderData("resultMessage").toString(),
            RefundReasonType.OTHER,
            providerResponse.getProviderData("refundReasonMessage").toString());
        logService.debug(requestId, "L", "rejectRefund", "rejected");

        // if fees are applied
        if (refundFee != null) {
          // reject fee transaction - no more check for transaction existence
          rejectTransfer(
              requestId,
              account,
              application,
              refundFee,
              accountWallet,
              providerResponse.getProviderData("resultCode").toString(),
              providerResponse.getProviderData("resultMessage").toString(),
              true);
          logService.debug(requestId, "L", "rejectTransfer", "rejected");
        }

        // notify client hook
        notificationService.notifyClient(
            requestId,
            account,
            application,
            NotificationType.TRANSFER_REFUND_FAILED,
            refund.getId());
      }
    }

    // check provider status for pay in
    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      logService.error(requestId, "L", "error", "providerError");
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(new Throwable()));

      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public List<Transaction> createFailedRefund(
      String requestId,
      Account account,
      Application application,
      Wallet originalDebitWallet,
      Wallet originalCreditWallet,
      Transfer originalTransaction,
      String customTag,
      String errorMessage) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "originalDebitWallet", originalDebitWallet.getId());
    logService.debug(requestId, "IN", "originalCreditWallet", originalCreditWallet.getId());
    logService.debug(requestId, "IN", "originalTransaction", originalTransaction.getId());
    logService.debug(requestId, "IN", "customTag", customTag);
    logService.debug(requestId, "IN", "errorMessage", errorMessage);

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // Initialise variables
    Refund refund = new Refund();

    // create amount object
    Amount refundAmount = new Amount();
    refundAmount.setCurrency(originalTransaction.getAmount().getCurrency());
    refundAmount.setValue(originalTransaction.getAmount().getValue());

    // set custom data
    refund.setCustomTag(customTag);

    // set debit side
    refund.setDebitedUserId(originalCreditWallet.getUserId());
    refund.setDebitedWalletId(originalCreditWallet.getId().toString());

    // set credit side
    refund.setCreditedUserId(originalDebitWallet.getUserId());
    refund.setCreditedWalletId(originalDebitWallet.getId().toString());

    // set amount
    refund.setAmount(refundAmount);

    // set transaction details
    refund.setType(TransactionType.TRANSFER);
    refund.setNature(TransactionNature.REFUND);
    refund.setStatus(TransactionStatus.FAILED);
    refund.setResultCode(applicationErrorService.getErrorCode(requestId, errorMessage));
    refund.setResultMessage(errorMessage);

    // set refund specific details
    refund.setInitialTransactionId(originalTransaction.getId().toString());
    refund.setInitialTransactionType(originalTransaction.getType());
    refund.setRefundReasonType(RefundReasonType.OTHER);
    refund.setRefusedReasonMessage("");

    // save transaction
    refundService.saveRefund(requestId, account, refund);
    logService.debug(requestId, "L", "dbservice", "saved");

    // return the transaction with status created for the client api call
    refund.setStatus(TransactionStatus.CREATED);
    refund.setResultCode("");
    refund.setResultMessage("");
    logService.debug(requestId, "L", "refund", "change response for client");

    // create response with single transaction
    result.add(refund);

    // notify client hook - hooks might reach the client earlier
    notificationService.notifyClient(
        requestId, account, application, NotificationType.TRANSFER_REFUND_CREATED, refund.getId());
    notificationService.notifyClient(
        requestId, account, application, NotificationType.TRANSFER_REFUND_FAILED, refund.getId());
    logService.debug(requestId, "L", "notifications", "startedAsyncNotifications");

    // output
    logService.debug(requestId, "OUT", "result", result);

    // return and leave the provide transaction registration to be performed in background
    return result;
  }

  @Override
  public List<Transaction> createFailedTransfer(
      String requestId,
      Account account,
      Application application,
      Wallet debitWallet,
      Wallet creditWallet,
      RequestAmount requestedAmount,
      String customTag,
      String errorMessage) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "requestedAmount", requestedAmount.getValue());
    logService.debug(requestId, "IN", "customTag", customTag);
    logService.debug(requestId, "IN", "errorMessage", errorMessage);

    // create result
    List<Transaction> result = new ArrayList<Transaction>();

    // Initialise variables
    Transfer transfer = new Transfer();

    // create amount object
    Amount transferAmount = new Amount();
    transferAmount.setCurrency(CurrencyISO.valueOf(requestedAmount.getCurrency()));
    transferAmount.setValue(requestedAmount.getValue());

    // set custom data
    transfer.setCustomTag(customTag);

    // set debit side
    transfer.setDebitedUserId(debitWallet.getUserId());
    transfer.setDebitedWalletId(debitWallet.getId().toString());

    // set credit side
    transfer.setCreditedUserId(creditWallet.getUserId());
    transfer.setCreditedWalletId(creditWallet.getId().toString());

    // set amount
    transfer.setAmount(transferAmount);

    // set transaction details
    transfer.setType(TransactionType.TRANSFER);
    transfer.setNature(TransactionNature.REGULAR);
    transfer.setStatus(TransactionStatus.FAILED);
    transfer.setResultCode(applicationErrorService.getErrorCode(requestId, errorMessage));
    transfer.setResultMessage(errorMessage);

    // set transaction Audit
    transfer.updateAudit(application.getApplicationStamp());

    // save transaction
    transferService.saveTransfer(requestId, account, transfer);
    logService.debug(requestId, "L", "dbservice", "saved");

    // return the transaction with status created for the client api call
    transfer.setStatus(TransactionStatus.CREATED);
    transfer.setResultCode("");
    transfer.setResultMessage("");
    logService.debug(requestId, "L", "transfer", "change response for client");

    // create response with single transaction
    result.add(transfer);

    // notify client hook - hooks might reach the client earlier
    notificationService.notifyClient(
        requestId,
        account,
        application,
        NotificationType.TRANSFER_NORMAL_CREATED,
        transfer.getId());
    notificationService.notifyClient(
        requestId, account, application, NotificationType.TRANSFER_NORMAL_FAILED, transfer.getId());
    logService.debug(requestId, "L", "notifications", "startedAsyncNotifications");

    // output
    logService.debug(requestId, "OUT", "result", result);

    // return and leave the provide transaction registration to be performed in background
    return result;
  }

  @Override
  public void updateBankCard(
      String requestId,
      Account account,
      Application application,
      User bankCardOwner,
      BankCard bankCard,
      RequestUpdateBankCard requestUpdateBankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCardOwner", bankCardOwner.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // name on card. This comes from php, where the names are truncated to proper lengths
    bankCard.setFirstName(requestUpdateBankCard.getFirstName());
    bankCard.setLastName(requestUpdateBankCard.getLastName());

    // card address
    String[] providerAddressLines = null;

    if (bankCard.isPhysicalCard() && bankCard.deliveryAddressIsValid()) {
      // PHYSICAL card, take delivery address from card application
      providerAddressLines = map2AddressLinesTo4(bankCard.getAddress1(), bankCard.getAddress2());
      bankCard.setCity(bankCard.getCity());
      bankCard.setCountyName(bankCard.getCountyName());
      bankCard.setZipCode(bankCard.getZipCode());
      bankCard.setCountryCode(bankCard.getCountryCode());
    } else {
      // VIRTUAL card or PHYSICAL card created before api7, take address from card owner
      Address address = bankCardOwner.getAddress();
      providerAddressLines =
          map2AddressLinesTo4(address.getAddressLine1(), address.getAddressLine2());
      bankCard.setCity(address.getCity());
      bankCard.setCountyName(address.getCounty());
      bankCard.setZipCode(address.getPostalCode());
      bankCard.setCountryCode(address.getCountry().toString());
    }

    bankCard.setAddress1(providerAddressLines[0]);
    bankCard.setAddress2(providerAddressLines[1]);
    bankCard.setAddress3(providerAddressLines[2]);
    bankCard.setAddress4(providerAddressLines[3]);

    // other owner details
    bankCard.setPhone(bankCardOwner.getMobilePhone());
    bankCard.setEmail(bankCardOwner.getEmail());
    bankCard.setDob(utilsService.timeStampToDate(bankCardOwner.getBirthDate(), "dd/MM/yyyy"));

    RequestCardUserInfo requestCardUserInfo = requestUpdateBankCard.getCardUserInfo();

    if (requestCardUserInfo != null) {
      CardUserInfo cardUserInfo = new CardUserInfo();
      cardUserInfo.setEmploymentStatus(
          CardUserInfoEmploymentStatus.valueOf(requestCardUserInfo.getEmploymentStatus()));
      cardUserInfo.setEstate(CardUserInfoEstate.valueOf(requestCardUserInfo.getEstate()));
      cardUserInfo.setMonthlyIncome(
          CardUserInfoMonthlyIncome.valueOf(requestCardUserInfo.getMonthlyIncome()));
      cardUserInfo.setOccupation(
          CardUserInfoOccupation.valueOf(requestCardUserInfo.getOccupation()));
      cardUserInfo.setPurpose(CardUserInfoPurpose.valueOf(requestCardUserInfo.getPurpose()));
      bankCard.setCardUserInfo(cardUserInfo);
    }

    try {
      // create bank card at provider server
      updateProviderBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "provider", "updated");

      // create bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "result", "OK");
  }

  @Override
  public BankCard createBankCard(
      String requestId,
      Account account,
      Application application,
      User bankCardOwner,
      RequestCreateBankCard requestCreateBankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCardOwner", bankCardOwner.getId());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.userId", requestCreateBankCard.getUserId());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.type", requestCreateBankCard.getType());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.customTag", requestCreateBankCard.getCustomTag());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.embossName", requestCreateBankCard.getEmbossName());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.firstName", requestCreateBankCard.getFirstName());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.lastName", requestCreateBankCard.getLastName());
    logService.debug(
        requestId, "IN", "requestCreateBankCard.address1", requestCreateBankCard.getAddress1());

    // create bank card
    BankCard newBankCard = new BankCard();

    // relation to LoLo
    newBankCard.setCustomTag(requestCreateBankCard.getCustomTag());
    newBankCard.setUserId(requestCreateBankCard.getUserId());

    // card type
    newBankCard.setType(BankCardType.valueOf(requestCreateBankCard.getType()));

    // card status
    newBankCard.setStatus(BankCardStatus.ISSUED);

    // card currency list
    List<String> cardCurrencies = new ArrayList<String>();
    cardCurrencies.add(ConfigFactory.load().getString("pfs.defaultCardCurrency"));
    newBankCard.setCurrencies(cardCurrencies);

    // name on card
    newBankCard.setFirstName(requestCreateBankCard.getFirstName());
    newBankCard.setLastName(requestCreateBankCard.getLastName());
    newBankCard.setEmbossName(requestCreateBankCard.getEmbossName());

    // card address
    String[] providerAddressLines = null;

    if (requestCreateBankCard.isPhysicalCard()) {
      // PHYSICAL card, take delivery address from request
      providerAddressLines =
          map2AddressLinesTo4(
              requestCreateBankCard.getAddress1(), requestCreateBankCard.getAddress2());
      newBankCard.setCity(requestCreateBankCard.getCity());
      newBankCard.setCountyName(requestCreateBankCard.getCountyName());
      newBankCard.setZipCode(requestCreateBankCard.getZipCode());
      newBankCard.setCountryCode(requestCreateBankCard.getCountryCode());

    } else {
      // VIRTUAL card, no delivery address is supplied, take address from owner
      Address address = bankCardOwner.getAddress();
      providerAddressLines =
          map2AddressLinesTo4(address.getAddressLine1(), address.getAddressLine2());
      newBankCard.setCity(address.getCity());
      newBankCard.setCountyName(address.getCounty());
      newBankCard.setZipCode(address.getPostalCode());
      newBankCard.setCountryCode(address.getCountry().toString());
    }

    newBankCard.setAddress1(providerAddressLines[0]);
    newBankCard.setAddress2(providerAddressLines[1]);
    newBankCard.setAddress3(providerAddressLines[2]);
    newBankCard.setAddress4(providerAddressLines[3]);

    // other owner details
    newBankCard.setPhone(bankCardOwner.getMobilePhone());
    newBankCard.setEmail(bankCardOwner.getEmail());
    newBankCard.setDob(utilsService.timeStampToDate(bankCardOwner.getBirthDate(), "dd/MM/yyyy"));

    RequestCardUserInfo requestCardUserInfo = requestCreateBankCard.getCardUserInfo();

    if (requestCardUserInfo != null) {
      CardUserInfo cardUserInfo = new CardUserInfo();
      cardUserInfo.setEmploymentStatus(
          CardUserInfoEmploymentStatus.valueOf(requestCardUserInfo.getEmploymentStatus()));
      cardUserInfo.setEstate(CardUserInfoEstate.valueOf(requestCardUserInfo.getEstate()));
      cardUserInfo.setMonthlyIncome(
          CardUserInfoMonthlyIncome.valueOf(requestCardUserInfo.getMonthlyIncome()));
      cardUserInfo.setOccupation(
          CardUserInfoOccupation.valueOf(requestCardUserInfo.getOccupation()));
      cardUserInfo.setPurpose(CardUserInfoPurpose.valueOf(requestCardUserInfo.getPurpose()));
      newBankCard.setCardUserInfo(cardUserInfo);
    }

    try {
      // create bank card at provider server
      createProviderBankCard(requestId, account, newBankCard);
      logService.debug(requestId, "L", "provider", "created");

      // create bank card audit
      newBankCard.updateAudit(application.getApplicationStamp());

      // save bank card
      bankCardService.saveBankCard(requestId, account, newBankCard);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // create card default wallet
    Amount newBankCardWalletAmount = new Amount();
    newBankCardWalletAmount.setCurrency(
        CurrencyISO.valueOf(ConfigFactory.load().getString("pfs.defaultCardCurrency")));
    newBankCardWalletAmount.setValue(0);

    BankCardWallet newBankCardWallet = new BankCardWallet();
    newBankCardWallet.setCardId(newBankCard.getId());
    newBankCardWallet.setAvailableBalance(newBankCardWalletAmount);
    newBankCardWallet.setLedgerBalance(newBankCardWalletAmount);
    newBankCardWallet.setCurrency(
        CurrencyISO.valueOf(ConfigFactory.load().getString("pfs.defaultCardCurrency")));

    // save default wallet
    bankCardWalletService.saveBankCardWallet(requestId, account, newBankCardWallet);
    logService.debug(requestId, "L", "dbservice", "saved");

    // output
    logService.debug(requestId, "OUT", "newBankCard", newBankCard);

    // response
    return newBankCard;
  }

  @Override
  public void transferToBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      RequestBankCardTransfer requestBankCardTransfer) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.cardId", requestBankCardTransfer.getCardId());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.currency", requestBankCardTransfer.getCurrency());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.amount", requestBankCardTransfer.getAmount());

    try {
      // deposit to card
      providerTransferToBankCard(
          requestId,
          account,
          bankCard,
          requestBankCardTransfer.getCurrency(),
          requestBankCardTransfer.getAmount());
      logService.debug(requestId, "L", "provider", "created");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void transferFromBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      RequestBankCardTransfer requestBankCardTransfer) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.cardId", requestBankCardTransfer.getCardId());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.currency", requestBankCardTransfer.getCurrency());
    logService.debug(
        requestId, "IN", "requestBankCardTransfer.amount", requestBankCardTransfer.getAmount());

    try {
      // deposit to card
      providerTransferFromBankCard(
          requestId,
          account,
          bankCard,
          requestBankCardTransfer.getCurrency(),
          requestBankCardTransfer.getAmount());
      logService.debug(requestId, "L", "provider", "created");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  @SuppressWarnings("unchecked")
  public BankCard replaceCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      String reason) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REPLACECARD);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.replaceProviderCard(requestId, bankCard, reason);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      logService.debug(
          requestId, "L", "cardHolderId", providerResponse.getProviderData("cardHolderId"));
      logService.debug(
          requestId, "L", "availableBalance", providerResponse.getProviderData("availableBalance"));
      logService.debug(
          requestId, "L", "ledgerBalance", providerResponse.getProviderData("ledgerBalance"));

      // get current card currencies
      PaginatedList paginatedList =
          bankCardWalletService.getBankCardWallets(requestId, account, bankCard.getId());

      // create a new card
      BankCard newBankCard = new BankCard();
      newBankCard.setProviderId(providerResponse.getProviderData("cardHolderId").toString());
      bankCardService.saveBankCard(requestId, account, newBankCard);
      logService.debug(requestId, "L", "newBankCard.id", newBankCard.getId());

      // get old card wallets
      List<BankCardWallet> oldBankCardWallets = (List<BankCardWallet>) paginatedList.getList();

      for (BankCardWallet oldBankCardWallet : oldBankCardWallets) {
        // create new card wallet
        Amount newBankCardWalletAmount = new Amount();
        newBankCardWalletAmount.setCurrency(oldBankCardWallet.getCurrency());
        newBankCardWalletAmount.setValue(0);

        BankCardWallet newBankCardWallet = new BankCardWallet();
        newBankCardWallet.setCardId(newBankCard.getId());
        newBankCardWallet.setAvailableBalance(newBankCardWalletAmount);
        newBankCardWallet.setLedgerBalance(newBankCardWalletAmount);
        newBankCardWallet.setCurrency(oldBankCardWallet.getCurrency());
        newBankCardWallet.updateAudit(application.getApplicationStamp());

        // save new card wallet
        bankCardWalletService.saveBankCardWallet(requestId, account, newBankCardWallet);
      }
      logService.debug(requestId, "L", "new card wallets created", newBankCard.getId());

      // make a get card to provider to update all details
      // getBankCard(requestId, account, application, newBankCard);

      // return refreshed new card
      return newBankCard;
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    return null;
  }

  @Override
  public void addBankCardCurrency(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      RequestAddBankCardCurrency requestAddBankCardCurrency) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(
        requestId,
        "IN",
        "requestAddBankCardCurrency.cardId",
        requestAddBankCardCurrency.getCardId());
    logService.debug(
        requestId,
        "IN",
        "requestAddBankCardCurrency.currency",
        requestAddBankCardCurrency.getCurrency());

    // create card default wallet
    Amount newBankCardWalletAmount = new Amount();
    newBankCardWalletAmount.setCurrency(
        CurrencyISO.valueOf(requestAddBankCardCurrency.getCurrency()));
    newBankCardWalletAmount.setValue(0);

    BankCardWallet newBankCardWallet = new BankCardWallet();
    newBankCardWallet.setCardId(bankCard.getId());
    newBankCardWallet.setAvailableBalance(newBankCardWalletAmount);
    newBankCardWallet.setLedgerBalance(newBankCardWalletAmount);
    newBankCardWallet.setCurrency(CurrencyISO.valueOf(requestAddBankCardCurrency.getCurrency()));

    try {
      // create bank card wallet at provider server
      addProviderBankCardCurrency(
          requestId, account, bankCard, requestAddBankCardCurrency.getCurrency());
      logService.debug(requestId, "L", "provider", "added");

      // create bank card wallet audit
      newBankCardWallet.updateAudit(application.getApplicationStamp());

      // save new wallet
      bankCardWalletService.saveBankCardWallet(requestId, account, newBankCardWallet);
      logService.debug(requestId, "L", "dbservice", "saved");

      // update bank card as well
      bankCard.getCurrencies().add(requestAddBankCardCurrency.getCurrency());

      // update bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card as well
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void changeStatusBankCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "oldStatus", oldStatus);
    logService.debug(requestId, "IN", "newStatus", newStatus);

    try {
      // change card status from provider server
      changeStatusProviderBankCard(requestId, account, bankCard, oldStatus, newStatus);
      logService.debug(requestId, "L", "provider", "changed");

      // set new status
      bankCard.setStatus(newStatus);

      // update bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card as well
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void lockUnlockCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "oldStatus", oldStatus);
    logService.debug(requestId, "IN", "newStatus", newStatus);

    try {
      // lock/unlock card status from provider server
      lockUnlockProviderBankCard(requestId, account, bankCard, oldStatus, newStatus);
      logService.debug(requestId, "L", "provider", "updated");

      // set new status
      bankCard.setStatus(newStatus);

      // update bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card as well
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void getBankCard(
      String requestId, Account account, Application application, BankCard bankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    try {
      // get bank card from provider server
      getProviderBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "provider", "retrieved");

      // create bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "updated");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public BankCard retrieveManuallyReissuedCard(
      String requestId,
      Account account,
      Application application,
      BankCard existingBankCard,
      String reissuedCardProviderId) {
    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "existingBankCard", existingBankCard.getId());

    logService.debug(requestId, "IN", "reissuedCardProviderId", reissuedCardProviderId);

    BankCard newBankCard = new BankCard();

    try {
      newBankCard.setProviderId(reissuedCardProviderId);
      // get bank card from provider server
      getProviderBankCard(requestId, account, newBankCard);
      logService.debug(requestId, "L", "provider", "retrieved");

      // phone must be the same, otherwise we wouldn't be able to associate the card to the user
      boolean cardBelongsToSameUser =
          StringUtils.equalsIgnoreCase(newBankCard.getPhone(), existingBankCard.getPhone());
      // TODO: uncomment these if you want to perform additional checks
      //              || (StringUtils.equalsIgnoreCase(
      //                      newBankCard.getFirstName(), existingBankCard.getFirstName())
      //                  && StringUtils.equalsIgnoreCase(
      //                      newBankCard.getLastName(), existingBankCard.getLastName()))
      //              || StringUtils.equalsIgnoreCase(
      //                  newBankCard.getAddress1(), existingBankCard.getAddress1())
      //              || StringUtils.equalsIgnoreCase(newBankCard.getEmail(),
      // existingBankCard.getEmail());

      if (!cardBelongsToSameUser) {
        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_RETRIEVE_MAN_REISSUED_CARD_NOT_SAME_PERSON);
        responseError.setErrorDescription(
            "Reissued card does not seem to belong to the same person");
        GenericRestException gre = new GenericRestException();
        gre.addResponseError(responseError);
        throw gre;
      }
      // create bank card audit
      newBankCard.updateAudit(application.getApplicationStamp());

      // save bank card

      // relation to Lolo
      newBankCard.setUserId(existingBankCard.getUserId());
      newBankCard.setCustomTag(existingBankCard.getCustomTag());

      bankCardService.saveBankCard(requestId, account, newBankCard);
      logService.debug(requestId, "L", "dbservice", "updated");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");

    return newBankCard;
  }

  @Override
  public void retrieveWalletForManuallyReissuedCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      String currencyCode) {
    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "existingBankCard", bankCard.getId());

    logService.debug(requestId, "IN", "currencyCode", currencyCode);

    try {
      // create card default wallet
      Amount newBankCardWalletAmount = new Amount();
      newBankCardWalletAmount.setCurrency(CurrencyISO.valueOf(currencyCode));
      newBankCardWalletAmount.setValue(0);

      BankCardWallet newBankCardWallet = new BankCardWallet();
      newBankCardWallet.setCardId(bankCard.getId());
      newBankCardWallet.setAvailableBalance(newBankCardWalletAmount);
      newBankCardWallet.setLedgerBalance(newBankCardWalletAmount);
      newBankCardWallet.setCurrency(CurrencyISO.valueOf(currencyCode));

      // create bank card audit
      newBankCardWallet.updateAudit(application.getApplicationStamp());

      // create new wallet
      bankCardWalletService.saveBankCardWallet(requestId, account, newBankCardWallet);

      // update new wallet with data from provider
      getProviderBankCardWallet(requestId, account, bankCard, newBankCardWallet);
      logService.debug(requestId, "L", "provider", "retrieved");

      // create bank card audit
      newBankCardWallet.updateAudit(application.getApplicationStamp());

      // save bank card wallet details
      bankCardWalletService.saveBankCardWallet(requestId, account, newBankCardWallet);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public String getBankCardNumber(
      String requestId, Account account, Application application, BankCard bankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // init provider card number
    String providerCardNo = "";

    try {
      // get bank card number from provider server
      providerCardNo = getProviderBankCardNumber(requestId, account, bankCard);
      logService.debug(requestId, "L", "provider", "retrieved");

      // update card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "updated");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "**** **** **** ****");
    return providerCardNo;
  }

  @Override
  public String getBankCardExpiryDate(String requestId, Account account, BankCard bankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    try {
      logService.debug(requestId, "L", "provider", "startSyncRetrieve");

      // get bank card expiry date from provider server
      return getProviderBankCardExpiryDate(requestId, account, bankCard);

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }
  }

  @Override
  public String getBankCardCVV(String requestId, Account account, BankCard bankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    try {
      logService.debug(requestId, "L", "provider", "startSyncRetrieve");

      // get bank card cvv from provider server
      return getProviderBankCardCvv(requestId, account, bankCard);

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }
  }

  @Override
  public void getBankCardWallet(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardWallet bankCardWallet) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "bankCardWallet", bankCardWallet.getId());

    try {
      // get bank card from provider server
      getProviderBankCardWallet(requestId, account, bankCard, bankCardWallet);
      logService.debug(requestId, "L", "provider", "retrieved");

      // create bank card audit
      bankCardWallet.updateAudit(application.getApplicationStamp());

      // save bank card wallet details
      bankCardWalletService.saveBankCardWallet(requestId, account, bankCardWallet);
      logService.debug(requestId, "L", "dbservice", "saved");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public List<BankCardTransaction> getBankCardWalletTransaction(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      Application application,
      long startDate,
      long endDate) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "bankCardWallet", bankCardWallet.getId());
    logService.debug(requestId, "IN", "startDate", Instant.ofEpochSecond(startDate));

    if (bankCard.getTransactionDates() != null) {
      if (bankCard.getTransactionDates().containsKey(bankCardWallet.getCurrency())) {
        logService.debug(
            requestId,
            "L",
            "bankCard START startDate",
            Instant.ofEpochSecond(
                bankCard.getTransactionDates().get(bankCardWallet.getCurrency()).getStartDate()));
        logService.debug(
            requestId,
            "L",
            "bankCard START endDate",
            Instant.ofEpochSecond(
                bankCard.getTransactionDates().get(bankCardWallet.getCurrency()).getEndDate()));
      }
    }

    // set start date timestamp to the beginning of the day
    startDate =
        LocalDate.now(Clock.fixed(Instant.ofEpochSecond(startDate), ZoneOffset.UTC))
            .atStartOfDay(ZoneOffset.UTC)
            .toEpochSecond();
    endDate =
        LocalDate.now(Clock.fixed(Instant.ofEpochSecond(endDate), ZoneOffset.UTC))
            .plusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .minusSeconds(1)
            .toEpochSecond();

    TransactionDate providerTransactionDate = new TransactionDate();
    try {
      logService.debug(requestId, "L", "provider", "startSyncRetrieve");

      // get dates for provider retrieve
      providerTransactionDate =
          bankCardTransactionsService.getDatesForProvider(
              requestId, account, bankCard, bankCardWallet, startDate, endDate);

      // get transactions from provider
      List<BankCardTransaction> retrievedTransactionsFromProvider =
          getProviderBankCardWalletTransaction(
              requestId, account, bankCard, providerTransactionDate);

      // save retrieved transactions from provider in database (except the ones from seven days
      // until endDate)
      bankCardTransactionsService.saveTransactionsWithOffset(
          requestId,
          account,
          retrievedTransactionsFromProvider,
          bankCard,
          bankCardWallet,
          providerTransactionDate);

      logService.debug(
          requestId,
          "L",
          "bankCard END startDate",
          Instant.ofEpochSecond(
              bankCard.getTransactionDates().get(bankCardWallet.getCurrency()).getStartDate()));
      logService.debug(
          requestId,
          "L",
          "bankCard END endDate",
          Instant.ofEpochSecond(
              bankCard.getTransactionDates().get(bankCardWallet.getCurrency()).getEndDate()));

      // update bankCard
      bankCardService.saveBankCard(requestId, account, bankCard);

      // get bank card transactions from database
      List<BankCardTransaction> returnBankCardTransactions = new ArrayList<BankCardTransaction>();
      returnBankCardTransactions =
          bankCardTransactionsService.getBankCardWalletTransaction(
              requestId,
              account,
              bankCard,
              bankCardWallet,
              startDate,
              endDate,
              retrievedTransactionsFromProvider);

      return returnBankCardTransactions;
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }
  }

  /**
   * Retrieve list of transactions for a wallet
   *
   * @param account
   * @param bankCard
   * @param bankCardWallet
   * @param startDate
   * @param endDate
   * @return
   */
  @SuppressWarnings("unchecked")
  private List<BankCardTransaction> getProviderBankCardWalletTransaction(
      String requestId, Account account, BankCard bankCard, TransactionDate transactionDate) {

    logService.debug(requestId, "L", "requestId", requestId);

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARDWALLETTRANSACTIONS);

    // get records from provider
    ProviderResponse providerResponse =
        providerService.getProviderBankCardWalletTransaction(requestId, bankCard, transactionDate);

    // init response
    List<BankCardTransaction> responseBankCardTransactions = new ArrayList<BankCardTransaction>();

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {

      // get transaction list from provider response
      responseBankCardTransactions =
          (List<BankCardTransaction>) providerResponse.getProviderData("listBankCardTransactions");

      // return list
      return responseBankCardTransactions;
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // log retrieved dates and return
    responseBankCardTransactions
        .stream()
        .forEach(p -> logService.debug(requestId, "L", "responseBankCardTransactions", p.getId()));
    return responseBankCardTransactions;
  }

  @Override
  public void upgradeBankCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      RequestUpgradeBankCard requestUpgradeBankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    try {
      // take delivery address from request
      String[] providerAddressLines =
          map2AddressLinesTo4(
              requestUpgradeBankCard.getAddress1(), requestUpgradeBankCard.getAddress2());
      bankCard.setAddress1(providerAddressLines[0]);
      bankCard.setAddress2(providerAddressLines[1]);
      bankCard.setAddress3(providerAddressLines[2]);
      bankCard.setAddress4(providerAddressLines[3]);

      bankCard.setCity(requestUpgradeBankCard.getCity());
      bankCard.setCountyName(requestUpgradeBankCard.getCountyName());
      bankCard.setZipCode(requestUpgradeBankCard.getZipCode());
      bankCard.setCountryCode(requestUpgradeBankCard.getCountryCode());

      // upgrade bank card from provider server
      upgradeProviderBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "provider", "upgraded");

      // set new status
      bankCard.setType(BankCardType.PHYSICAL);

      // update bank card audit
      bankCard.updateAudit(application.getApplicationStamp());

      // save bank card as well
      bankCardService.saveBankCard(requestId, account, bankCard);
      logService.debug(requestId, "L", "dbservice", "saved");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public void executeBankPayment(
      String requestId, Account account, BankCard bankCard, RequestBankPayment requestBankPayment) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(
        requestId,
        "IN",
        "requestBankPayment.beneficiaryName",
        requestBankPayment.getBeneficiaryName());
    logService.debug(
        requestId, "IN", "requestBankPayment.paymentAmount", requestBankPayment.getPaymentAmount());
    logService.debug(
        requestId, "IN", "requestBankPayment.reference", requestBankPayment.getReference());
    logService.debug(
        requestId, "IN", "requestBankPayment.creditorIBAN", requestBankPayment.getCreditorIban());
    logService.debug(
        requestId, "IN", "requestBankPayment.creditorBIC", requestBankPayment.getCreditorBic());

    try {
      // execute payment on provider server
      executeProviderBankPayment(requestId, account, bankCard, requestBankPayment);
      logService.debug(requestId, "L", "provider", "payment executed");

    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public FxQuote getCurrencyFxQuote(
      String requestId, Account account, BankCard bankCard, RequestFxQuote requestFxQuote) {

    logService.debug(requestId, "IN", "requestId", requestId);
    logService.debug(requestId, "IN", "bankCard.id", bankCard.getId());
    logService.debug(
        requestId, "IN", "requestFxQuote getCurrencyFrom", requestFxQuote.getCurrencyFrom());
    logService.debug(
        requestId, "IN", "requestFxQuote getCurrencyTo", requestFxQuote.getCurrencyTo());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETFXQUOTE);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.getProviderCurrencyFxQuote(
            requestId,
            bankCard,
            requestFxQuote.getCurrencyFrom(),
            requestFxQuote.getCurrencyTo(),
            requestFxQuote.getAmount().toString());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      logService.debug(requestId, "L", "status", "no error received");

      FxQuote fxQuote = new FxQuote();
      fxQuote.setCurrencyFrom(providerResponse.getProviderData("currencyFrom").toString());
      fxQuote.setCurrencyTo(providerResponse.getProviderData("currencyTo").toString());
      fxQuote.setAmount(providerResponse.getProviderData("amount").toString());
      fxQuote.setRate(providerResponse.getProviderData("rate").toString());
      return fxQuote;
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    return null;
  }

  @Override
  public ExecuteCardWalletsTrade executeCardWalletsTrade(
      String requestId,
      Account sessionAccount,
      BankCard bankCard,
      RequestExecuteCardWalletsTrade requestExecuteCardWalletsTrade) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.FXTRADE);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.executeProviderCurrencyFXTrade(
            requestId,
            bankCard,
            requestExecuteCardWalletsTrade.getCurrencyFrom(),
            requestExecuteCardWalletsTrade.getCurrencyTo(),
            requestExecuteCardWalletsTrade.getAmount().toString());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      logService.debug(requestId, "L", "status", "no error received");

      ExecuteCardWalletsTrade executeCardWalletsTrade = new ExecuteCardWalletsTrade();
      executeCardWalletsTrade.setAmount(providerResponse.getProviderData("amount").toString());
      executeCardWalletsTrade.setFromCurrency(
          providerResponse.getProviderData("fromCurrency").toString());
      executeCardWalletsTrade.setRate(providerResponse.getProviderData("rate").toString());
      executeCardWalletsTrade.setToCurrency(
          providerResponse.getProviderData("toCurrency").toString());
      return executeCardWalletsTrade;
    }

    GenericRestException gre = new GenericRestException();
    gre.setResponseErrors(providerResponse.getProviderErrors());
    throw gre;
  }

  @Override
  public void sendPin(String requestId, Account account, BankCard bankCard) {

    logService.debug(requestId, "IN", "account", account.getId());
    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    try {
      // send pin to user
      sendProviderPin(requestId, account, bankCard);
      logService.debug(requestId, "L", "provider", "sent");
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    // output
    logService.debug(requestId, "OUT", "response", "OK");
  }

  @Override
  public CardRegistration createCardRegistrations(
      String requestId,
      Account sessionAccount,
      User user,
      RequestCreateCardRegistration requestCreateCardRegistration) {

    logService.debug(requestId, "IN", "user.getEmail", user.getEmail());
    CardRegistration cardRegistration = new CardRegistration();
    try {
      cardRegistration =
          createProviderCardRegistration(
              requestId, sessionAccount, user, requestCreateCardRegistration);
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }
    return cardRegistration;
  }

  @Override
  public CardRegistration updateCardRegistrations(
      String requestId,
      Account sessionAccount,
      Application application,
      User user,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration) {

    logService.debug(requestId, "IN", "start", "no params");

    CardRegistration cardRegistration = new CardRegistration();

    try {
      cardRegistration =
          updateProviderCardRegistration(
              requestId, sessionAccount, cardRegistrationId, requestUpdateCardRegistration);

      String cardProviderId = cardRegistration.getCardProviderId();

      DepositCard depositCard = getProviderDepositCard(requestId, sessionAccount, cardProviderId);

      depositCard.setUserId(user.getId());

      // create bank card audit
      depositCard.updateAudit(application.getApplicationStamp());

      // save deposit card to database
      depositCardService.saveDepositCard(requestId, sessionAccount, depositCard);
    } catch (GenericRestException gre) {
      logService.error(requestId, "L", "error", gre.getMessage());
      logService.error(requestId, "L", "responseErrors", Json.toJson(gre.getResponseErrors()));
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(gre));
      throw gre;
    }

    return cardRegistration;
  }

  // ************************************************** DOCUMENT HELPERS
  // **************************************************

  /**
   * Create document in provider system
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param document
   * @throws GenericRestException
   */
  private void createProviderDocument(
      String requestId, Account account, Application application, User owner, Document document)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEDOCUMENT);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderDocument(requestId, account, application, owner, document);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for record
      document.setProviderId(providerResponse.getProviderData("providerId").toString());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create document page in provider system
   *
   * @param account
   * @param application
   * @param owner
   * @param document
   * @param fileContent
   * @return
   */
  private void createProviderDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String fileContent)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CERATEDOCUMENTPAGE);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderDocumentPage(
            requestId, account, application, owner, document, fileContent);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // nothing to do
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Submit provider document for approval in provider system
   *
   * @param account
   * @param application
   * @param document
   * @return
   */
  private void submitProviderDocument(
      String requestId, Account account, Application application, Document document)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.SUBMITDOCUMENT);

    // submit record to provider
    ProviderResponse providerResponse = providerService.submitProviderDocument(requestId, document);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // set document status
      document.setStatus(DocumentStatus.VALIDATION_ASKED);
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create Ubo declaration in provider system
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @throws GenericRestException
   */
  private void createProviderUboDeclaration(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration)
      throws GenericRestException {

    // TODO: this is copy-paste from createProviderDocument
    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEUBODECLARATION);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderUboDeclaration(requestId, account, application, owner);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for record
      uboDeclaration.setProviderId(providerResponse.getProviderData("providerId").toString());
      uboDeclaration.setProcessedDate(
          Integer.valueOf(providerResponse.getProviderData("processedDate").toString()));
      uboDeclaration.setCreatedAt(
          Long.valueOf(providerResponse.getProviderData("createdAt").toString()));
      uboDeclaration.setStatus(
          EnumUtils.getEnum(
              UboDeclarationStatus.class, providerResponse.getProviderData("status").toString()));
      uboDeclaration.setMessage(
          StringUtils.defaultString((String) providerResponse.getProviderData("message")));
      uboDeclaration.setReason(
          EnumUtils.getEnum(
              UboDeclarationRefusedReasonType.class,
              providerResponse.getProviderData("reason").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Submit provider Ubo declaration for approval in provider system
   *
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @return
   */
  private void submitProviderUboDeclaration(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.SUBMITUBODECLARATION);

    // submit record to provider
    ProviderResponse providerResponse =
        providerService.submitProviderUboDeclaration(requestId, owner, uboDeclaration);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // set document status
      uboDeclaration.setProcessedDate(
          Integer.valueOf(providerResponse.getProviderData("processedDate").toString()));
      uboDeclaration.setCreatedAt(
          Long.valueOf(providerResponse.getProviderData("createdAt").toString()));
      uboDeclaration.setStatus(
          EnumUtils.getEnum(
              UboDeclarationStatus.class, providerResponse.getProviderData("status").toString()));
      uboDeclaration.setMessage(
          StringUtils.defaultString((String) providerResponse.getProviderData("message")));
      uboDeclaration.setReason(
          EnumUtils.getEnum(
              UboDeclarationRefusedReasonType.class,
              providerResponse.getProviderData("reason").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create Ubo in provider system
   *
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @param fileContent
   * @return
   */
  private void createProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEUBO);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderUbo(
            requestId, account, application, owner, uboDeclaration, ubo);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      ubo.setProviderId(providerResponse.getProviderData("providerId").toString());
      ubo.setCreatedAt(Integer.valueOf(providerResponse.getProviderData("createdAt").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create Ubo in provider system
   *
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @param fileContent
   * @return
   */
  private void updateProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.UPDATEUBO);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.updateProviderUbo(
            requestId, account, application, owner, uboDeclaration, ubo);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      ubo.setCreatedAt(Integer.valueOf(providerResponse.getProviderData("createdAt").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  // ************************************************** WALLET HELPERS
  // **************************************************

  /**
   * Create wallet in provider system
   *
   * @param account
   * @param application
   * @param owner
   * @param wallet
   * @return
   */
  private void createProviderWallet(
      String requestId, Account account, Application application, User owner, Wallet wallet) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEWALLET);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderWallet(requestId, owner, wallet);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for record
      wallet.setProviderId(providerResponse.getProviderData("providerId").toString());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  public void updateCompanyWalletBalance(
      String requestId, Account account, Application application, Wallet wallet) {

    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETWALLET);

    // register record to provider
    ProviderResponse providerResponse = providerService.getProviderWallet(requestId, wallet);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // update provider id for record
      Amount balance = new Amount();
      balance.setCurrency(wallet.getBalance().getCurrency());
      balance.setValue((int) providerResponse.getProviderData("balance"));
      wallet.setBalance(balance);
      walletService.saveWallet(requestId, account, wallet);
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Update wallet balance
   *
   * @param account
   * @param wallet
   * @param valueToUpdate
   */
  private void updateWalletBalance(
      String requestId, Account account, Wallet wallet, int valueToUpdate) {

    walletService.updateWalletBalance(requestId, account, wallet, valueToUpdate);
  }

  /**
   * Update wallet blocked balance
   *
   * @param account
   * @param wallet
   * @param valueToUpdate
   */
  private void updateWalletBlockedBalance(
      String requestId, Account account, Wallet wallet, int valueToUpdate) {

    walletService.updateWalletBlockedBalance(requestId, account, wallet, valueToUpdate);
  }

  // ******* BANK ACCOUNT HELPERS **************************************************

  /**
   * Create bank account in provider system
   *
   * @param account
   * @param application
   * @param bankAccount
   * @return
   */
  private void createProviderBankAccount(
      String requestId,
      Account account,
      Application application,
      User user,
      BankAccount bankAccount)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEBANKACCOUNT);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.createProviderBankAccount(requestId, user, bankAccount);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for record
      bankAccount.setProviderId(providerResponse.getProviderData("providerId").toString());
      if (bankAccount.getType().equals(BankAccountType.IBAN)) {
        if (providerResponse.getProviderData("bic") != null) {
          ((BankAccountIBAN) bankAccount)
              .setBic(providerResponse.getProviderData("bic").toString());
        }
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Deactivate bank account in provider system
   *
   * @param account
   * @param application
   * @param bankAccount
   * @return
   */
  private void deactivateProviderBankAccount(
      String requestId,
      Account account,
      Application application,
      User user,
      BankAccount bankAccount) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.DEACTIVATEBANKACCOUNT);

    // call provider method
    ProviderResponse providerResponse =
        providerService.deactivateProviderBankAccount(requestId, user, bankAccount);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // update provider id for user
      bankAccount.setActive(false);
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  // ************************************************** USER HELPERS
  // **************************************************

  /**
   * Register user to account provider
   *
   * @param account
   * @param application
   * @param user
   */
  private void createProviderNaturalUser(
      String requestId, Account account, Application application, User user)
      throws GenericRestException {

    logService.debug(requestId, "IN", "user", Json.toJson(user).toString());

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEUSER);

    // register natural user to provider
    ProviderResponse providerResponse = providerService.createProviderNaturalUser(requestId, user);
    logService.debug(requestId, "L", "providerResponse", Json.toJson(providerResponse).toString());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for user
      user.setProviderId(providerResponse.getProviderData("providerId").toString());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Update user to account provider
   *
   * @param account
   * @param application
   * @param user
   */
  private void saveProviderNaturalUser(
      String requestId, Account account, Application application, User user)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.SAVEUSER);

    // save natural user to provider - do not handle any response
    ProviderResponse providerResponse = providerService.saveProviderNaturalUser(requestId, user);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // update audit
      user.updateAudit(application.getApplicationStamp());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Register legal user to provider
   *
   * @param account
   * @param application
   * @param legalUser
   * @return
   */
  private void createProviderLegalUser(
      String requestId, Account account, Application application, User legalUser) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATELEGALUSER);

    // register legal user to provider
    ProviderResponse providerResponse =
        providerService.createProviderLegalUser(requestId, legalUser);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for legal user
      legalUser.setProviderId(providerResponse.getProviderData("providerId").toString());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Save provider legal user
   *
   * @param account
   * @param application
   * @param user
   * @return
   */
  private void saveProviderLegalUser(
      String requestId, Account account, Application application, User legalUser)
      throws GenericRestException {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.SAVELEGALUSER);

    // call provider api
    ProviderResponse providerResponse = providerService.saveProviderLegalUser(requestId, legalUser);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      // update audit
      legalUser.updateAudit(application.getApplicationStamp());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }
  // ************************************************** PAYIN HELPERS
  // **************************************************

  /**
   * Create pay in transaction to provider
   *
   * @param account
   * @param application
   * @param user
   * @param creditWallet
   * @param accountWallet
   * @param mainTransaction
   * @param feeTransaction
   */
  private void createProviderPayIn(
      String requestId,
      Account account,
      Application application,
      User user,
      Wallet creditWallet,
      Wallet accountWallet,
      PayIn mainTransaction,
      Transfer feeTransaction) {

    // get financial provider
    ProviderService providerService = providerFactory.getProvider(account, ProviderOperation.PAYIN);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createProviderPayIn(
            requestId, account, application, user, creditWallet, mainTransaction, feeTransaction);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update pay in with provider details
      updatePayIn(
          requestId,
          account,
          application,
          mainTransaction,
          providerResponse.getProviderData("resultCode").toString(),
          providerResponse.getProviderData("resultMessage").toString(),
          providerResponse.getProviderData("externalReference").toString(),
          providerResponse.getProviderData("redirectUrl").toString(),
          providerResponse.getProviderData("payInId").toString());

      if ((feeTransaction != null) && (providerResponse.getProviderData("payInFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            feeTransaction,
            providerResponse.getProviderData("payInFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // we have an error on provider, mark transactions as failed and reverse balances
      rejectPayIn(
          requestId,
          account,
          application,
          mainTransaction,
          responseError.getErrorCode(),
          responseError.getErrorDescription());

      // if fees are applied
      if ((feeTransaction != null) && (accountWallet != null)) {
        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            feeTransaction,
            creditWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            false);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.PAYIN_NORMAL_FAILED,
          mainTransaction.getId());
    }
  }

  private void createProviderDirectPayIn(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      Wallet accountWallet,
      PayIn mainTransaction,
      Transfer feeTransaction) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.DIRECTPAYIN);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createProviderDirectPayIn(
            requestId, depositCard, user, creditWallet, mainTransaction, feeTransaction);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update pay in with provider details
      updateDirectPayIn(requestId, account, application, mainTransaction, providerResponse);

      if ((feeTransaction != null) && (providerResponse.getProviderData("payInFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            feeTransaction,
            providerResponse.getProviderData("payInFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // we have an error on provider, mark transactions as failed and reverse balances
      rejectPayIn(
          requestId,
          account,
          application,
          mainTransaction,
          responseError.getErrorCode(),
          responseError.getErrorDescription());

      // if fees are applied
      if ((feeTransaction != null) && (accountWallet != null)) {
        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            feeTransaction,
            creditWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            false);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.PAYIN_NORMAL_FAILED,
          mainTransaction.getId());
    }
  }

  /**
   * Reject pay in transaction due to some problems
   *
   * @param account
   * @param application
   * @param payIn
   * @param resultCode
   * @param resultMessage
   */
  private void rejectPayIn(
      String requestId,
      Account account,
      Application application,
      PayIn payIn,
      String resultCode,
      String resultMessage) {

    // set transaction details
    payIn.setStatus(TransactionStatus.FAILED);
    payIn.setResultCode(resultCode);
    payIn.setResultMessage(resultMessage);
    payIn.setExecutionDate(utilsService.getTimeStamp());

    // set transaction Audit
    payIn.updateAudit(application.getApplicationStamp());

    // save transaction
    payInService.savePayIn(requestId, account, payIn);
  }

  /**
   * Accept pay in transaction - update wallet balance as well
   *
   * @param account
   * @param application
   * @param payIn
   * @param creditWallet
   * @param resultCode
   * @param resultMessage
   */
  private void acceptPayIn(
      String requestId,
      Account account,
      Application application,
      PayIn payIn,
      Wallet creditWallet,
      String resultCode,
      String resultMessage,
      String securityInfo) {

    // set transaction details
    payIn.setStatus(TransactionStatus.SUCCEEDED);
    payIn.setResultCode(resultCode);
    payIn.setResultMessage(resultMessage);
    payIn.setExecutionDate(utilsService.getTimeStamp());
    payIn.setSecurityInfo(SecurityInfo.valueOf(securityInfo));

    // set transaction Audit
    payIn.updateAudit(application.getApplicationStamp());

    // save transaction
    payInService.savePayIn(requestId, account, payIn);

    // update balance for credited wallet - increase credit balance
    updateWalletBalance(requestId, account, creditWallet, +payIn.getAmount().getValue());
  }

  /**
   * Accept a transaction by taking from blocked balance of debit wallet and putting in credit
   * wallet balance
   *
   * @param account
   * @param application
   * @param payInFee
   * @param debitWallet
   * @param creditWallet
   * @param updateBalances
   */
  private void acceptPayInFee(
      String requestId,
      Account account,
      Application application,
      Transfer payInFee,
      Wallet payInWallet,
      Wallet accountWallet) {

    // change transaction status
    payInFee.setStatus(TransactionStatus.SUCCEEDED);
    payInFee.setExecutionDate(utilsService.getTimeStamp());

    // set transaction Audit
    payInFee.updateAudit(application.getApplicationStamp());

    // save fee transaction
    transferService.saveTransfer(requestId, account, payInFee);

    // update balance for payInWallet wallet - get balance
    updateWalletBalance(requestId, account, payInWallet, -payInFee.getAmount().getValue());

    // update balance for account wallet - add balance
    updateWalletBalance(requestId, account, accountWallet, +payInFee.getAmount().getValue());
  }

  /**
   * Update pay in due to a success provider registration
   *
   * @param account
   * @param application
   * @param transaction
   * @param resultCode
   * @param resultMessage
   * @param externalReference
   * @param redirectURL
   * @param providerId
   */
  private void updatePayIn(
      String requestId,
      Account account,
      Application application,
      PayIn transaction,
      String resultCode,
      String resultMessage,
      String externalReference,
      String redirectURL,
      String providerId) {

    // set transaction details
    transaction.setResultCode(resultCode);
    transaction.setResultMessage(resultMessage);
    transaction.setExternalReference(externalReference);
    transaction.setRedirectURL(redirectURL);
    transaction.setProviderId(providerId);

    // set transaction Audit
    transaction.updateAudit(application.getApplicationStamp());

    // save transaction
    payInService.savePayIn(requestId, account, transaction);
  }

  private void updateDirectPayIn(
      String requestId,
      Account account,
      Application application,
      PayIn mainTransaction,
      ProviderResponse providerResponse) {

    // set transaction details
    if (providerResponse.getProviderData("resultCode") != null) {
      mainTransaction.setResultCode(providerResponse.getProviderData("resultCode").toString());
    }

    if (providerResponse.getProviderData("resultMessage") != null) {
      mainTransaction.setResultMessage(
          providerResponse.getProviderData("resultMessage").toString());
    }

    if (providerResponse.getProviderData("id") != null) {
      mainTransaction.setProviderId(providerResponse.getProviderData("id").toString());
    }

    if (providerResponse.getProviderData("cardId") != null) {
      mainTransaction.setCardProviderId(providerResponse.getProviderData("cardId").toString());
    }

    if (providerResponse.getProviderData("executionType") != null) {
      mainTransaction.setExecutionType(
          ExecutionType.valueOf(providerResponse.getProviderData("executionType").toString()));
    }

    if (providerResponse.getProviderData("secureModeRedirectUrl") != null) {
      mainTransaction.setSecureModeRedirectUrl(
          providerResponse.getProviderData("secureModeRedirectUrl").toString());
    }

    // set transaction Audit
    mainTransaction.updateAudit(application.getApplicationStamp());

    // save transaction
    payInService.savePayIn(requestId, account, mainTransaction);
  }

  /**
   * Update pay in relation id
   *
   * @param account
   * @param application
   * @param payIn
   * @param relatedTransactionId
   */
  private void updatePayInRelation(
      String requestId,
      Account account,
      Application application,
      PayIn payIn,
      String relatedTransactionId) {

    // change travelling object as well first
    payIn.setRelatedTransactionId(relatedTransactionId);

    // save
    payInService.savePayIn(requestId, account, payIn);
  }

  private <T extends RequestCreateDirectPayIn> PayIn createDirectPayInTransaction(
      String requestId,
      Account account,
      Application application,
      Wallet creditWallet,
      Amount transferAmount,
      T requestCreateDirectPayIn) {

    // create transfer in the system
    PayIn newTransaction = new PayIn();

    // set custom data
    newTransaction.setCustomTag(requestCreateDirectPayIn.getCustomTag());

    // set credit side
    newTransaction.setCreditedUserId(creditWallet.getUserId());
    newTransaction.setCreditedWalletId(creditWallet.getId().toString());

    // set amount
    newTransaction.setAmount(transferAmount);

    // set transaction details
    newTransaction.setType(TransactionType.PAYIN);
    newTransaction.setNature(TransactionNature.REGULAR);
    newTransaction.setStatus(TransactionStatus.CREATED);

    // set pay in details
    newTransaction.setSecureModeReturnUrl(requestCreateDirectPayIn.getSecureModeReturnURL());
    newTransaction.setSecureMode(SecureMode.valueOf(requestCreateDirectPayIn.getSecureMode()));
    newTransaction.setStatementDescriptor(requestCreateDirectPayIn.getStatementDescriptor());
    newTransaction.setExecutionType(ExecutionType.DIRECT);
    newTransaction.setPaymentType(PaymentType.CARD);
    newTransaction.setCardProviderId(requestCreateDirectPayIn.getCardId());

    // set transaction Audit
    newTransaction.updateAudit(application.getApplicationStamp());
    logService.debug(
        requestId,
        "L",
        "instanceof requestCreateDirectPayIn",
        requestCreateDirectPayIn.getClass().getName());
    if (requestCreateDirectPayIn instanceof RequestCreateAVSDirectPayIn) {
      RequestCreateAVSDirectPayIn requestCreateAVSDirectPayIn =
          (RequestCreateAVSDirectPayIn) requestCreateDirectPayIn;
      Address billing = new Address();
      billing.setAddressLine1(requestCreateAVSDirectPayIn.getBilling().getAddressLine1());
      billing.setAddressLine2(requestCreateAVSDirectPayIn.getBilling().getAddressLine2());
      billing.setCity(requestCreateAVSDirectPayIn.getBilling().getCity());
      billing.setCountry(CountryISO.valueOf(requestCreateAVSDirectPayIn.getBilling().getCountry()));
      billing.setCounty(requestCreateAVSDirectPayIn.getBilling().getCounty());
      billing.setPostalCode(requestCreateAVSDirectPayIn.getBilling().getPostalCode());
      newTransaction.setBilling(billing);
    }
    // save transaction
    payInService.savePayIn(requestId, account, newTransaction);

    // we do not update balance unless provider confirms the deposit/pay-in
    return newTransaction;
  }

  /**
   * Create a transaction for pay in
   *
   * @param account
   * @param application
   * @param creditWallet
   * @param amount
   * @param customTag
   * @return
   */
  private PayIn createPayInTransaction(
      String requestId,
      Account account,
      Application application,
      Wallet creditWallet,
      Amount amount,
      String customTag,
      String returnURL,
      CardType cardType,
      SecureMode secureMode,
      CultureCode culture,
      String templateURL,
      String statementDescriptor) {

    // create transfer in the system
    PayIn newTransaction = new PayIn();

    // set custom data
    newTransaction.setCustomTag(customTag);

    // set credit side
    newTransaction.setCreditedUserId(creditWallet.getUserId());
    newTransaction.setCreditedWalletId(creditWallet.getId().toString());

    // set amount
    newTransaction.setAmount(amount);

    // set transaction details
    newTransaction.setType(TransactionType.PAYIN);
    newTransaction.setNature(TransactionNature.REGULAR);
    newTransaction.setStatus(TransactionStatus.CREATED);

    // set pay in details
    newTransaction.setReturnURL(returnURL);
    newTransaction.setCardType(cardType);
    newTransaction.setSecureMode(secureMode);
    newTransaction.setCulture(culture);
    newTransaction.setTemplateURL(templateURL);
    newTransaction.setStatementDescriptor(statementDescriptor);
    newTransaction.setExecutionType(ExecutionType.WEB);
    newTransaction.setPaymentType(PaymentType.CARD);

    // set transaction Audit
    newTransaction.updateAudit(application.getApplicationStamp());

    // save transaction
    payInService.savePayIn(requestId, account, newTransaction);

    // we do not update balance unless provider confirms the deposit/pay-in
    return newTransaction;
  }

  // ************************************************** TRANSFER HELPERS
  // **************************************************
  /**
   * Create a between wallet of the same currency transaction
   *
   * @param account
   * @param application
   * @param debitWallet
   * @param creditWallet
   * @param debitedFunds
   * @param fees
   * @param feeModel
   * @param customTag
   * @param updateBalances
   * @return
   */
  private Transfer createNoFxTransfer(
      String requestId,
      Account account,
      Application application,
      Wallet debitWallet,
      Wallet creditWallet,
      Amount amount,
      String customTag,
      TransactionType transactionType,
      TransactionNature transactionNature,
      String relatedTransactionId,
      boolean updateBalances) {

    // create transfer in the system
    Transfer newTransaction = new Transfer();

    // set custom data
    newTransaction.setCustomTag(customTag);

    // set debit side
    newTransaction.setDebitedUserId(debitWallet.getUserId());
    newTransaction.setDebitedWalletId(debitWallet.getId().toString());

    // set credit side
    newTransaction.setCreditedUserId(creditWallet.getUserId());
    newTransaction.setCreditedWalletId(creditWallet.getId().toString());

    // set amount
    newTransaction.setAmount(amount);

    // set transaction details
    newTransaction.setType(transactionType);
    newTransaction.setNature(transactionNature);
    newTransaction.setStatus(TransactionStatus.CREATED);

    // set relation if exists
    if (relatedTransactionId != null) {
      newTransaction.setRelatedTransactionId(relatedTransactionId);
    }

    // set transaction Audit
    newTransaction.updateAudit(application.getApplicationStamp());

    newTransaction.setId(ObjectId.get().toString());
    newTransaction.setCreatedAt(utilsService.getCurrentTimeMiliseconds() / 1000L);

    // save transaction
    transferService.saveTransfer(requestId, account, newTransaction);

    // test for balance update, some transactions like deposit fee, are not processed unless they
    // are confirmed
    if (updateBalances) {
      // update balance for debited wallet - decrease
      updateWalletBalance(requestId, account, debitWallet, -amount.getValue());

      // update balance for debited wallet - increase
      updateWalletBlockedBalance(requestId, account, debitWallet, amount.getValue());
    }

    return newTransaction;
  }

  /**
   * Register transaction to account provider
   *
   * @param account
   * @param application
   * @param debitWallet
   * @param creditWallet
   * @param transfer
   * @param transferFee
   * @return
   */
  private boolean createProviderTransfer(
      String requestId,
      Account account,
      Application application,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet,
      Transfer transfer,
      Transfer transferFee) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.TRANSFER);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createProviderTransfer(
            requestId,
            account,
            application,
            transfer,
            transferFee,
            debitUser,
            debitWallet,
            creditUser,
            creditWallet);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for main transaction
      updateTransferProvider(
          requestId,
          account,
          application,
          transfer,
          providerResponse.getProviderData("transferId").toString());
      logService.debug(requestId, "L", "transfer", Json.toJson(transfer).toString());

      // update provider id for fee transaction - if fees are involved
      if ((transferFee != null) && (providerResponse.getProviderData("transferFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            transferFee,
            providerResponse.getProviderData("transferFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // reject main transaction
      rejectTransfer(
          requestId,
          account,
          application,
          transfer,
          debitWallet,
          responseError.getErrorCode(),
          responseError.getErrorDescription(),
          true);

      // if fees are applied
      if (transferFee != null) {
        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            transferFee,
            debitWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            true);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.TRANSFER_NORMAL_FAILED,
          transfer.getId());
    }

    // irrelevant response - we just need it to process this operation a-sync
    return true;
  }

  // ************************************************** TRANSACTION HELPERS
  // **************************************************

  /**
   * Reject a transaction by unblocking debit wallet balance
   *
   * @param account
   * @param application
   * @param transfer
   * @param debitWallet
   * @param updateBalances
   */
  private void rejectTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      Wallet debitWallet,
      String resultCode,
      String resultMessage,
      boolean updateBalances) {

    transfer.setStatus(TransactionStatus.FAILED);
    transfer.setResultCode(resultCode);
    transfer.setResultMessage(resultMessage);
    transfer.setExecutionDate(utilsService.getTimeStamp());

    // save transfer
    transferService.saveTransfer(requestId, account, transfer);

    if (updateBalances) {
      // update balance for debited wallet - increase back debit balance
      updateWalletBalance(requestId, account, debitWallet, +transfer.getAmount().getValue());

      // update balance for debited wallet - decrease back blocked balance
      updateWalletBlockedBalance(requestId, account, debitWallet, -transfer.getAmount().getValue());
    }
  }

  /**
   * Accept a transfer by taking from blocked balance of debit wallet and putting in credit wallet
   * balance
   *
   * @param account
   * @param application
   * @param transfer
   * @param debitWallet
   * @param creditWallet
   * @param updateBalances
   */
  private void acceptTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      Wallet debitWallet,
      Wallet creditWallet,
      boolean updateBalances) {

    // update status
    transfer.setStatus(TransactionStatus.SUCCEEDED);
    transfer.setExecutionDate(utilsService.getTimeStamp());

    // save record
    transferService.saveTransfer(requestId, account, transfer);

    if (updateBalances) {
      // update balance for debited wallet - get balance
      updateWalletBlockedBalance(requestId, account, debitWallet, -transfer.getAmount().getValue());

      // update balance for credited wallet - add balance
      updateWalletBalance(requestId, account, creditWallet, +transfer.getAmount().getValue());
    }
  }

  /**
   * Update provide id on transfer
   *
   * @param account
   * @param transfer
   * @param providerId
   */
  private void updateTransferProvider(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      String providerId) {

    // update travelling object first
    transfer.setProviderId(providerId);

    // save transfer
    transferService.saveTransfer(requestId, account, transfer);
  }

  /**
   * Update transfer relation id
   *
   * @param account
   * @param transfer
   * @param relatedTransactionId
   * @return
   */
  private void updateTransferRelation(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      String relatedTransactionId) {

    // change traveling object as well first
    transfer.setRelatedTransactionId(relatedTransactionId);

    // save
    transferService.saveTransfer(requestId, account, transfer);
  }

  // ************************************************** PAYOUT HELPERS
  // **************************************************
  /**
   * Accept pay out transaction - update blocked wallet balance as well
   *
   * @param account
   * @param application
   * @param transaction
   * @param debitWallet
   * @param resultCode
   * @param resultMessage
   */
  private void acceptPayOut(
      String requestId,
      Account account,
      Application application,
      PayOut transaction,
      Wallet debitWallet,
      String resultCode,
      String resultMessage) {

    // set transaction details
    transaction.setStatus(TransactionStatus.SUCCEEDED);
    transaction.setResultCode(resultCode);
    transaction.setResultMessage(resultMessage);
    transaction.setExecutionDate(utilsService.getTimeStamp());

    // set transaction Audit
    transaction.updateAudit(application.getApplicationStamp());

    // save transaction
    payOutService.savePayOut(requestId, account, transaction);

    // update blocked balance for debited wallet - decrease pay out amount
    updateWalletBlockedBalance(
        requestId, account, debitWallet, -transaction.getAmount().getValue());
  }

  /**
   * Reject pay out
   *
   * @param account
   * @param application
   * @param transaction
   * @param debitWallet
   * @param resultCode
   * @param resultMessage
   */
  private void rejectPayOut(
      String requestId,
      Account account,
      Application application,
      PayOut transaction,
      Wallet debitWallet,
      String resultCode,
      String resultMessage) {

    // change transaction status
    transaction.setStatus(TransactionStatus.FAILED);

    // change execution date
    transaction.setResultCode(resultCode);
    transaction.setResultMessage(resultMessage);
    transaction.setExecutionDate(utilsService.getTimeStamp());

    // set transaction Audit
    transaction.updateAudit(application.getApplicationStamp());

    // save fee transaction
    payOutService.savePayOut(requestId, account, transaction);

    // update balance for debited wallet - increase back debited balance
    updateWalletBalance(requestId, account, debitWallet, +transaction.getAmount().getValue());

    // update blocked balance for debited wallet - decrease
    updateWalletBlockedBalance(
        requestId, account, debitWallet, -transaction.getAmount().getValue());
  }

  /**
   * Update pay out after registered to provider
   *
   * @param account
   * @param application
   * @param payOut
   * @param resultCode
   * @param resultMessage
   * @param externalReference
   * @param providerId
   */
  private void updatePayOut(
      String requestId,
      Account account,
      Application application,
      PayOut payOut,
      String resultCode,
      String resultMessage,
      String externalReference,
      String providerId) {

    // set transaction details
    payOut.setResultCode(resultCode);
    payOut.setResultMessage(resultMessage);
    payOut.setExternalReference(externalReference);
    payOut.setProviderId(providerId);

    // set transaction Audit
    payOut.updateAudit(application.getApplicationStamp());

    // save transaction
    payOutService.savePayOut(requestId, account, payOut);
  }

  /**
   * Update pay out relation with fee
   *
   * @param account
   * @param application
   * @param payOut
   * @param relatedTransactionId
   */
  private void updatePayOutRelation(
      String requestId,
      Account account,
      Application application,
      PayOut payOut,
      String relatedTransactionId) {

    // change traveling object as well first
    payOut.setRelatedTransactionId(relatedTransactionId);

    // save
    payOutService.savePayOut(requestId, account, payOut);
  }

  /**
   * Create provider pay out
   *
   * @param account
   * @param application
   * @param debitWallet
   * @param payOut
   * @param feeTransaction
   * @return
   */
  private boolean createProviderPayOut(
      String requestId,
      Account account,
      Application application,
      BankAccount bankAccount,
      User debitUser,
      Wallet debitWallet,
      PayOut payOut,
      Transfer feeTransaction) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.PAYOUT);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createProviderPayOut(
            requestId,
            account,
            application,
            bankAccount,
            debitUser,
            debitWallet,
            payOut,
            feeTransaction);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update provider id for main pay out transaction
      updatePayOut(
          requestId,
          account,
          application,
          payOut,
          providerResponse.getProviderData("resultCode").toString(),
          providerResponse.getProviderData("resultMessage").toString(),
          providerResponse.getProviderData("externalReference").toString(),
          providerResponse.getProviderData("payOutId").toString());

      // update provider id for fee - if fees are involved
      if ((feeTransaction != null) && (providerResponse.getProviderData("payOutFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            feeTransaction,
            providerResponse.getProviderData("payOutFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // we have an error on provider, mark transactions as failed and reverse balances

      // reject main pay out transaction
      rejectPayOut(
          requestId,
          account,
          application,
          payOut,
          debitWallet,
          responseError.getErrorCode(),
          responseError.getErrorDescription());

      // if fees are applied
      if (feeTransaction != null) {
        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            feeTransaction,
            debitWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            true);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId, account, application, NotificationType.PAYOUT_NORMAL_FAILED, payOut.getId());
    }

    // irrelevant response
    return true;
  }

  /**
   * Create a transaction for pay out
   *
   * @param account
   * @param application
   * @param debitWallet
   * @param amount
   * @param customTag
   * @param paymentType
   * @param bankAccountId
   * @param bankWireRef
   * @return
   */
  private PayOut createPayOutTransaction(
      String requestId,
      Account account,
      Application application,
      Wallet debitWallet,
      Amount amount,
      String customTag,
      PaymentType paymentType,
      String bankAccountId,
      String bankWireRef) {

    // create transfer in the system
    PayOut newTransaction = new PayOut();

    // set custom data
    newTransaction.setCustomTag(customTag);

    // set debit side
    newTransaction.setDebitedUserId(debitWallet.getUserId());
    newTransaction.setDebitedWalletId(debitWallet.getId().toString());

    // set amount
    newTransaction.setAmount(amount);

    // set transaction details
    newTransaction.setType(TransactionType.PAYOUT);
    newTransaction.setNature(TransactionNature.REGULAR);
    newTransaction.setStatus(TransactionStatus.CREATED);

    // set payout details
    newTransaction.setPaymentType(paymentType);
    newTransaction.setBankAccountId(bankAccountId);
    newTransaction.setBankWireRef(bankWireRef);

    // set transaction Audit
    newTransaction.updateAudit(application.getApplicationStamp());

    newTransaction.setId(ObjectId.get().toString());
    newTransaction.setCreatedAt(utilsService.getTimeStamp());

    // save transaction
    payOutService.savePayOut(requestId, account, newTransaction);

    // update balance for debited wallet - decrease
    updateWalletBalance(requestId, account, debitWallet, -amount.getValue());

    // update blocked balance for debited wallet - increase
    updateWalletBlockedBalance(requestId, account, debitWallet, amount.getValue());

    return newTransaction;
  }
  // ************************************************** REFUND HELPERS
  // **************************************************

  /**
   * Accept refund transaction
   *
   * @param account
   * @param application
   * @param refund
   * @param originalCreditWallet
   * @param originalDebitWallet
   * @param updateBalances
   */
  private void acceptRefund(
      String requestId,
      Account account,
      Application application,
      Refund refund,
      Wallet originalCreditWallet,
      Wallet originalDebitWallet,
      boolean updateBalances) {

    // update status
    refund.setStatus(TransactionStatus.SUCCEEDED);
    refund.setExecutionDate(utilsService.getTimeStamp());

    // save record
    refundService.saveRefund(requestId, account, refund);

    if (updateBalances) {
      // update balance for debited wallet - get balance
      updateWalletBlockedBalance(
          requestId, account, originalCreditWallet, -refund.getAmount().getValue());

      // update balance for credited wallet - add balance
      updateWalletBalance(requestId, account, originalDebitWallet, +refund.getAmount().getValue());
    }
  }

  /**
   * Update refund with details after submitted to provider
   *
   * @param account
   * @param application
   * @param refund
   * @param resultCode
   * @param resultMessage
   * @param refundReasonType
   * @param refusedReasonMessage
   * @param providerId
   */
  private void updateRefund(
      String requestId,
      Account account,
      Application application,
      Refund refund,
      String resultCode,
      String resultMessage,
      RefundReasonType refundReasonType,
      String refusedReasonMessage,
      String providerId) {

    // set refund details
    refund.setResultCode(resultCode);
    refund.setResultMessage(resultMessage);
    refund.setRefundReasonType(refundReasonType);
    refund.setRefusedReasonMessage(refusedReasonMessage);

    // set refund provider id
    refund.setProviderId(providerId);

    // set transaction Audit
    refund.updateAudit(application.getApplicationStamp());

    // save transaction
    refundService.saveRefund(requestId, account, refund);
  }

  /**
   * Update refund relation
   *
   * @param account
   * @param application
   * @param refund
   * @param relatedTransactionId
   */
  private void updateRefundRelation(
      String requestId,
      Account account,
      Application application,
      Refund refund,
      String relatedTransactionId) {

    // change travelling object as well first
    refund.setRelatedTransactionId(relatedTransactionId);

    // save
    refundService.saveRefund(requestId, account, refund);
  }

  /**
   * Reject procedure in case that a refund is not accepted
   *
   * @param account
   * @param application
   * @param refundTransaction
   * @param originalCreditWallet
   * @param resultCode
   * @param resultMessage
   * @param refundReasonType
   * @param refusedReasonMessage
   */
  private void rejectRefund(
      String requestId,
      Account account,
      Application application,
      Refund refundTransaction,
      Wallet originalCreditWallet,
      String resultCode,
      String resultMessage,
      RefundReasonType refundReasonType,
      String refusedReasonMessage) {

    // change transaction refund status
    refundTransaction.setStatus(TransactionStatus.FAILED);

    // set refund details
    refundTransaction.setResultCode(resultCode);
    refundTransaction.setResultMessage(resultMessage);
    refundTransaction.setRefundReasonType(refundReasonType);
    refundTransaction.setRefusedReasonMessage(refusedReasonMessage);
    refundTransaction.setExecutionDate(utilsService.getTimeStamp());

    // set transaction Audit
    refundTransaction.updateAudit(application.getApplicationStamp());

    // save fee transaction
    refundService.saveRefund(requestId, account, refundTransaction);

    // update balance for original credited wallet - increase back balance
    updateWalletBalance(
        requestId, account, originalCreditWallet, +refundTransaction.getAmount().getValue());

    // update balance for original credited wallet - decrease back blocked balance
    updateWalletBlockedBalance(
        requestId, account, originalCreditWallet, -refundTransaction.getAmount().getValue());
  }

  /**
   * Create a refund transaction in payment framework
   *
   * @param account
   * @param application
   * @param originalDebitWallet
   * @param originalCreditWallet
   * @param originalTransaction
   * @param customTag
   * @return
   */
  private Refund createNoFxRefundTransaction(
      String requestId,
      Account account,
      Application application,
      Wallet originalDebitWallet,
      Wallet originalCreditWallet,
      Transfer originalTransaction,
      String customTag) {

    // create transfer refund in the system
    Refund newRefundTransaction = new Refund();

    // set custom data
    newRefundTransaction.setCustomTag(customTag);

    // set debit side
    newRefundTransaction.setDebitedUserId(originalCreditWallet.getUserId());
    newRefundTransaction.setDebitedWalletId(originalCreditWallet.getId().toString());

    // set credit side
    newRefundTransaction.setCreditedUserId(originalDebitWallet.getUserId());
    newRefundTransaction.setCreditedWalletId(originalDebitWallet.getId().toString());

    // set amount
    newRefundTransaction.setAmount(originalTransaction.getAmount());

    // set transaction details
    newRefundTransaction.setType(TransactionType.TRANSFER);
    newRefundTransaction.setNature(TransactionNature.REFUND);
    newRefundTransaction.setStatus(TransactionStatus.CREATED);

    // set refund specific data
    newRefundTransaction.setInitialTransactionId(originalTransaction.getId().toString());
    newRefundTransaction.setInitialTransactionType(TransactionType.TRANSFER);

    // set transaction Audit
    newRefundTransaction.updateAudit(application.getApplicationStamp());

    // save transaction
    refundService.saveRefund(requestId, account, newRefundTransaction);

    // update balance for debited wallet - decrease
    updateWalletBalance(
        requestId, account, originalCreditWallet, -originalTransaction.getAmount().getValue());

    // update clocked balance for debited wallet - increase
    updateWalletBlockedBalance(
        requestId, account, originalCreditWallet, originalTransaction.getAmount().getValue());

    return newRefundTransaction;
  }

  private Refund createNoFxRefundPayIn(
      String requestId,
      Account account,
      Application application,
      RequestRefundPayIn formRefundPayIn,
      PayIn originalPayIn,
      Wallet originalCreditWallet) {

    // create transfer refund in the system
    Refund newRefundTransaction = new Refund();

    // set custom data
    if (formRefundPayIn.getCustomTag() != null) {
      newRefundTransaction.setCustomTag(formRefundPayIn.getCustomTag());
    }

    // set debit side
    newRefundTransaction.setDebitedUserId(originalPayIn.getCreditedUserId());
    newRefundTransaction.setDebitedWalletId(originalPayIn.getCreditedWalletId());

    // set amount
    if (formRefundPayIn.getAmount() == null) {
      newRefundTransaction.setAmount(originalPayIn.getAmount());
    } else {
      Amount debitedFunds = new Amount();
      debitedFunds.setCurrency(CurrencyISO.valueOf(formRefundPayIn.getAmount().getCurrency()));
      debitedFunds.setValue(formRefundPayIn.getAmount().getValue());
      newRefundTransaction.setAmount(debitedFunds);
    }

    // set transaction details
    newRefundTransaction.setType(TransactionType.PAYOUT);
    newRefundTransaction.setNature(TransactionNature.REFUND);
    newRefundTransaction.setStatus(TransactionStatus.CREATED);

    // set refund specific data
    newRefundTransaction.setInitialTransactionId(originalPayIn.getId().toString());
    newRefundTransaction.setInitialTransactionType(TransactionType.PAYIN);

    // set transaction Audit
    newRefundTransaction.updateAudit(application.getApplicationStamp());

    // save transaction
    refundService.saveRefund(requestId, account, newRefundTransaction);

    // update balance for debited wallet - decrease
    updateWalletBalance(
        requestId, account, originalCreditWallet, -newRefundTransaction.getAmount().getValue());

    // update clocked balance for debited wallet - increase
    updateWalletBlockedBalance(
        requestId, account, originalCreditWallet, newRefundTransaction.getAmount().getValue());

    return newRefundTransaction;
  }

  /**
   * Register refund to account provider
   *
   * @param account
   * @param application
   * @param originalCreditWallet
   * @param originalDebitWallet
   * @param refundAccountWallet
   * @param refundTransaction
   * @param refundFeeTransaction
   * @param originalTransaction
   * @param originalFeeTransaction
   * @return
   */
  private boolean createProviderTransferRefund(
      String requestId,
      Account account,
      Application application,
      Wallet originalCreditWallet,
      Wallet originalDebitWallet,
      User originalDebitUser,
      Wallet refundAccountWallet,
      Refund refundTransaction,
      Transfer refundFeeTransaction,
      Transfer originalTransaction,
      Transfer originalFeeTransaction) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REFUND);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createProviderTransferRefund(
            requestId,
            refundTransaction,
            refundFeeTransaction,
            originalTransaction,
            originalDebitUser);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      // update details for refund transaction
      updateRefund(
          requestId,
          account,
          application,
          refundTransaction,
          providerResponse.getProviderData("resultCode").toString(),
          providerResponse.getProviderData("resultMessage").toString(),
          RefundReasonType.OTHER,
          providerResponse.getProviderData("refundReasonMessage").toString(),
          providerResponse.getProviderData("refundId").toString());

      // update provider id for refund fee transaction - if fees are involved
      if ((refundFeeTransaction != null)
          && (providerResponse.getProviderData("refundFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            refundFeeTransaction,
            providerResponse.getProviderData("refundFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      // we have an error on provider, mark refund transactions as failed and reverse balances
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // reject refund transaction
      rejectRefund(
          requestId,
          account,
          application,
          refundTransaction,
          originalCreditWallet,
          responseError.getErrorCode(),
          responseError.getErrorDescription(),
          RefundReasonType.OTHER,
          "Provider error");

      // if fees are applied
      if ((refundFeeTransaction != null) && (refundAccountWallet != null)) {
        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            refundFeeTransaction,
            refundAccountWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            true);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId,
          account,
          application,
          NotificationType.TRANSFER_REFUND_FAILED,
          refundTransaction.getId());
    }

    return true;
  }

  /**
   * Register a PayIn Refund at provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param formRefundPayIn
   * @param originalPayIn
   * @param originalCreditWallet
   * @param refund
   * @return
   */
  private boolean createProviderPayInRefund(
      String requestId,
      Account account,
      Application application,
      RequestRefundPayIn formRefundPayIn,
      PayIn originalPayIn,
      Wallet originalCreditWallet,
      User originalCredituser,
      Refund refund,
      Transfer refundFee) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.REFUND);

    // register transaction to provider
    ProviderResponse providerResponse =
        providerService.createPayInRefund(
            requestId, originalPayIn.getProviderId(), refund, refundFee, originalCredituser);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {

      // update details for refund transaction
      updateRefund(
          requestId,
          account,
          application,
          refund,
          providerResponse.getProviderData("resultCode").toString(),
          providerResponse.getProviderData("resultMessage").toString(),
          RefundReasonType.OTHER,
          providerResponse.getProviderData("refundReasonMessage").toString(),
          providerResponse.getProviderData("refundId").toString());

      // update provider id for refund fee transaction - if fees are involved
      if ((refundFee != null) && (providerResponse.getProviderData("refundFeeId") != null)) {
        updateTransferProvider(
            requestId,
            account,
            application,
            refundFee,
            providerResponse.getProviderData("refundFeeId").toString());
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {

      // we have an error on provider, mark refund transactions as failed and reverse balances
      ResponseError responseError = providerResponse.getProviderErrors().get(0);

      // reject refund transaction
      rejectRefund(
          requestId,
          account,
          application,
          refund,
          originalCreditWallet,
          responseError.getErrorCode(),
          responseError.getErrorDescription(),
          RefundReasonType.OTHER,
          "Provider error");

      // if fees are applied
      if ((refundFee != null) && (originalCreditWallet != null)) {

        // reject fee transaction
        rejectTransfer(
            requestId,
            account,
            application,
            refundFee,
            originalCreditWallet,
            responseError.getErrorCode(),
            responseError.getErrorDescription(),
            true);
      }

      // notify client hook
      notificationService.notifyClient(
          requestId, account, application, NotificationType.PAYIN_REFUND_FAILED, refund.getId());
    }
    return true;
  }

  // ************************************************** PAYIN REFUND HELPERS
  // **************************************************

  /**
   * Reject a payInRefund by taking from blocked balance of debit wallet and put back into debit
   * wallet balance
   *
   * @param requestId
   * @param account
   * @param userWallet
   * @param payInRefund
   */
  private void rejectPayInRefund(
      String requestId, Account account, Wallet userWallet, Refund payInRefund) {

    // update status
    payInRefund.setStatus(TransactionStatus.FAILED);
    payInRefund.setExecutionDate(utilsService.getTimeStamp());

    // save record
    refundService.saveRefund(requestId, account, payInRefund);

    // update blocked balance for debited wallet - get balance
    updateWalletBlockedBalance(requestId, account, userWallet, -payInRefund.getAmount().getValue());

    // update balance for debited wallet - get balance
    updateWalletBalance(requestId, account, userWallet, +payInRefund.getAmount().getValue());
  }

  /**
   * Accept a payInRefund by taking from blocked balance of debit wallet
   *
   * @param requestId
   * @param account
   * @param userWallet
   * @param payInRefund
   */
  private void acceptPayInRefund(
      String requestId, Account account, Wallet userWallet, Refund payInRefund) {

    // update status
    payInRefund.setStatus(TransactionStatus.SUCCEEDED);
    payInRefund.setExecutionDate(utilsService.getTimeStamp());

    // save record
    refundService.saveRefund(requestId, account, payInRefund);

    // update balance for debited wallet - get balance
    updateWalletBlockedBalance(requestId, account, userWallet, -payInRefund.getAmount().getValue());
  }

  // ************************************************** PFS HELPERS
  // **************************************************
  /**
   * Withdraw money from provider card
   *
   * @param account
   * @param bankCard
   * @param currency
   * @param amount
   */
  private void providerTransferFromBankCard(
      String requestId, Account account, BankCard bankCard, String currency, Integer amount) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.TRANSFERFROMCARD);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.providerTransferFromBankCard(requestId, bankCard, currency, amount);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Deposit money to card at provider
   *
   * @param account
   * @param bankCard
   * @param currency
   * @param amount
   */
  private void providerTransferToBankCard(
      String requestId, Account account, BankCard bankCard, String currency, Integer amount) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.TRANSFERTOCARD);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.providerTransferToBankCard(requestId, bankCard, currency, amount);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Lock or unlock a card to provider
   *
   * @param account
   * @param bankCard
   * @param oldStatus
   * @param newStatus
   */
  private void lockUnlockProviderBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.LOCKUNLOCKCARD);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.lockUnlockProviderBankCard(requestId, bankCard, oldStatus, newStatus);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Change status of a bank card at provider server
   *
   * @param account
   * @param bankCard
   */
  private void changeStatusProviderBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CHANGECARDSTATUS);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.changeStatusProviderBankCard(requestId, bankCard, oldStatus, newStatus);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Send the new pin request request to provider
   *
   * @param account
   * @param bankCard
   */
  private void sendProviderPin(String requestId, Account account, BankCard bankCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.SENDCARDPIN);

    // register record to provider
    ProviderResponse providerResponse = providerService.sendProviderPin(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create a bank card to provider
   *
   * @param account
   * @param bankCard
   */
  private void upgradeProviderBankCard(String requestId, Account account, BankCard bankCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.UPGRADEBANKCARD);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.upgradeProviderBankCard(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create a card registration at provider
   *
   * @param requestId
   * @param sessionAccount
   * @param user
   * @param requestCreateCardRegistration
   * @return
   */
  private CardRegistration createProviderCardRegistration(
      String requestId,
      Account sessionAccount,
      User user,
      RequestCreateCardRegistration requestCreateCardRegistration) {

    CardRegistration cardRegistration = new CardRegistration();

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.CREATECARDREGISTRATION);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.createCardRegistrations(requestId, user, requestCreateCardRegistration);

    logService.debug(
        requestId, "IN", "providerResponse", utilsService.prettyPrintObject(providerResponse));

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      mapCardRegistration(
          cardRegistration, providerResponse, requestCreateCardRegistration.getApiVersion());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    return cardRegistration;
  }

  private CardRegistration updateProviderCardRegistration(
      String requestId,
      Account sessionAccount,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration) {

    CardRegistration cardRegistration = new CardRegistration();

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.UPDATECARDREGISTRATION);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.updateCardRegistrations(
            requestId, cardRegistrationId, requestUpdateCardRegistration);
    logService.debug(
        requestId, "IN", "providerResponse", utilsService.prettyPrintObject(providerResponse));

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      mapCardRegistration(
          cardRegistration, providerResponse, requestUpdateCardRegistration.getApiVersion());
      return cardRegistration;
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    return cardRegistration;
  }

  private void mapCardRegistration(
      CardRegistration cardRegistration, ProviderResponse providerResponse, String apiVersion) {

    if (providerResponse.getProviderData("id") != null) {
      cardRegistration.setId(providerResponse.getProviderData("id").toString());
    }
    if (providerResponse.getProviderData("tag") != null) {
      cardRegistration.setCustomTag(providerResponse.getProviderData("tag").toString());
    }
    if (providerResponse.getProviderData("creationDate") != null) {
      cardRegistration.setCreatedAt(
          Long.valueOf(providerResponse.getProviderData("creationDate").toString()));
    }
    if (providerResponse.getProviderData("userId") != null) {
      cardRegistration.setUserProviderId(providerResponse.getProviderData("userId").toString());
    }
    if (providerResponse.getProviderData("accessKey") != null) {
      cardRegistration.setAccessKey(providerResponse.getProviderData("accessKey").toString());
    }
    if (providerResponse.getProviderData("preregistrationData") != null) {
      cardRegistration.setPreRegistrationData(
          providerResponse.getProviderData("preregistrationData").toString());
    }
    if (providerResponse.getProviderData("cardRegistrationUrl") != null) {
      cardRegistration.setCardRegistrationUrl(
          providerResponse.getProviderData("cardRegistrationUrl").toString());
    }
    if (providerResponse.getProviderData("cardId") != null) {
      cardRegistration.setCardProviderId(providerResponse.getProviderData("cardId").toString());
    }
    if (providerResponse.getProviderData("registrationData") != null) {
      cardRegistration.setRegistrationData(
          providerResponse.getProviderData("registrationData").toString());
    }
    if (providerResponse.getProviderData("resultCode") != null) {
      cardRegistration.setResultCode(providerResponse.getProviderData("resultCode").toString());
    }
    if (providerResponse.getProviderData("currency") != null) {
      cardRegistration.setCurrency(
          CurrencyISO.valueOf(providerResponse.getProviderData("currency").toString()));
    }
    if (providerResponse.getProviderData("status") != null) {
      cardRegistration.setStatus(providerResponse.getProviderData("status").toString());
    }
    if (providerResponse.getProviderData("cardType") != null) {
      cardRegistration.setCardType(
          CardType.valueOf(providerResponse.getProviderData("cardType").toString()));
    }
    cardRegistration.setReturnUrl(
        String.format(
            ConfigFactory.load().getString("mangopay.token.returnurl"),
            apiVersion,
            cardRegistration.getId()));
  }

  @Override
  public DepositCard getProviderDepositCard(
      String requestId, Account sessionAccount, String cardProviderId) {

    DepositCard depositCard = new DepositCard();

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.GETDEPOSITCARD);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.getProviderDepositCard(requestId, cardProviderId);
    logService.debug(
        requestId, "IN", "providerResponse", utilsService.prettyPrintObject(providerResponse));

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      mapDepositCard(depositCard, providerResponse);
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
    return depositCard;
  }

  private void mapDepositCard(DepositCard depositCard, ProviderResponse providerResponse) {

    if (providerResponse.getProviderData("active") != null) {
      depositCard.setActive(Boolean.valueOf(providerResponse.getProviderData("active").toString()));
    }

    if (providerResponse.getProviderData("alias") != null) {
      depositCard.setAlias(providerResponse.getProviderData("alias").toString());
    }

    if (providerResponse.getProviderData("cardProvider") != null) {
      depositCard.setCardProvider(providerResponse.getProviderData("cardProvider").toString());
    }

    if (providerResponse.getProviderData("cardType") != null) {
      depositCard.setCardType(
          CardType.valueOf(providerResponse.getProviderData("cardType").toString()));
    }

    if (providerResponse.getProviderData("country") != null) {
      depositCard.setCountry(providerResponse.getProviderData("country").toString());
    }

    if (providerResponse.getProviderData("currency") != null) {
      depositCard.setCurrency(
          CurrencyISO.valueOf(providerResponse.getProviderData("currency").toString()));
    }

    if (providerResponse.getProviderData("customTag") != null) {
      depositCard.setCustomTag(providerResponse.getProviderData("customTag").toString());
    }

    if (providerResponse.getProviderData("expirationDate") != null) {
      depositCard.setExpirationDate(providerResponse.getProviderData("expirationDate").toString());
    }

    if (providerResponse.getProviderData("fingerprint") != null) {
      depositCard.setFingerprint(providerResponse.getProviderData("fingerprint").toString());
    }

    if (providerResponse.getProviderData("providerId") != null) {
      depositCard.setProviderId(providerResponse.getProviderData("providerId").toString());
    }

    if (providerResponse.getProviderData("userProviderId") != null) {
      depositCard.setUserProviderId(providerResponse.getProviderData("userProviderId").toString());
    }

    if (providerResponse.getProviderData("validity") != null) {
      depositCard.setValidity(
          CardValidity.valueOf(providerResponse.getProviderData("validity").toString()));
    }
  }

  /**
   * Execute bank payment to provider
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @param requestBankPayment
   */
  private void executeProviderBankPayment(
      String requestId, Account account, BankCard bankCard, RequestBankPayment requestBankPayment) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.EXECUTEBANKPAYMENT);

    // register request to provider
    ProviderResponse providerResponse =
        providerService.executeProviderBankPayment(
            requestId,
            bankCard,
            requestBankPayment.getBeneficiaryName(),
            requestBankPayment.getCreditorIban(),
            requestBankPayment.getCreditorBic(),
            requestBankPayment.getPaymentAmount(),
            requestBankPayment.getReference());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  private void addProviderBankCardCurrency(
      String requestId, Account account, BankCard bankCard, String currency) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.ADDCARDCURRENCY);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.addProviderBankCardCurrency(requestId, bankCard, currency);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("cardCurrencies"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Update provider card information
   *
   * @param account
   * @param bankCard
   */
  private void updateProviderBankCard(String requestId, Account account, BankCard bankCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.UPDATEBANKCARD);

    // register record to provider
    ProviderResponse providerResponse = providerService.updateProviderBankCard(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.UPDATED)) {
      logService.debug(
          requestId, "L", "referenceId", providerResponse.getProviderData("referenceId"));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Create a bank card to provider
   *
   * @param account
   * @param bankCard
   */
  private void createProviderBankCard(String requestId, Account account, BankCard bankCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.CREATEBANKCARD);

    // register record to provider
    ProviderResponse providerResponse = providerService.createProviderBankCard(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      bankCard.setProviderId(providerResponse.getProviderData("cardHolderId").toString());
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Retrieve bank card wallet balance from provider and update local data
   *
   * @param account
   * @param bankCard
   * @param bankCardWallet
   */
  private void getProviderBankCardWallet(
      String requestId, Account account, BankCard bankCard, BankCardWallet bankCardWallet) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARDWALLET);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.getProviderBankCardWallet(requestId, bankCard, bankCardWallet);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // enrich bank card wallet object with data retrieved from server
      bankCardWallet
          .getAvailableBalance()
          .setValue(
              Integer.valueOf(providerResponse.getProviderData("availableBalance").toString()));
      bankCardWallet
          .getLedgerBalance()
          .setValue(Integer.valueOf(providerResponse.getProviderData("ledgerBalance").toString()));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Retrieve provider card number
   *
   * @param account
   * @param bankCard
   * @return
   */
  private String getProviderBankCardNumber(String requestId, Account account, BankCard bankCard) {

    String providerCardNumber = "";

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARDNUMBER);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.getProviderBankCardNumber(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // set card
      bankCard.setSensitiveCardNumber(
          "**** **** **** "
              .concat(providerResponse.getProviderData("cardNumber").toString().substring(12, 16)));

      // return full card number
      providerCardNumber = providerResponse.getProviderData("cardNumber").toString();
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // clean sensitive var
    providerResponse = null;

    // return response
    return providerCardNumber;
  }

  /**
   * Retrieve bank card expiry date from provider
   *
   * @param account
   * @param bankCard
   * @return
   */
  private String getProviderBankCardExpiryDate(
      String requestId, Account account, BankCard bankCard) {

    String providerCardExpDate = "";

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARDEXPDATE);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.getProviderBankCardExpiryDate(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // return exp date
      providerCardExpDate = providerResponse.getProviderData("expDate").toString();
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // clean sensitive var
    providerResponse = null;

    // return response
    return providerCardExpDate;
  }

  /**
   * Retrieve bank card cvv from provider
   *
   * @param account
   * @param bankCard
   * @return
   */
  private String getProviderBankCardCvv(String requestId, Account account, BankCard bankCard) {

    String providerCardCvv = "";

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARDCVV);

    // register record to provider
    ProviderResponse providerResponse = providerService.getProviderBankCardCvv(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // return cvv
      providerCardCvv = providerResponse.getProviderData("cvv").toString();
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    // clean sensitive var
    providerResponse = null;

    // return response
    return providerCardCvv;
  }

  /**
   * Retrieve bank card from provider and update local data
   *
   * @param account
   * @param bankCard
   */
  @SuppressWarnings("unchecked")
  private void getProviderBankCard(String requestId, Account account, BankCard bankCard) {

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(account, ProviderOperation.GETCARD);

    // register record to provider
    ProviderResponse providerResponse = providerService.getProviderBankCard(requestId, bankCard);

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.RETRIEVED)) {
      // enrich bank card object with data retrieved from server
      bankCard.setFirstName(providerResponse.getProviderData("firstName").toString());
      bankCard.setLastName(providerResponse.getProviderData("lastName").toString());
      bankCard.setEmbossName(providerResponse.getProviderData("embossName").toString());
      bankCard.setAddress1(providerResponse.getProviderData("address1").toString());
      bankCard.setAddress2(providerResponse.getProviderData("address2").toString());
      bankCard.setAddress3(providerResponse.getProviderData("address3").toString());
      bankCard.setAddress4(providerResponse.getProviderData("address4").toString());
      bankCard.setCity(providerResponse.getProviderData("city").toString());
      bankCard.setCountyName(providerResponse.getProviderData("countyName").toString());
      bankCard.setZipCode(providerResponse.getProviderData("zip").toString());
      bankCard.setCountryCode(providerResponse.getProviderData("countryCode").toString());
      bankCard.setPhone(providerResponse.getProviderData("phone").toString());
      bankCard.setEmail(providerResponse.getProviderData("emailAddr").toString());

      Map<String, String> udfFields =
          (Map<String, String>) providerResponse.getProviderData("udfFields");

      if (udfFields != null) {
        CardUserInfo cardUserInfo = new CardUserInfo();
        cardUserInfo.setOccupation(
            CardUserInfoOccupation.getEnumByLabel(
                udfFields.get(CardUserInfoOccupation.FIELD_NAME)));
        cardUserInfo.setPurpose(
            CardUserInfoPurpose.getEnumByLabel(udfFields.get(CardUserInfoPurpose.FIELD_NAME)));
        cardUserInfo.setMonthlyIncome(
            CardUserInfoMonthlyIncome.getEnumByLabel(
                udfFields.get(CardUserInfoMonthlyIncome.FIELD_NAME)));
        cardUserInfo.setEstate(
            CardUserInfoEstate.getEnumByLabel(udfFields.get(CardUserInfoEstate.FIELD_NAME)));
        cardUserInfo.setEmploymentStatus(
            CardUserInfoEmploymentStatus.getEnumByLabel(
                udfFields.get(CardUserInfoEmploymentStatus.FIELD_NAME)));

        if (cardUserInfo.getOccupation() != null
            || cardUserInfo.getPurpose() != null
            || cardUserInfo.getMonthlyIncome() != null
            || cardUserInfo.getEstate() != null
            || cardUserInfo.getEmploymentStatus() != null) {
          bankCard.setCardUserInfo(cardUserInfo);
        }
      }

      String expirationDateStr = providerResponse.getProviderData("expirationDate").toString();
      try {
        // Expiration date comes in format 2204 (end of April 2022)
        long expirationDate =
            LocalDateTime.ofInstant(
                    new SimpleDateFormat("yyMM").parse(expirationDateStr).toInstant(),
                    ZoneId.systemDefault())
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .minusSeconds(1)
                .toEpochSecond(ZoneOffset.UTC);
        bankCard.setExpirationDate(expirationDate);
      } catch (ParseException e) {
        logService.error(requestId, "L", "errors", e.getMessage());
        e.printStackTrace();
      }
      bankCard.setDob(
          providerResponse
              .getProviderData("dob")
              .toString()
              .substring(0, 2)
              .concat("/")
              .concat(providerResponse.getProviderData("dob").toString().substring(2, 4))
              .concat("/")
              .concat(providerResponse.getProviderData("dob").toString().substring(4, 8)));

      // card data bic / iban
      bankCard.setProviderBic(providerResponse.getProviderData("bic").toString());
      bankCard.setProviderIban(providerResponse.getProviderData("iban").toString());

      // get card list of currencies
      bankCard.setCurrencies((List<String>) providerResponse.getProviderData("currency"));

      // card type
      if (providerResponse.getProviderData("deliveryType").toString().equals("VC")) {
        bankCard.setType(BankCardType.VIRTUAL);
      } else {
        bankCard.setType(BankCardType.PHYSICAL);
      }

      switch (providerResponse.getProviderData("cardStatus").toString()) {
        case "0":
          bankCard.setStatus(BankCardStatus.ISSUED);
          break;

        case "1":
          bankCard.setStatus(BankCardStatus.OPEN);
          break;

        case "2":
          bankCard.setStatus(BankCardStatus.LOST);
          break;

        case "3":
          bankCard.setStatus(BankCardStatus.STOLEN);
          break;

        case "4":
          bankCard.setStatus(BankCardStatus.BLOCKED_PAYOUT);
          break;

        case "9":
          bankCard.setStatus(BankCardStatus.BLOCKED_FINAL);
          break;

        case "E":
          bankCard.setStatus(BankCardStatus.EXPIRED);
          break;

        case "Q":
          bankCard.setStatus(BankCardStatus.BLOCKED_PIN);
          break;

        case "C":
          bankCard.setStatus(BankCardStatus.BLOCKED_FRAUD);
          break;
      }
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }
  }

  /**
   * Map 2 address lines from LoLo framework standard address to a 4 lines address array to match
   * card providers requirements
   *
   * @param addressLine1
   * @param addressLine2
   * @return
   */
  private String[] map2AddressLinesTo4(String addressLine1, String addressLine2) {

    String[] addressArray = {" ", " ", " ", " "};

    if (StringUtils.isNotBlank(addressLine2)) {
      addressLine1 = addressLine1.concat(" ").concat(addressLine2);
    }

    if (addressLine1.length() > 30) {
      addressArray[0] = addressLine1.substring(0, 30);
      if (addressLine1.length() > 60) {
        addressArray[1] = addressLine1.substring(30, 60);
        if (addressLine1.length() > 95) {
          addressArray[2] = addressLine1.substring(60, 95);
          if (addressLine1.length() > 130) {
            addressArray[3] = addressLine1.substring(95, 130);
          } else {
            addressArray[3] = addressLine1.substring(95, addressLine1.length());
          }
        } else {
          addressArray[2] = addressLine1.substring(60, addressLine1.length());
        }
      } else {
        addressArray[1] = addressLine1.substring(30, addressLine1.length());
      }
    } else {
      addressArray[0] = addressLine1;
    }

    return addressArray;
  }

  @SuppressWarnings("unchecked")
  @Override
  public PaginatedList getUserDisputes(
      String requestId, Account sessionAccount, User user, int page, int pageSize) {

    // init return object
    PaginatedList paginatedList = new PaginatedList();

    // get financial provider
    ProviderService providerService =
        providerFactory.getProvider(sessionAccount, ProviderOperation.GETUSERSDISPUTES);

    // register record to provider
    ProviderResponse providerResponse =
        providerService.getProviderUserDisputes(requestId, user.getProviderId(), page, pageSize);

    logService.debug(requestId, "L", "providerResponse", Json.toJson(providerResponse).toString());

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.CREATED)) {
      paginatedList.setList(
          (List<? extends TableCollection>) providerResponse.getProviderData("disputes"));
      paginatedList.setPage(
          Long.parseLong(String.valueOf(providerResponse.getProviderData("page"))));
      paginatedList.setPageSize(
          Long.parseLong(String.valueOf(providerResponse.getProviderData("itemsPerPage"))));
      paginatedList.setTotalPages(
          Long.parseLong(String.valueOf(providerResponse.getProviderData("totalItems"))));
      paginatedList.setTotalRecords(
          Long.parseLong(String.valueOf(providerResponse.getProviderData("totalPages"))));
    }

    if (providerResponse.getProviderOperationStatus().equals(ProviderOperationStatus.ERROR)) {
      GenericRestException gre = new GenericRestException();
      gre.setResponseErrors(providerResponse.getProviderErrors());
      throw gre;
    }

    return paginatedList;
  }
}
