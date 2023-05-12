package ro.iss.lolopay.services.implementation;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mangopay.MangoPayApi;
import com.mangopay.core.Billing;
import com.mangopay.core.FilterDisputes;
import com.mangopay.core.Money;
import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.CountryIso;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.PayInExecutionType;
import com.mangopay.core.enumerations.SecureMode;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.entities.CardRegistration;
import com.mangopay.entities.Dispute;
import com.mangopay.entities.subentities.BankAccountDetailsIBAN;
import com.mangopay.entities.subentities.PayInExecutionDetailsDirect;
import com.mangopay.entities.subentities.PayInExecutionDetailsWeb;
import com.mangopay.entities.subentities.PayInPaymentDetailsCard;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.Json;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.BankAccountType;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.Birthplace;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.DepositAccountType;
import ro.iss.lolopay.models.classes.DisputeReason;
import ro.iss.lolopay.models.classes.DisputeReasonType;
import ro.iss.lolopay.models.classes.DisputeStatus;
import ro.iss.lolopay.models.classes.DisputeType;
import ro.iss.lolopay.models.classes.DocumentRejectReason;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.DocumentType;
import ro.iss.lolopay.models.classes.IncomeRange;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.SecurityInfo;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.TransactionType;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.BankAccountCA;
import ro.iss.lolopay.models.database.BankAccountGB;
import ro.iss.lolopay.models.database.BankAccountIBAN;
import ro.iss.lolopay.models.database.BankAccountOTHER;
import ro.iss.lolopay.models.database.BankAccountUS;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.database.DepositCard;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.database.PayOut;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.ApplicationErrorService;
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.MangoPayService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class MangoPayImplementation implements MangoPayService {
  private MangoPayApi api = null;

  private LogService logService;

  @Inject private UtilsService utilsService;

  @Inject ApplicationErrorService applicationErrorService;

  /** Log singleton creation moment */
  @Inject
  public MangoPayImplementation(LogService logService) {

    this.logService = logService;

    if (api == null) {
      api = new MangoPayApi();

      // configuration
      api.getConfig().setBaseUrl(ConfigFactory.load().getString("mangopay.baseUrl"));
      api.getConfig().setClientId(ConfigFactory.load().getString("mangopay.clientId"));
      api.getConfig().setClientPassword(ConfigFactory.load().getString("mangopay.clientPassword"));
      api.getConfig().setConnectTimeout(ConfigFactory.load().getInt("mangopay.connectionTimeout"));
      api.getConfig().setDebugMode(ConfigFactory.load().getBoolean("mangopay.debugMode"));
      api.getConfig().setReadTimeout(ConfigFactory.load().getInt("mangopay.readTimeout"));
    }
  }

  @Override
  public ProviderResponse createProviderNaturalUser(String requestId, User user) {

    logService.debug(requestId, "IN", "user", user.getId());
    ProviderResponse providerResponse = null;

    try {
      com.mangopay.entities.UserNatural newNaturalUser =
          (com.mangopay.entities.UserNatural)
              api.getUserApi().create(mapNaturalUser(requestId, user));
      logService.debug(requestId, "L", "newNaturalUser", newNaturalUser.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", newNaturalUser.getId());

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse saveProviderNaturalUser(String requestId, User user) {

    logService.debug(requestId, "IN", "user", user.getId());
    ProviderResponse providerResponse = null;

    try {
      // call api
      com.mangopay.entities.UserNatural updatedNaturalUser =
          (com.mangopay.entities.UserNatural)
              api.getUserApi().update(mapNaturalUser(requestId, user));
      logService.debug(requestId, "L", "updatedNaturalUser", updatedNaturalUser.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
      providerResponse.addProviderData("providerId", updatedNaturalUser.getId());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderLegalUser(String requestId, User legalUser) {

    logService.debug(requestId, "IN", "user", legalUser.getId());

    ProviderResponse providerResponse = null;

    try {
      com.mangopay.entities.UserLegal newLegalUser =
          (com.mangopay.entities.UserLegal) api.getUserApi().create(mapLegalUser(legalUser));
      logService.debug(requestId, "L", "newLegalUser", newLegalUser.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", newLegalUser.getId());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse saveProviderLegalUser(String requestId, User legalUser) {

    logService.debug(requestId, "IN", "legalUser", legalUser.getId());

    ProviderResponse providerResponse = null;

    try {
      // call api
      com.mangopay.entities.UserLegal updatedLegalUser =
          (com.mangopay.entities.UserLegal) api.getUserApi().update(mapLegalUser(legalUser));
      logService.debug(requestId, "L", "legalUser", legalUser.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
      providerResponse.addProviderData("providerId", updatedLegalUser.getId());

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderUser(String requestId, User user) {

    logService.debug(requestId, "IN", "user", user.getId());

    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.User mangoUser = null;

      if (user.getType().equals(UserType.NATURAL)) {
        mangoUser = api.getUserApi().getNatural(user.getProviderId());
        logService.debug(requestId, "L", "mangoUserNatural", mangoUser.getId());

        providerResponse.addProviderData(
            "firstName", ((com.mangopay.entities.UserNatural) mangoUser).getFirstName());
        providerResponse.addProviderData(
            "lastName", ((com.mangopay.entities.UserNatural) mangoUser).getLastName());
        providerResponse.addProviderData(
            "occupation", ((com.mangopay.entities.UserNatural) mangoUser).getOccupation());
        if (((com.mangopay.entities.UserNatural) mangoUser).getAddress() != null) {
          providerResponse.addProviderData(
              "addressLine1",
              ((com.mangopay.entities.UserNatural) mangoUser).getAddress().getAddressLine1());
          providerResponse.addProviderData(
              "addressLine2",
              ((com.mangopay.entities.UserNatural) mangoUser).getAddress().getAddressLine2());
          providerResponse.addProviderData(
              "city", ((com.mangopay.entities.UserNatural) mangoUser).getAddress().getCity());
          providerResponse.addProviderData(
              "county", ((com.mangopay.entities.UserNatural) mangoUser).getAddress().getRegion());
        }
      }

      if (user.getType().equals(UserType.LEGAL)) {
        mangoUser = api.getUserApi().getLegal(user.getProviderId());
        logService.debug(requestId, "L", "mangoUserLegal", mangoUser.getId());
      }

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

      // for the moment get only KYC level of an user, but this is to be improved with additional
      // fields if needed
      if (mangoUser.getKycLevel() != null) {
        switch (mangoUser.getKycLevel()) {
          case REGULAR:
            providerResponse.addProviderData("kycLevel", KYCLevel.VERIFIED);
            break;

          case LIGHT:
            providerResponse.addProviderData("kycLevel", KYCLevel.STANDARD);
            break;

          default:
            providerResponse.addProviderData("kycLevel", KYCLevel.STANDARD);
            break;
        }
      } else {
        providerResponse.addProviderData("kycLevel", KYCLevel.STANDARD);
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    logService.debug(requestId, "IN", "user", user.getId());

    ProviderResponse providerResponse = null;

    try {
      // call api
      com.mangopay.entities.BankAccount providerBankAccount =
          api.getUserApi()
              .createBankAccount(user.getProviderId(), mapBankAccount(user, bankAccount));

      logService.debug(requestId, "L", "providerBankAccount.id", providerBankAccount.getId());

      logService.debug(requestId, "L", "providerBankAccount.type", providerBankAccount.getType());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", providerBankAccount.getId());

      if (providerBankAccount
          .getType()
          .equals(com.mangopay.core.enumerations.BankAccountType.IBAN)) {
        if (providerBankAccount.getDetails() != null) {
          com.mangopay.entities.subentities.BankAccountDetailsIBAN bankAccountDetailsIBAN =
              (BankAccountDetailsIBAN) providerBankAccount.getDetails();
          providerResponse.addProviderData("bic", bankAccountDetailsIBAN.getBic());
          logService.debug(
              requestId, "L", "providerBankAccount.bic", bankAccountDetailsIBAN.getBic());
        }
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse deactivateProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "bankAccount", bankAccount.getId());

    ProviderResponse providerResponse = null;
    try {
      // get provide original bank account
      com.mangopay.entities.BankAccount mangoBankAccount =
          api.getUserApi().getBankAccount(user.getProviderId(), bankAccount.getProviderId());
      logService.debug(requestId, "L", "mangoBankAccount", mangoBankAccount.getId());

      mangoBankAccount.setActive(false);

      // update bank account activation to false
      api.getUserApi()
          .updateBankAccount(
              mangoBankAccount.getUserId(), mangoBankAccount, mangoBankAccount.getId());
      logService.debug(requestId, "L", "mangoBankAccount", "deactivated");

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderWallet(String requestId, User owner, Wallet wallet) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "wallet", wallet.getId());

    ProviderResponse providerResponse = null;

    try {
      com.mangopay.entities.Wallet newMangoWallet =
          api.getWalletApi().create(mapWallet(owner, wallet));
      logService.debug(requestId, "L", "newMangoWallet", newMangoWallet.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", newMangoWallet.getId());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderDocument(
      String requestId, Account account, Application application, User owner, Document document) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "document", document.getId());

    ProviderResponse providerResponse = null;
    try {
      com.mangopay.entities.KycDocument mangoKYCDocument =
          api.getUserApi()
              .createKycDocument(owner.getProviderId(), mapDocumentType(document.getType()));
      logService.debug(requestId, "L", "mangoKYCDocument", mangoKYCDocument.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", mangoKYCDocument.getId());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDocument(String requestId, User owner, Document document) {

    logService.debug(requestId, "IN", "user", owner.getId());
    logService.debug(requestId, "IN", "user", document.getId());

    ProviderResponse providerResponse = null;

    try {
      com.mangopay.entities.KycDocument mangoKYCDocument =
          api.getUserApi().getKycDocument(owner.getProviderId(), document.getProviderId());
      logService.debug(requestId, "L", "mangoKYCDocument", mangoKYCDocument.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

      if (mangoKYCDocument.getStatus() != null) {
        logService.debug(
            requestId, "L", "mangoKYCDocument.getStatus()", mangoKYCDocument.getStatus());

        switch (mangoKYCDocument.getStatus()) {
          case CREATED:
            providerResponse.addProviderData("status", DocumentStatus.CREATED);
            break;

          case VALIDATION_ASKED:
            providerResponse.addProviderData("status", DocumentStatus.VALIDATION_ASKED);
            break;

          case VALIDATED:
            providerResponse.addProviderData("status", DocumentStatus.VALIDATED);
            break;

          case REFUSED:
            providerResponse.addProviderData("status", DocumentStatus.REFUSED);
            break;

          case OUT_OF_DATE:
            providerResponse.addProviderData("status", DocumentStatus.OUT_OF_DATE);
            break;

          default:
            // TODO: this can be dangerous, if a cast is made later to enum type
            providerResponse.addProviderData("status", document.getStatus());
            break;
        }
      } else {
        // leave it as it was
        providerResponse.addProviderData("status", document.getStatus());
      }

      providerResponse.addProviderData(
          "rejectionReasonMessage", mangoKYCDocument.getRefusedReasonMessage());

      if (mangoKYCDocument.getRefusedReasonType() != null) {
        switch (mangoKYCDocument.getRefusedReasonType()) {
          case "DOCUMENT_UNREADABLE":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_UNREADABLE);
            break;
          case "DOCUMENT_NOT_ACCEPTED":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_NOT_ACCEPTED);
            break;
          case "DOCUMENT_HAS_EXPIRED":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_HAS_EXPIRED);
            break;
          case "DOCUMENT_INCOMPLETE":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_INCOMPLETE);
            break;
          case "DOCUMENT_MISSING":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_MISSING);
            break;
          case "DOCUMENT_DO_NOT_MATCH_USER_DATA":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_DO_NOT_MATCH_USER_DATA);
            break;
          case "DOCUMENT_DO_NOT_MATCH_ACCOUNT_DATA":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_DO_NOT_MATCH_ACCOUNT_DATA);
            break;
          case "SPECIFIC_CASE":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.SPECIFIC_CASE);
            break;
          case "DOCUMENT_FALSIFIED":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.DOCUMENT_FALSIFIED);
            break;
          case "UNDERAGE_PERSON":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.UNDERAGE_PERSON);
            break;
          case "OTHER":
            providerResponse.addProviderData("rejectionReasonType", DocumentRejectReason.OTHER);
            break;
          case "TRIGGER_PEPS":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.TRIGGER_PEPS);
            break;
          case "TRIGGER_SANCTIONS_LISTS":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.TRIGGER_SANCTIONS_LISTS);
            break;
          case "TRIGGER_INTERPOL":
            providerResponse.addProviderData(
                "rejectionReasonType", DocumentRejectReason.TRIGGER_INTERPOL);
            break;
          default:
            providerResponse.addProviderData(
                "rejectionReasonType", document.getRejectionReasonType());
            break;
        }
      } else {
        // leave it as it was
        providerResponse.addProviderData("rejectionReasonType", document.getRejectionReasonType());
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String fileContent) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "document", document.getId());
    logService.debug(requestId, "IN", "fileContent", fileContent.substring(0, 10));

    ProviderResponse providerResponse = null;
    try {

      api.getUserApi()
          .createKycPage(
              owner.getProviderId(), document.getProviderId(), Base64.decodeBase64(fileContent));
      logService.debug(requestId, "L", "documentPage", "created");

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse submitProviderDocument(String requestId, Document document) {

    logService.debug(requestId, "IN", "document", document.getId());

    ProviderResponse providerResponse = null;
    try {
      // get provider original document
      com.mangopay.entities.KycDocument mangoKYCDocument =
          api.getKycDocumentApi().getKycDocument(document.getProviderId());
      logService.debug(requestId, "L", "mangoKYCDocument", mangoKYCDocument.getId());

      mangoKYCDocument.setStatus(com.mangopay.core.enumerations.KycStatus.VALIDATION_ASKED);
      mangoKYCDocument.setTag(document.getCustomTag());

      // update provider original document
      api.getUserApi().updateKycDocument(mangoKYCDocument.getUserId(), mangoKYCDocument);
      logService.debug(requestId, "L", "updateKycDocument", "updated");

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderUboDeclaration(
      String requestId, Account account, Application application, User owner) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "owner providerID", owner.getProviderId());

    ProviderResponse providerResponse = null;
    try {
      com.mangopay.entities.UboDeclaration mangoUboDeclaration =
          api.getUboDeclarationApi().create(owner.getProviderId());
      logService.debug(requestId, "L", "uboDeclaration", mangoUboDeclaration.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", mangoUboDeclaration.getId());

      providerResponse.addProviderData("processedDate", mangoUboDeclaration.getProcessedDate());
      providerResponse.addProviderData("createdAt", mangoUboDeclaration.getCreationDate());
      providerResponse.addProviderData("status", String.valueOf(mangoUboDeclaration.getStatus()));
      providerResponse.addProviderData("message", mangoUboDeclaration.getMessage());
      providerResponse.addProviderData("reason", String.valueOf(mangoUboDeclaration.getReason()));
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);

      if (e instanceof ResponseException) {
        // the exception format from Mango is not understood by m3 php. And the exceptions from
        // Mangopay are garbage, they don't have error codes, onley error messages
        ResponseException mangoResponseException = (ResponseException) e;
        String errorKey = "";
        if ("You can not create a declaration because you already have a declaration in progress"
            .equalsIgnoreCase(mangoResponseException.getApiMessage())) {
          errorKey = ErrorMessage.ERROR_CREATEUBODECLARATION_IN_PROGRESS;
        } else {
          errorKey = ErrorMessage.ERROR_CREATEUBODECLARATION_INVALID_OPERATION;
          logService.error(
              requestId,
              "L",
              "unknonwn provider error message",
              mangoResponseException.getApiMessage());
          logService.error(
              requestId, "L", "unknown error, using default value for error code", errorKey);
        }

        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(applicationErrorService.getErrorCode(requestId, errorKey));
        responseError.setErrorDescription(errorKey);
        providerResponse.addProviderError(responseError);
      } else {
        providerResponse.addProviderError(e.getMessage());
      }
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    logService.debug(requestId, "IN", "user", owner.getId());
    logService.debug(requestId, "IN", "user", uboDeclaration.getId());

    ProviderResponse providerResponse = null;

    try {
      com.mangopay.entities.UboDeclaration mangoUboDeclaration =
          api.getUboDeclarationApi().get(owner.getProviderId(), uboDeclaration.getProviderId());

      logService.debug(requestId, "L", "mangoUboDeclaration", mangoUboDeclaration.getId());

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

      providerResponse.addProviderData("processedDate", mangoUboDeclaration.getProcessedDate());
      providerResponse.addProviderData("createdAt", mangoUboDeclaration.getCreationDate());
      providerResponse.addProviderData("status", String.valueOf(mangoUboDeclaration.getStatus()));
      providerResponse.addProviderData("message", mangoUboDeclaration.getMessage());
      providerResponse.addProviderData("reason", String.valueOf(mangoUboDeclaration.getReason()));
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse submitProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    logService.debug(requestId, "IN", "user", owner.getId());
    logService.debug(requestId, "IN", "owner providerID", owner.getProviderId());
    logService.debug(requestId, "IN", "uboDecaration", uboDeclaration.getId());

    ProviderResponse providerResponse = null;
    try {

      com.mangopay.entities.UboDeclaration mangoUboDeclaration =
          api.getUboDeclarationApi()
              .submitForValidation(owner.getProviderId(), uboDeclaration.getProviderId());
      logService.debug(requestId, "L", "submitProviderUboDeclaration", "submitted");

      providerResponse = new ProviderResponse();
      providerResponse.addProviderData("processedDate", mangoUboDeclaration.getProcessedDate());
      providerResponse.addProviderData("createdAt", mangoUboDeclaration.getCreationDate());
      providerResponse.addProviderData("status", String.valueOf(mangoUboDeclaration.getStatus()));
      providerResponse.addProviderData("message", mangoUboDeclaration.getMessage());
      providerResponse.addProviderData("reason", String.valueOf(mangoUboDeclaration.getReason()));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "uboDeclaration", uboDeclaration.getId());

    ProviderResponse providerResponse = null;
    try {

      // UboDeclaration uboDeclaration = api.getUboDeclarationApi().create(owner.getProviderId());
      com.mangopay.entities.Ubo mangoUbo =
          api.getUboDeclarationApi()
              .createUbo(owner.getProviderId(), uboDeclaration.getProviderId(), mapUbo(ubo));
      logService.debug(requestId, "L", "ubo", "created");

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("providerId", mangoUbo.getId());
      providerResponse.addProviderData("createdAt", mangoUbo.getCreationDate());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse updateProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo) {

    logService.debug(requestId, "IN", "owner", owner.getId());
    logService.debug(requestId, "IN", "uboDeclaration", uboDeclaration.getId());
    logService.debug(requestId, "IN", "uboId", ubo.getProviderId());

    ProviderResponse providerResponse = null;
    try {

      com.mangopay.entities.Ubo mangoUbo =
          api.getUboDeclarationApi()
              .updateUbo(owner.getProviderId(), uboDeclaration.getProviderId(), mapUbo(ubo));
      logService.debug(requestId, "L", "ubo", "updated");

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
      providerResponse.addProviderData("createdAt", mangoUbo.getCreationDate());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderPayIn(
      String requestId,
      Account account,
      Application application,
      User user,
      Wallet userWallet,
      PayIn payIn,
      Transfer payInFee) {

    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "userWallet", userWallet.getId());
    logService.debug(requestId, "IN", "payIn", payIn.getId());
    logService.debug(requestId, "IN", "payIn.secureMode", payIn.getSecureMode());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.PayIn providerPayIn =
          api.getPayInApi().create(mapPayIn(user, userWallet, payIn, payInFee));
      logService.debug(requestId, "L", "providerPayIn", Json.toJson(providerPayIn));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("payInId", providerPayIn.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerPayIn.getResultCode() != null ? providerPayIn.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerPayIn.getResultMessage() != null ? providerPayIn.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");

      PayInExecutionDetailsWeb payInExecutionDetailsWeb =
          (PayInExecutionDetailsWeb) providerPayIn.getExecutionDetails();
      providerResponse.addProviderData(
          "redirectUrl",
          (payInExecutionDetailsWeb.getRedirectUrl() != null
              ? payInExecutionDetailsWeb.getRedirectUrl()
              : ""));

      // test because fee is not mandatory, it might be missing
      if (payInFee != null) {
        providerResponse.addProviderData("payInFeeId", providerPayIn.getId());
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderDirectPayIn(
      String requestId,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      PayIn payIn,
      Transfer feeTransaction) {

    logService.debug(requestId, "IN", "user", user.getId());
    logService.debug(requestId, "IN", "userWallet", creditWallet.getId());
    logService.debug(requestId, "IN", "payIn", payIn.getId());
    logService.debug(requestId, "IN", "payIn.secureMode", payIn.getSecureMode());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.PayIn providerPayIn =
          api.getPayInApi()
              .create(mapDirectPayIn(depositCard, user, creditWallet, payIn, feeTransaction));
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("authorId", providerPayIn.getAuthorId());
      providerResponse.addProviderData("createdAt", providerPayIn.getCreationDate());
      providerResponse.addProviderData("creditedFunds", providerPayIn.getCreditedFunds());
      providerResponse.addProviderData("creditedUserId", providerPayIn.getCreditedUserId());
      providerResponse.addProviderData("creditedWalletId", providerPayIn.getCreditedWalletId());
      providerResponse.addProviderData("debitedFunds", providerPayIn.getDebitedFunds());
      providerResponse.addProviderData("executionDate", providerPayIn.getExecutionDate());
      providerResponse.addProviderData("executionDetails", providerPayIn.getExecutionDetails());
      providerResponse.addProviderData("executionType", providerPayIn.getExecutionType());
      providerResponse.addProviderData("fees", providerPayIn.getFees());
      providerResponse.addProviderData("id", providerPayIn.getId());
      providerResponse.addProviderData("nature", providerPayIn.getNature());
      providerResponse.addProviderData("paymentDetails", providerPayIn.getPaymentDetails());
      providerResponse.addProviderData("paymentType", providerPayIn.getPaymentType());
      providerResponse.addProviderData("resultCode", providerPayIn.getResultCode());
      providerResponse.addProviderData("resultMessage", providerPayIn.getResultMessage());
      providerResponse.addProviderData("status", providerPayIn.getStatus());
      providerResponse.addProviderData("customTag", providerPayIn.getTag());
      providerResponse.addProviderData("type", providerPayIn.getType());

      PayInExecutionDetailsDirect payInExecutionDetailsDirect =
          (PayInExecutionDetailsDirect) providerPayIn.getExecutionDetails();

      providerResponse.addProviderData(
          "secureModeRedirectUrl", payInExecutionDetailsDirect.getSecureModeRedirectUrl());
      providerResponse.addProviderData("cardId", payInExecutionDetailsDirect.getCardId());
      providerResponse.addProviderData(
          "securityInfo",
          String.valueOf(payInExecutionDetailsDirect.getSecurityInfo().getAvsResult()));

      PayInPaymentDetailsCard paymentDetails =
          (PayInPaymentDetailsCard) providerPayIn.getPaymentDetails();
      providerResponse.addProviderData(
          "statementDescriptor", paymentDetails.getStatementDescriptor());

      // test because fee is not mandatory, it might be missing
      if (feeTransaction != null) {
        providerResponse.addProviderData("payInFeeId", providerPayIn.getId());
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", Json.toJson(providerResponse));
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDepositCard(String requestId, String cardId) {

    logService.debug(requestId, "IN", "requestId", requestId);
    logService.debug(requestId, "IN", "cardId", cardId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Card providerCard = api.getCardApi().get(cardId);
      logService.debug(requestId, "L", "providerPayIn", providerCard.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("active", providerCard.isActive());
      providerResponse.addProviderData("alias", providerCard.getAlias());
      providerResponse.addProviderData("cardProvider", providerCard.getCardProvider());
      providerResponse.addProviderData("cardType", providerCard.getCardType());
      providerResponse.addProviderData("country", providerCard.getCountry());
      providerResponse.addProviderData("currency", providerCard.getCurrency());
      providerResponse.addProviderData("customTag", providerCard.getTag());
      providerResponse.addProviderData("expirationDate", providerCard.getExpirationDate());
      providerResponse.addProviderData("fingerprint", providerCard.getFingerprint());
      providerResponse.addProviderData("providerId", providerCard.getId());
      providerResponse.addProviderData("userProviderId", providerCard.getUserId());
      providerResponse.addProviderData("validity", providerCard.getValidity());

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse deactivateProviderDepositCard(String requestId, DepositCard depositCard) {

    logService.debug(requestId, "IN", "requestId", requestId);
    logService.debug(requestId, "IN", "depositCard", depositCard.getId());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      // construct object for the request
      com.mangopay.entities.Card requestCard = new com.mangopay.entities.Card();
      requestCard.setId(depositCard.getProviderId());

      com.mangopay.entities.Card providerCard = api.getCardApi().disable(requestCard);
      logService.debug(requestId, "L", "providerCard", Json.toJson(providerCard).toString());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
      providerResponse.addProviderData("active", providerCard.isActive());
      providerResponse.addProviderData("alias", providerCard.getAlias());
      providerResponse.addProviderData("cardProvider", providerCard.getCardProvider());
      providerResponse.addProviderData("cardType", providerCard.getCardType());
      providerResponse.addProviderData("country", providerCard.getCountry());
      providerResponse.addProviderData("currency", providerCard.getCurrency());
      providerResponse.addProviderData("customTag", providerCard.getTag());
      providerResponse.addProviderData("expirationDate", providerCard.getExpirationDate());
      providerResponse.addProviderData("fingerprint", providerCard.getFingerprint());
      providerResponse.addProviderData("providerId", providerCard.getId());
      providerResponse.addProviderData("userProviderId", providerCard.getUserId());
      providerResponse.addProviderData("validity", providerCard.getValidity());

    } catch (Exception e) {

      ResponseException responseException = (ResponseException) e;
      logService.error(
          requestId, "L", "responseException getApiMessage", responseException.getApiMessage());
      logService.error(
          requestId, "L", "responseException getMessage", responseException.getMessage());
      logService.error(
          requestId,
          "L",
          "responseException errorStack",
          ExceptionUtils.getStackTrace(responseException));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(responseException.getApiMessage());
    }

    logService.debug(requestId, "OUT", "response", Json.toJson(providerResponse).toString());
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      Transfer transferFee,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet) {

    logService.debug(requestId, "IN", "transfer", transfer.getId());
    logService.debug(requestId, "IN", "transferFee", transferFee);
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "creditUser", creditUser.getId());
    logService.debug(requestId, "IN", "creditWallet", creditWallet.getId());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Transfer providerTransfer =
          api.getTransferApi()
              .create(
                  mapTransfer(
                      creditUser, creditWallet, debitUser, debitWallet, transfer, transferFee));
      logService.debug(requestId, "L", "providerTransfer", providerTransfer.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("transferId", providerTransfer.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerTransfer.getResultCode() != null ? providerTransfer.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerTransfer.getResultMessage() != null ? providerTransfer.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");

      // test because fee is not mandatory, it might be missing
      if (transferFee != null) {
        providerResponse.addProviderData("transferFeeId", providerTransfer.getId());
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderPayOut(
      String requestId,
      Account account,
      Application application,
      BankAccount bankAccount,
      User debitUser,
      Wallet debitWallet,
      PayOut payOut,
      Transfer payOutFee) {

    logService.debug(requestId, "IN", "bankAccount", bankAccount.getId());
    logService.debug(requestId, "IN", "debitUser", debitUser.getId());
    logService.debug(requestId, "IN", "debitWallet", debitWallet.getId());
    logService.debug(requestId, "IN", "payOut", payOut.getId());
    logService.debug(requestId, "IN", "payOutFee", payOutFee);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.PayOut providerPayOut =
          api.getPayOutApi()
              .create(mapPayOut(bankAccount, debitUser, debitWallet, payOut, payOutFee));
      logService.debug(requestId, "L", "providerPayOut", providerPayOut.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("payOutId", providerPayOut.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerPayOut.getResultCode() != null ? providerPayOut.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerPayOut.getResultMessage() != null ? providerPayOut.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");

      // test because fee is not mandatory, it might be missing
      if (payOutFee != null) {
        providerResponse.addProviderData("payOutFeeId", providerPayOut.getId());
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderTransferRefund(
      String requestId,
      Refund refund,
      Transfer refundFee,
      Transfer originalTransfer,
      User originalDebitUser) {

    logService.debug(requestId, "IN", "refund", refund.getId());
    logService.debug(requestId, "IN", "refundFee", refundFee);
    logService.debug(requestId, "IN", "originalTransfer", originalTransfer.getId());
    logService.debug(requestId, "IN", "originalDebitUser", originalDebitUser.getId());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Refund providerRefundTransaction = new com.mangopay.entities.Refund();
      providerRefundTransaction.setAuthorId(originalDebitUser.getProviderId());
      providerRefundTransaction.setTag(refund.getCustomTag());

      com.mangopay.entities.Refund providerRefund =
          api.getTransferApi()
              .createRefund(originalTransfer.getProviderId(), providerRefundTransaction);
      logService.debug(requestId, "L", "providerRefund", providerRefund.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("refundId", providerRefund.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerRefund.getResultCode() != null ? providerRefund.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerRefund.getResultMessage() != null ? providerRefund.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");
      providerResponse.addProviderData("refundReasonMessage", "");

      if (providerRefund.getRefundReason() != null
          && providerRefund.getRefundReason().getRefundReasonMessage() != null) {
        providerResponse.addProviderData(
            "refundReasonMessage", providerRefund.getRefundReason().getRefundReasonMessage());
      }

      // test because fee is not mandatory, it might be missing
      if (refundFee != null) {
        providerResponse.addProviderData("refundFeeId", providerRefund.getId());
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.PayIn providerPayIn = api.getPayInApi().get(providerId);
      logService.debug(requestId, "L", "providerPayIn", Json.toJson(providerPayIn).toString());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("payInId", providerPayIn.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerPayIn.getResultCode() != null ? providerPayIn.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerPayIn.getResultMessage() != null ? providerPayIn.getResultMessage() : ""));

      providerResponse.addProviderData("securityInfo", String.valueOf(SecurityInfo.NO_CHECK));
      if (providerPayIn.getExecutionType() == PayInExecutionType.DIRECT) {
        PayInExecutionDetailsDirect payInExecutionDetailsDirect =
            (PayInExecutionDetailsDirect) providerPayIn.getExecutionDetails();
        if (payInExecutionDetailsDirect.getSecurityInfo() != null) {
          if (payInExecutionDetailsDirect.getSecurityInfo().getAvsResult() != null) {
            providerResponse.addProviderData(
                "securityInfo",
                String.valueOf(payInExecutionDetailsDirect.getSecurityInfo().getAvsResult()));
          }
        }
      }

      providerResponse.addProviderData("externalReference", "");

      if (providerPayIn.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("payInStatus", TransactionStatus.CREATED);
      } else if (providerPayIn.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("payInStatus", TransactionStatus.FAILED);
      } else if (providerPayIn.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("payInStatus", TransactionStatus.SUCCEEDED);
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderTransferStatus(String requestId, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Transfer providerTransfer = api.getTransferApi().get(providerId);
      logService.debug(requestId, "L", "providerTransfer", providerTransfer.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("transferId", providerTransfer.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerTransfer.getResultCode() != null ? providerTransfer.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerTransfer.getResultMessage() != null ? providerTransfer.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");

      if (providerTransfer.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("transferStatus", TransactionStatus.CREATED);
      } else if (providerTransfer.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("transferStatus", TransactionStatus.FAILED);
      } else if (providerTransfer.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("transferStatus", TransactionStatus.SUCCEEDED);
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderPayOutStatus(String requestId, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.PayOut providerPayOut = api.getPayOutApi().get(providerId);
      logService.debug(requestId, "L", "providerPayOut", providerPayOut.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("payOutId", providerPayOut.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerPayOut.getResultCode() != null ? providerPayOut.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerPayOut.getResultMessage() != null ? providerPayOut.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");
      if (providerPayOut.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("payOutStatus", TransactionStatus.CREATED);
      } else if (providerPayOut.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("payOutStatus", TransactionStatus.FAILED);
      } else if (providerPayOut.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("payOutStatus", TransactionStatus.SUCCEEDED);
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderSettlement(String requestId, String settlementId) {

    logService.debug(requestId, "IN", "settlementId", settlementId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Settlement settlement =
          api.getSettlementApi().getSettlement(settlementId);

      logService.debug(requestId, "L", "providerRefund", settlement.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("Id", settlement.getId());
      providerResponse.addProviderData("Tag", settlement.getTag());
      providerResponse.addProviderData("CreationDate", settlement.getCreationDate());

      providerResponse.addProviderData(
          "ResultCode", (settlement.getResultCode() != null ? settlement.getResultCode() : ""));
      providerResponse.addProviderData(
          "ResultMessage",
          (settlement.getResultMessage() != null ? settlement.getResultMessage() : ""));

      providerResponse.addProviderData(
          "DebitedFundsAmount", settlement.getDebitedFunds().getAmount());
      providerResponse.addProviderData(
          "DebitedFundsCurrency", settlement.getDebitedFunds().getCurrency());

      providerResponse.addProviderData("FeesAmount", settlement.getFees().getAmount());
      providerResponse.addProviderData("FeesCurrency", settlement.getFees().getCurrency());

      providerResponse.addProviderData(
          "CreditedFundsAmount", settlement.getCreditedFunds().getAmount());
      providerResponse.addProviderData(
          "CreditedFundsCurrency", settlement.getCreditedFunds().getCurrency());

      providerResponse.addProviderData("AuthorId", settlement.getAuthorId());
      providerResponse.addProviderData("CreditedUserId", settlement.getCreditedUserId());

      if (settlement.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("Status", TransactionStatus.CREATED);
      } else if (settlement.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("Status", TransactionStatus.FAILED);
      } else if (settlement.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("Status", TransactionStatus.SUCCEEDED);
      }

      providerResponse.addProviderData("ExecutionDate", settlement.getExecutionDate());

      providerResponse.addProviderData("CreditedWalletId", settlement.getCreditedWalletId());
      providerResponse.addProviderData("DebitedWalletId", settlement.getDebitedWalletId());
      providerResponse.addProviderData("RepudiationId", settlement.getRepudiationId());
      providerResponse.addProviderData("Type", settlement.getType());
      providerResponse.addProviderData("Nature", settlement.getNature());
      providerResponse.addProviderData("ExternalReference", "");
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderWithdrawRefund(String requestId, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Refund depositRefund = api.getRefundApi().get(providerId);
      logService.debug(requestId, "L", "providerRefund", depositRefund.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("Id", depositRefund.getId());
      providerResponse.addProviderData("Tag", depositRefund.getTag());

      providerResponse.addProviderData("CreditedWalletId", depositRefund.getCreditedWalletId());
      providerResponse.addProviderData(
          "DebitedFundsAmount", depositRefund.getDebitedFunds().getAmount());
      providerResponse.addProviderData(
          "DebitedFundsCurrency", depositRefund.getDebitedFunds().getCurrency());
      providerResponse.addProviderData("FeesAmount", depositRefund.getFees().getAmount());
      providerResponse.addProviderData("FeesCurrency", depositRefund.getFees().getCurrency());
      providerResponse.addProviderData(
          "ResultCode",
          (depositRefund.getResultCode() != null ? depositRefund.getResultCode() : ""));
      providerResponse.addProviderData(
          "ResultMessage",
          (depositRefund.getResultMessage() != null ? depositRefund.getResultMessage() : ""));
      providerResponse.addProviderData("CreationDate", depositRefund.getCreationDate());
      providerResponse.addProviderData("ExecutionDate", depositRefund.getExecutionDate());
      providerResponse.addProviderData("ExternalReference", "");
      providerResponse.addProviderData(
          "RefundReasonMessage", depositRefund.getRefundReason().getRefundReasonMessage());
      providerResponse.addProviderData(
          "RefundReasonType", depositRefund.getRefundReason().getRefundReasonType());
      providerResponse.addProviderData(
          "InitialTransactionId", depositRefund.getInitialTransactionId());
      providerResponse.addProviderData(
          "InitialTransactionType", depositRefund.getInitialTransactionType());

      if (depositRefund.getRefundReason() != null
          && depositRefund.getRefundReason().getRefundReasonMessage() != null) {
        providerResponse.addProviderData(
            "RefundReasonMessage", depositRefund.getRefundReason().getRefundReasonMessage());
      }

      if (depositRefund.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("Status", TransactionStatus.CREATED);
      } else if (depositRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("Status", TransactionStatus.FAILED);
      } else if (depositRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("Status", TransactionStatus.SUCCEEDED);
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDepositRefund(String requestId, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Refund depositRefund = api.getRefundApi().get(providerId);
      logService.debug(requestId, "L", "providerRefund", depositRefund.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("Id", depositRefund.getId());
      providerResponse.addProviderData("Tag", depositRefund.getTag());

      providerResponse.addProviderData("DebitedWalletId", depositRefund.getDebitedWalletId());
      providerResponse.addProviderData(
          "CreditedFundsAmount", depositRefund.getCreditedFunds().getAmount());
      providerResponse.addProviderData(
          "CreditedFundsCurrency", depositRefund.getCreditedFunds().getCurrency());
      providerResponse.addProviderData("FeesAmount", depositRefund.getFees().getAmount());
      providerResponse.addProviderData("FeesCurrency", depositRefund.getFees().getCurrency());
      providerResponse.addProviderData(
          "ResultCode",
          (depositRefund.getResultCode() != null ? depositRefund.getResultCode() : ""));
      providerResponse.addProviderData(
          "ResultMessage",
          (depositRefund.getResultMessage() != null ? depositRefund.getResultMessage() : ""));
      providerResponse.addProviderData("CreationDate", depositRefund.getCreationDate());
      providerResponse.addProviderData("ExecutionDate", depositRefund.getExecutionDate());
      providerResponse.addProviderData("ExternalReference", "");
      providerResponse.addProviderData(
          "RefundReasonMessage", depositRefund.getRefundReason().getRefundReasonMessage());
      providerResponse.addProviderData(
          "RefundReasonType", depositRefund.getRefundReason().getRefundReasonType());
      providerResponse.addProviderData(
          "InitialTransactionId", depositRefund.getInitialTransactionId());
      providerResponse.addProviderData(
          "InitialTransactionType", depositRefund.getInitialTransactionType());

      if (depositRefund.getRefundReason() != null
          && depositRefund.getRefundReason().getRefundReasonMessage() != null) {
        providerResponse.addProviderData(
            "RefundReasonMessage", depositRefund.getRefundReason().getRefundReasonMessage());
      }

      if (depositRefund.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("Status", TransactionStatus.CREATED);
      } else if (depositRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("Status", TransactionStatus.FAILED);
      } else if (depositRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("Status", TransactionStatus.SUCCEEDED);
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", Json.toJson(providerResponse).toString());
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderTransferRefundStatus(String requestId, String providerId) {

    logService.debug(requestId, "IN", "providerId", providerId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Refund providerRefund = api.getRefundApi().get(providerId);
      logService.debug(requestId, "L", "providerRefund", providerRefund.getId());

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("refundId", providerRefund.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerRefund.getResultCode() != null ? providerRefund.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerRefund.getResultMessage() != null ? providerRefund.getResultMessage() : ""));
      providerResponse.addProviderData("externalReference", "");
      providerResponse.addProviderData("refundReasonMessage", "");

      if (providerRefund.getRefundReason() != null
          && providerRefund.getRefundReason().getRefundReasonMessage() != null) {
        providerResponse.addProviderData(
            "refundReasonMessage", providerRefund.getRefundReason().getRefundReasonMessage());
      }

      if (providerRefund.getStatus() == com.mangopay.core.enumerations.TransactionStatus.CREATED) {
        providerResponse.addProviderData("transferRefundStatus", TransactionStatus.CREATED);
      } else if (providerRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.FAILED) {
        providerResponse.addProviderData("transferRefundStatus", TransactionStatus.FAILED);
      } else if (providerRefund.getStatus()
          == com.mangopay.core.enumerations.TransactionStatus.SUCCEEDED) {
        providerResponse.addProviderData("transferRefundStatus", TransactionStatus.SUCCEEDED);
      }
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", providerResponse);
    return providerResponse;
  }

  @Override
  public ProviderResponse createCardRegistrations(
      String requestId, User user, RequestCreateCardRegistration requestCreateCardRegistration) {

    logService.debug(requestId, "IN", "providerId", requestId);
    logService.debug(requestId, "IN", "user.getProviderId", user.getProviderId());

    // create response
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      CardRegistration cardRegistration = new CardRegistration();
      cardRegistration.setUserId(user.getProviderId());
      cardRegistration.setCurrency(
          CurrencyIso.valueOf(requestCreateCardRegistration.getCurrency()));

      CardRegistration cardRegistrationInstance =
          api.getCardRegistrationApi().create(cardRegistration);

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
      providerResponse.addProviderData("id", cardRegistrationInstance.getId());
      providerResponse.addProviderData("customTag", cardRegistrationInstance.getTag());
      providerResponse.addProviderData("createdAt", cardRegistrationInstance.getCreationDate());
      providerResponse.addProviderData("userId", cardRegistrationInstance.getUserId());
      providerResponse.addProviderData("accessKey", cardRegistrationInstance.getAccessKey());
      providerResponse.addProviderData(
          "preregistrationData", cardRegistrationInstance.getPreregistrationData());
      providerResponse.addProviderData(
          "cardRegistrationUrl", cardRegistrationInstance.getCardRegistrationUrl());
      providerResponse.addProviderData("cardId", cardRegistrationInstance.getCardId());
      providerResponse.addProviderData(
          "registrationData", cardRegistrationInstance.getRegistrationData());
      providerResponse.addProviderData("resultCode", cardRegistrationInstance.getResultCode());
      providerResponse.addProviderData("currency", cardRegistrationInstance.getCurrency());
      providerResponse.addProviderData("status", cardRegistrationInstance.getStatus());
      providerResponse.addProviderData("cardType", cardRegistrationInstance.getCardType());
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }
    return providerResponse;
  }

  @Override
  public ProviderResponse updateCardRegistrations(
      String requestId,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration) {

    logService.debug(requestId, "IN", "providerId", requestId);

    // create response
    ProviderResponse providerResponse = new ProviderResponse();
    try {
      CardRegistration cardRegistration = new CardRegistration();
      cardRegistration.setId(cardRegistrationId);
      if (requestUpdateCardRegistration.getTag() != null) {
        cardRegistration.setTag(requestUpdateCardRegistration.getTag());
      }

      if (requestUpdateCardRegistration.getRegistrationData() != null) {
        cardRegistration.setRegistrationData(requestUpdateCardRegistration.getRegistrationData());
      }

      CardRegistration cardRegistrationInstance =
          api.getCardRegistrationApi().update(cardRegistration);
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
      providerResponse.addProviderData("id", cardRegistrationInstance.getId());
      providerResponse.addProviderData("customTag", cardRegistrationInstance.getTag());
      providerResponse.addProviderData("createdAt", cardRegistrationInstance.getCreationDate());
      providerResponse.addProviderData("userId", cardRegistrationInstance.getUserId());
      providerResponse.addProviderData("accessKey", cardRegistrationInstance.getAccessKey());
      providerResponse.addProviderData(
          "preregistrationData", cardRegistrationInstance.getPreregistrationData());
      providerResponse.addProviderData(
          "cardRegistrationUrl", cardRegistrationInstance.getCardRegistrationUrl());
      providerResponse.addProviderData("cardId", cardRegistrationInstance.getCardId());
      providerResponse.addProviderData(
          "registrationData", cardRegistrationInstance.getRegistrationData());
      providerResponse.addProviderData("resultCode", cardRegistrationInstance.getResultCode());
      providerResponse.addProviderData("currency", cardRegistrationInstance.getCurrency());
      providerResponse.addProviderData("status", cardRegistrationInstance.getStatus());
      providerResponse.addProviderData("cardType", cardRegistrationInstance.getCardType());

    } catch (Exception e) {
      ResponseException responseException = (ResponseException) e;
      logService.error(
          requestId, "L", "responseException getApiMessage", responseException.getApiMessage());
      logService.error(
          requestId, "L", "responseException getMessage", responseException.getMessage());
      logService.error(
          requestId,
          "L",
          "responseException errorStack",
          ExceptionUtils.getStackTrace(responseException));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(responseException.getApiMessage());
    }
    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderBankCard(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCard(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse upgradeProviderBankCard(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse changeStatusProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    return null;
  }

  @Override
  public ProviderResponse addProviderBankCardCurrency(
      String requestId, BankCard bankCard, String currency) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCardWallet(
      String requestId, BankCard bankCard, BankCardWallet bankCardWallet) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCardWalletTransaction(
      String requestId, BankCard bankCard, TransactionDate transactionDate) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCardNumber(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCardExpiryDate(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse getProviderBankCardCvv(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse providerTransferToBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    return null;
  }

  @Override
  public ProviderResponse providerTransferFromBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    return null;
  }

  @Override
  public ProviderResponse sendProviderPin(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse lockUnlockProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    return null;
  }

  @Override
  public ProviderResponse updateProviderBankCard(String requestId, BankCard bankCard) {

    return null;
  }

  @Override
  public ProviderResponse executeProviderBankPayment(
      String requestId,
      BankCard bankCard,
      String beneficiaryName,
      String creditorIBAN,
      String creditorBIC,
      Integer paymentAmount,
      String reference) {

    return null;
  }

  @Override
  public ProviderResponse getProviderCurrencyFxQuote(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    return null;
  }

  @Override
  public ProviderResponse replaceProviderCard(String requestId, BankCard bankCard, String reason) {

    return null;
  }

  @Override
  public ProviderResponse executeProviderCurrencyFXTrade(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    return null;
  }

  private com.mangopay.core.Address mapAddress(Address address) {

    if (address != null) {
      com.mangopay.core.Address newAddress = new com.mangopay.core.Address();
      newAddress.setAddressLine1(address.getAddressLine1());
      newAddress.setAddressLine2(address.getAddressLine2());
      newAddress.setCity(address.getCity());
      newAddress.setRegion(address.getCounty());
      if (address.getCountry() != null) {
        newAddress.setCountry(
            com.mangopay.core.enumerations.CountryIso.valueOf(address.getCountry().toString()));
      } else {
        newAddress.setCountry(null);
      }
      newAddress.setPostalCode(address.getPostalCode());
      return newAddress;
    } else {
      return null;
    }
  }

  private com.mangopay.entities.BankAccount mapBankAccount(User user, BankAccount bankAccount) {

    com.mangopay.entities.BankAccount newBankAccount = new com.mangopay.entities.BankAccount();
    newBankAccount.setUserId(user.getProviderId());
    newBankAccount.setTag(bankAccount.getCustomTag());
    newBankAccount.setOwnerName(bankAccount.getOwnerName());
    newBankAccount.setOwnerAddress(mapAddress(bankAccount.getOwnerAddress()));

    if (bankAccount.getType().equals(BankAccountType.IBAN)) {
      newBankAccount.setType(com.mangopay.core.enumerations.BankAccountType.IBAN);

      com.mangopay.entities.subentities.BankAccountDetailsIBAN bankAccountDetailsIBAN =
          new com.mangopay.entities.subentities.BankAccountDetailsIBAN();
      bankAccountDetailsIBAN.setIban(((BankAccountIBAN) bankAccount).getIban());
      bankAccountDetailsIBAN.setBic(((BankAccountIBAN) bankAccount).getBic());
      newBankAccount.setDetails(bankAccountDetailsIBAN);
    }

    if (bankAccount.getType().equals(BankAccountType.GB)) {
      newBankAccount.setType(com.mangopay.core.enumerations.BankAccountType.GB);

      com.mangopay.entities.subentities.BankAccountDetailsGB bankAccountDetailsGB =
          new com.mangopay.entities.subentities.BankAccountDetailsGB();
      bankAccountDetailsGB.setAccountNumber(((BankAccountGB) bankAccount).getAccountNumber());
      bankAccountDetailsGB.setSortCode(((BankAccountGB) bankAccount).getSortCode());
      newBankAccount.setDetails(bankAccountDetailsGB);
    }

    if (bankAccount.getType().equals(BankAccountType.US)) {
      newBankAccount.setType(com.mangopay.core.enumerations.BankAccountType.US);

      com.mangopay.entities.subentities.BankAccountDetailsUS bankAccountDetailsUS =
          new com.mangopay.entities.subentities.BankAccountDetailsUS();
      bankAccountDetailsUS.setAba(((BankAccountUS) bankAccount).getAba());
      bankAccountDetailsUS.setAccountNumber(((BankAccountUS) bankAccount).getAccountNumber());
      if ((((BankAccountUS) bankAccount).getDepositAccountType() != null)
          && ((BankAccountUS) bankAccount)
              .getDepositAccountType()
              .equals(DepositAccountType.CHECKING)) {
        bankAccountDetailsUS.setDepositAccountType(
            com.mangopay.core.enumerations.DepositAccountType.CHECKING);
      }

      if ((((BankAccountUS) bankAccount).getDepositAccountType() != null)
          && ((BankAccountUS) bankAccount)
              .getDepositAccountType()
              .equals(DepositAccountType.SAVINGS)) {
        bankAccountDetailsUS.setDepositAccountType(
            com.mangopay.core.enumerations.DepositAccountType.SAVINGS);
      }
      newBankAccount.setDetails(bankAccountDetailsUS);
    }

    if (bankAccount.getType().equals(BankAccountType.CA)) {
      newBankAccount.setType(com.mangopay.core.enumerations.BankAccountType.CA);

      com.mangopay.entities.subentities.BankAccountDetailsCA bankAccountDetailsCA =
          new com.mangopay.entities.subentities.BankAccountDetailsCA();
      bankAccountDetailsCA.setAccountNumber(((BankAccountCA) bankAccount).getAccountNumber());
      bankAccountDetailsCA.setBankName(((BankAccountCA) bankAccount).getBankName());
      bankAccountDetailsCA.setInstitutionNumber(
          ((BankAccountCA) bankAccount).getInstitutionNumber());
      bankAccountDetailsCA.setBranchCode(((BankAccountCA) bankAccount).getBranchCode());
      newBankAccount.setDetails(bankAccountDetailsCA);
    }

    if (bankAccount.getType().equals(BankAccountType.OTHER)) {
      newBankAccount.setType(com.mangopay.core.enumerations.BankAccountType.OTHER);

      com.mangopay.entities.subentities.BankAccountDetailsOTHER bankAccountDetailsOTHER =
          new com.mangopay.entities.subentities.BankAccountDetailsOTHER();
      bankAccountDetailsOTHER.setAccountNumber(((BankAccountOTHER) bankAccount).getAccountNumber());
      if (((BankAccountOTHER) bankAccount).getCountry() != null) {
        bankAccountDetailsOTHER.setCountry(
            com.mangopay.core.enumerations.CountryIso.valueOf(
                ((BankAccountOTHER) bankAccount).getCountry().toString()));
      }
      bankAccountDetailsOTHER.setBic(((BankAccountOTHER) bankAccount).getBic());
      newBankAccount.setDetails(bankAccountDetailsOTHER);
    }

    return newBankAccount;
  }

  private com.mangopay.entities.PayOut mapPayOut(
      BankAccount bankAccount,
      User debitUser,
      Wallet debitWallet,
      PayOut mainTransaction,
      Transfer feeTransaction) {

    com.mangopay.entities.PayOut payOut = new com.mangopay.entities.PayOut();

    // set debit side
    payOut.setAuthorId(debitUser.getProviderId());
    payOut.setDebitedWalletId(debitWallet.getProviderId());

    // set amount
    if (feeTransaction == null) {
      // if there is no fee in place

      // debit funds will be the entire transaction amount
      payOut.setDebitedFunds(mapMoney(mainTransaction.getAmount()));

      // we create 0 Money object
      com.mangopay.core.Money feeMoney = new com.mangopay.core.Money();
      feeMoney.setAmount(0);
      feeMoney.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(debitWallet.getCurrency().toString()));

      // we set the fee to money 0
      payOut.setFees(feeMoney);
    } else {
      // if we have fees

      // calculate the amount use main transaction + fee transaction, as per MangoPay rules
      com.mangopay.core.Money transactionAmount = new com.mangopay.core.Money();
      transactionAmount.setAmount(
          mainTransaction.getAmount().getValue() + feeTransaction.getAmount().getValue());
      transactionAmount.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              mainTransaction.getAmount().getCurrency().toString()));

      // set debit funds with the sum of both transactions
      payOut.setDebitedFunds(transactionAmount);

      // we set the fee to money 0
      payOut.setFees(mapMoney(feeTransaction.getAmount()));
    }

    // set details
    com.mangopay.entities.subentities.PayOutPaymentDetailsBankWire payOutPaymentDetailsBankWire =
        new com.mangopay.entities.subentities.PayOutPaymentDetailsBankWire();
    payOutPaymentDetailsBankWire.setBankAccountId(bankAccount.getProviderId());
    payOut.setMeanOfPaymentDetails(payOutPaymentDetailsBankWire);
    payOut.setTag(mainTransaction.getCustomTag());

    // return payout
    return payOut;
  }

  private com.mangopay.entities.Transfer mapTransfer(
      User creditUser,
      Wallet creditWallet,
      User debitUser,
      Wallet debitWallet,
      Transfer mainTransaction,
      Transfer feeTransaction) {

    com.mangopay.entities.Transfer transfer = new com.mangopay.entities.Transfer();

    // set debit side
    transfer.setAuthorId(debitUser.getProviderId());
    transfer.setDebitedWalletId(debitWallet.getProviderId());

    // set credit side
    transfer.setCreditedUserId(creditUser.getProviderId());
    transfer.setCreditedWalletId(creditWallet.getProviderId());

    // set amount
    if (feeTransaction == null) {
      // if there is no fee in place

      // debit funds will be the entire transaction amount
      transfer.setDebitedFunds(mapMoney(mainTransaction.getAmount()));

      // we create 0 Money object
      com.mangopay.core.Money feeMoney = new com.mangopay.core.Money();
      feeMoney.setAmount(0);
      feeMoney.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              creditWallet.getCurrency().toString()));

      // we set the fee to money 0
      transfer.setFees(feeMoney);
    } else {
      // if we have fees

      // calculate the amount use main transaction + fee transaction, as per MangoPay rules
      com.mangopay.core.Money transactionAmount = new com.mangopay.core.Money();
      transactionAmount.setAmount(
          mainTransaction.getAmount().getValue() + feeTransaction.getAmount().getValue());
      transactionAmount.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              mainTransaction.getAmount().getCurrency().toString()));

      // set debit funds with the sum of both transactions
      transfer.setDebitedFunds(transactionAmount);

      // we set the fee to money 0
      transfer.setFees(mapMoney(feeTransaction.getAmount()));
    }

    // set custom tag
    transfer.setTag(mainTransaction.getCustomTag());

    return transfer;
  }

  private com.mangopay.entities.PayIn mapPayIn(
      User user, Wallet userWallet, PayIn payInTransaction, Transfer feeTransaction) {

    com.mangopay.entities.PayIn payIn = new com.mangopay.entities.PayIn();
    payIn.setAuthorId(user.getProviderId());
    payIn.setCreditedUserId(user.getProviderId());
    payIn.setCreditedWalletId(userWallet.getProviderId());

    if (feeTransaction == null) {
      // if there is no fee in place

      // debit funds will be the entire transaction amount
      payIn.setDebitedFunds(mapMoney(payInTransaction.getAmount()));

      // we create 0 Money object
      com.mangopay.core.Money feeMoney = new com.mangopay.core.Money();
      feeMoney.setAmount(0);
      feeMoney.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(userWallet.getCurrency().toString()));

      // we set the fee to money 0
      payIn.setFees(feeMoney);
    } else {
      // if we have fees

      // calculate the amount use main transaction + fee transaction, as per MangoPay rules
      com.mangopay.core.Money transactionAmount = new com.mangopay.core.Money();
      transactionAmount.setAmount(payInTransaction.getAmount().getValue());
      transactionAmount.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              payInTransaction.getAmount().getCurrency().toString()));

      // set debit funds with the sum of both transactions
      payIn.setDebitedFunds(transactionAmount);

      // we set the fee to money 0
      payIn.setFees(mapMoney(feeTransaction.getAmount()));
    }

    com.mangopay.entities.subentities.PayInExecutionDetailsWeb payInExecutionDetailsWeb =
        new com.mangopay.entities.subentities.PayInExecutionDetailsWeb();
    payInExecutionDetailsWeb.setReturnUrl(ConfigFactory.load().getString("mangopay.returnUrl"));

    payInExecutionDetailsWeb.setCulture(
        com.mangopay.core.enumerations.CultureCode.valueOf(
            payInTransaction.getCulture().toString()));

    payInExecutionDetailsWeb.setSecureMode(
        com.mangopay.core.enumerations.SecureMode.valueOf(
            payInTransaction.getSecureMode().toString()));

    payInExecutionDetailsWeb.setTemplateUrl(payInTransaction.getTemplateURL());

    com.mangopay.entities.subentities.PayInPaymentDetailsCard payInPaymentDetailsCard =
        new com.mangopay.entities.subentities.PayInPaymentDetailsCard();

    payInPaymentDetailsCard.setCardType(
        com.mangopay.core.enumerations.CardType.valueOf(payInTransaction.getCardType().toString()));

    payInPaymentDetailsCard.setStatementDescriptor(payInTransaction.getStatementDescriptor());

    // set execution and payment details
    payIn.setExecutionDetails(payInExecutionDetailsWeb);
    payIn.setPaymentDetails(payInPaymentDetailsCard);

    // set custom tag
    payIn.setTag(payInTransaction.getCustomTag());
    return payIn;
  }

  private com.mangopay.entities.PayIn mapDirectPayIn(
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      PayIn payInTransaction,
      Transfer feeTransaction) {

    com.mangopay.entities.PayIn payIn = new com.mangopay.entities.PayIn();
    payIn.setAuthorId(user.getProviderId());
    payIn.setCreditedUserId(user.getProviderId());
    payIn.setCreditedWalletId(creditWallet.getProviderId());

    if (feeTransaction == null) {
      // if there is no fee in place

      // debit funds will be the entire transaction amount
      payIn.setDebitedFunds(mapMoney(payInTransaction.getAmount()));

      // we create 0 Money object
      com.mangopay.core.Money feeMoney = new com.mangopay.core.Money();
      feeMoney.setAmount(0);
      feeMoney.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              creditWallet.getCurrency().toString()));

      // we set the fee to money 0
      payIn.setFees(feeMoney);
    } else {
      // if we have fees

      // calculate the amount use main transaction + fee transaction, as per MangoPay rules
      com.mangopay.core.Money transactionAmount = new com.mangopay.core.Money();
      transactionAmount.setAmount(payInTransaction.getAmount().getValue());
      transactionAmount.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(
              payInTransaction.getAmount().getCurrency().toString()));

      // set debit funds with the sum of both transactions
      payIn.setDebitedFunds(transactionAmount);

      // we set the fee to money 0
      payIn.setFees(mapMoney(feeTransaction.getAmount()));
    }

    PayInExecutionDetailsDirect payInExecutionDetailsDirect = new PayInExecutionDetailsDirect();
    payInExecutionDetailsDirect.setCardId(depositCard.getProviderId());
    payInExecutionDetailsDirect.setSecureMode(
        SecureMode.valueOf(payInTransaction.getSecureMode().toString()));

    payInExecutionDetailsDirect.setSecureModeReturnUrl(payInTransaction.getSecureModeReturnUrl());

    if (payInTransaction.getBilling() != null) {

      // create address for billing
      com.mangopay.core.Address address = new com.mangopay.core.Address();
      address.setAddressLine1(payInTransaction.getBilling().getAddressLine1());
      address.setAddressLine2(payInTransaction.getBilling().getAddressLine2());
      address.setCity(payInTransaction.getBilling().getCity());
      address.setCountry(CountryIso.valueOf(payInTransaction.getBilling().getCountry().toString()));
      address.setPostalCode(payInTransaction.getBilling().getPostalCode());
      address.setRegion(payInTransaction.getBilling().getCounty());

      // construct billing
      Billing billing = new Billing();
      billing.setAddress(address);

      // set billing
      payInExecutionDetailsDirect.setBilling(billing);
    }

    // set execution details
    payIn.setExecutionDetails(payInExecutionDetailsDirect);

    PayInPaymentDetailsCard paymentDetails = new PayInPaymentDetailsCard();
    paymentDetails.setCardId(depositCard.getId());
    paymentDetails.setCardType(
        com.mangopay.core.enumerations.CardType.valueOf(depositCard.getCardType().toString()));
    paymentDetails.setStatementDescriptor(payInTransaction.getStatementDescriptor());
    payIn.setPaymentDetails(paymentDetails);

    // set custom tag
    payIn.setTag(payInTransaction.getCustomTag());

    return payIn;
  }

  private com.mangopay.entities.Wallet mapWallet(User owner, Wallet wallet) {

    com.mangopay.entities.Wallet mangoWallet = new com.mangopay.entities.Wallet();

    // set id
    if (wallet.getProviderId() != null) {
      mangoWallet.setId(wallet.getProviderId());
    }

    mangoWallet.setBalance(mapMoney(wallet.getBalance()));
    mangoWallet.setCurrency(
        com.mangopay.core.enumerations.CurrencyIso.valueOf(wallet.getCurrency().toString()));
    mangoWallet.setDescription(wallet.getDescription());
    mangoWallet.setFundsType(com.mangopay.core.enumerations.FundsType.DEFAULT);

    ArrayList<String> walletOwners = new ArrayList<String>();
    walletOwners.add(owner.getProviderId());

    mangoWallet.setOwners(walletOwners);
    mangoWallet.setTag(wallet.getCustomTag());

    return mangoWallet;
  }

  private com.mangopay.core.enumerations.KycDocumentType mapDocumentType(
      DocumentType documentType) {

    com.mangopay.core.enumerations.KycDocumentType mangoDocType =
        com.mangopay.core.enumerations.KycDocumentType.NotSpecified;
    if (documentType != null) {
      if (documentType.equals(DocumentType.IDENTITY_PROOF)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.IDENTITY_PROOF;
      }
      if (documentType.equals(DocumentType.REGISTRATION_PROOF)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.REGISTRATION_PROOF;
      }
      if (documentType.equals(DocumentType.ADDRESS_PROOF)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.ADDRESS_PROOF;
      }
      if (documentType.equals(DocumentType.ARTICLES_OF_ASSOCIATION)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.ARTICLES_OF_ASSOCIATION;
      }
      if (documentType.equals(DocumentType.SHAREHOLDER_DECLARATION)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.SHAREHOLDER_DECLARATION;
      }
      if (documentType.equals(DocumentType.COMPANY_STATUTE)) {
        mangoDocType = com.mangopay.core.enumerations.KycDocumentType.NotSpecified;
      }
    }
    return mangoDocType;
  }

  private com.mangopay.entities.Ubo mapUbo(Ubo ubo) {

    com.mangopay.entities.Ubo mangoUbo = new com.mangopay.entities.Ubo();
    mangoUbo.setAddress(mapAddress(ubo.getAddress()));
    mangoUbo.setBirthday(ubo.getBirthday());

    com.mangopay.entities.Birthplace mangoBirthplace = new com.mangopay.entities.Birthplace();
    Birthplace birthPlace = ubo.getBirthplace();
    mangoBirthplace.setCity(birthPlace.getCity());
    if (birthPlace.getCountry() != null) {
      mangoBirthplace.setCountry(
          com.mangopay.core.enumerations.CountryIso.valueOf(birthPlace.getCountry().toString()));
    } else {
      mangoBirthplace.setCountry(null);
    }
    mangoUbo.setBirthplace(mangoBirthplace);
    mangoUbo.setFirstName(ubo.getFirstName());
    mangoUbo.setId(ubo.getProviderId());
    mangoUbo.setLastName(ubo.getLastName());
    mangoUbo.setNationality(
        com.mangopay.core.enumerations.CountryIso.valueOf(ubo.getNationality().toString()));

    return mangoUbo;
  }

  private com.mangopay.entities.UserLegal mapLegalUser(User user) {

    com.mangopay.entities.UserLegal mangoLegalUser = new com.mangopay.entities.UserLegal();

    // set id
    if (user.getProviderId() != null) {
      mangoLegalUser.setId(user.getProviderId());
    }

    mangoLegalUser.setEmail(user.getCompanyEmail());
    mangoLegalUser.setCompanyNumber(user.getCompanyRegistrationNumber());

    // if address is set
    if (null != user.getCompanyAddress()) {
      com.mangopay.core.Address headquarterAddress = new com.mangopay.core.Address();
      headquarterAddress.setAddressLine1(user.getCompanyAddress().getAddressLine1());
      headquarterAddress.setAddressLine2(user.getCompanyAddress().getAddressLine2());
      headquarterAddress.setCity(user.getCompanyAddress().getCity());
      if (null != user.getCompanyAddress().getCountry()) {
        headquarterAddress.setCountry(
            com.mangopay.core.enumerations.CountryIso.valueOf(
                user.getCompanyAddress().getCountry().toString()));
      }

      headquarterAddress.setPostalCode(user.getCompanyAddress().getPostalCode());
      headquarterAddress.setRegion(user.getCompanyAddress().getCounty());

      // set company address
      mangoLegalUser.setHeadquartersAddress(headquarterAddress);
    }

    // if kyc level is set
    if (null != user.getKycLevel()) {
      if (user.getKycLevel().equals(KYCLevel.STANDARD)) {
        mangoLegalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.LIGHT);
      }
      if (user.getKycLevel().equals(KYCLevel.VERIFIED)) {
        mangoLegalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.REGULAR);
      }
      if (user.getKycLevel().equals(KYCLevel.HIGHRISK)) {
        mangoLegalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.LIGHT);
      }
    } else {
      mangoLegalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.NotSpecified);
    }

    // company type
    if (user.getCompanyType() != null) {
      if (user.getCompanyType().equals(CompanyType.BUSINESS)) {
        mangoLegalUser.setLegalPersonType(com.mangopay.core.enumerations.LegalPersonType.BUSINESS);
      }
      if (user.getCompanyType().equals(CompanyType.ORGANIZATION)) {
        mangoLegalUser.setLegalPersonType(
            com.mangopay.core.enumerations.LegalPersonType.ORGANIZATION);
      }
      if (user.getCompanyType().equals(CompanyType.SOLETRADER)) {
        mangoLegalUser.setLegalPersonType(
            com.mangopay.core.enumerations.LegalPersonType.SOLETRADER);
      }
    } else {
      mangoLegalUser.setLegalPersonType(
          com.mangopay.core.enumerations.LegalPersonType.NotSpecified);
    }

    // if address is set (legal representative)
    if (null != user.getAddress()) {
      com.mangopay.core.Address legalRepresentativeAddress = new com.mangopay.core.Address();
      legalRepresentativeAddress.setAddressLine1(user.getAddress().getAddressLine1());
      legalRepresentativeAddress.setAddressLine2(user.getAddress().getAddressLine2());
      legalRepresentativeAddress.setCity(user.getAddress().getCity());
      if (null != user.getAddress().getCountry()) {
        legalRepresentativeAddress.setCountry(
            com.mangopay.core.enumerations.CountryIso.valueOf(
                user.getAddress().getCountry().toString()));
      }

      legalRepresentativeAddress.setPostalCode(user.getAddress().getPostalCode());
      legalRepresentativeAddress.setRegion(user.getAddress().getCounty());

      // set company address
      mangoLegalUser.setLegalRepresentativeAddress(legalRepresentativeAddress);
    }

    mangoLegalUser.setLegalRepresentativeBirthday(user.getBirthDate());

    if (user.getCountryOfResidence() != null) {
      mangoLegalUser.setLegalRepresentativeCountryOfResidence(
          com.mangopay.core.enumerations.CountryIso.valueOf(
              user.getCountryOfResidence().toString()));
    } else {
      mangoLegalUser.setLegalRepresentativeCountryOfResidence(null);
    }

    mangoLegalUser.setLegalRepresentativeEmail(user.getEmail());
    mangoLegalUser.setLegalRepresentativeFirstName(user.getFirstName());
    mangoLegalUser.setLegalRepresentativeLastName(user.getLastName());

    if (user.getNationality() != null) {
      mangoLegalUser.setLegalRepresentativeNationality(
          com.mangopay.core.enumerations.CountryIso.valueOf(user.getNationality().toString()));
    } else {
      mangoLegalUser.setLegalRepresentativeNationality(null);
    }

    mangoLegalUser.setName(user.getCompanyName());
    mangoLegalUser.setTag(user.getCustomTag());

    return mangoLegalUser;
  }

  private com.mangopay.entities.UserNatural mapNaturalUser(String requestId, User user)
      throws Exception {

    com.mangopay.entities.UserNatural mangoNaturalUser = new com.mangopay.entities.UserNatural();

    // set id as well
    if (user.getProviderId() != null) {
      mangoNaturalUser.setId(user.getProviderId());
    }

    // if address is set
    if (null != user.getAddress()) {
      com.mangopay.core.Address naturalUserAddress = new com.mangopay.core.Address();
      naturalUserAddress.setAddressLine1(user.getAddress().getAddressLine1());
      naturalUserAddress.setAddressLine2(user.getAddress().getAddressLine2());
      naturalUserAddress.setCity(user.getAddress().getCity());
      if (null != user.getAddress().getCountry()) {
        naturalUserAddress.setCountry(
            com.mangopay.core.enumerations.CountryIso.valueOf(
                user.getAddress().getCountry().toString()));
      }

      naturalUserAddress.setPostalCode(user.getAddress().getPostalCode());
      naturalUserAddress.setRegion(user.getAddress().getCounty());

      // set address
      mangoNaturalUser.setAddress(naturalUserAddress);
    }

    mangoNaturalUser.setBirthday(user.getBirthDate());
    mangoNaturalUser.setCountryOfResidence(
        com.mangopay.core.enumerations.CountryIso.valueOf(user.getCountryOfResidence().toString()));
    mangoNaturalUser.setEmail(user.getEmail());
    mangoNaturalUser.setFirstName(user.getFirstName());
    mangoNaturalUser.setLastName(user.getLastName());

    if (null != user.getIncomeRange()) {
      if (user.getIncomeRange().equals(IncomeRange.BELOW_18K)) {
        mangoNaturalUser.setIncomeRange(1);
      }
      if (user.getIncomeRange().equals(IncomeRange.BELOW_30K)) {
        mangoNaturalUser.setIncomeRange(2);
      }
      if (user.getIncomeRange().equals(IncomeRange.BELOW_50K)) {
        mangoNaturalUser.setIncomeRange(3);
      }
      if (user.getIncomeRange().equals(IncomeRange.BELOW_80K)) {
        mangoNaturalUser.setIncomeRange(4);
      }
      if (user.getIncomeRange().equals(IncomeRange.BELOW_120K)) {
        mangoNaturalUser.setIncomeRange(5);
      }
      if (user.getIncomeRange().equals(IncomeRange.ABOVE_120K)) {
        mangoNaturalUser.setIncomeRange(6);
      }
    }

    if (null != user.getKycLevel()) {
      if (user.getKycLevel().equals(KYCLevel.STANDARD)) {
        mangoNaturalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.LIGHT);
      }
      if (user.getKycLevel().equals(KYCLevel.VERIFIED)) {
        mangoNaturalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.REGULAR);
      }
      if (user.getKycLevel().equals(KYCLevel.HIGHRISK)) {
        mangoNaturalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.LIGHT);
      }
    } else {
      mangoNaturalUser.setKycLevel(com.mangopay.core.enumerations.KycLevel.NotSpecified);
    }

    mangoNaturalUser.setNationality(
        com.mangopay.core.enumerations.CountryIso.valueOf(user.getNationality().toString()));
    mangoNaturalUser.setOccupation(user.getOccupation());
    mangoNaturalUser.setPersonType(com.mangopay.core.enumerations.PersonType.NATURAL);
    mangoNaturalUser.setTag(user.getCustomTag());
    logService.debug(
        requestId, "L", "mangoNaturalUser", utilsService.prettyPrintObject(mangoNaturalUser));

    return mangoNaturalUser;
  }

  private com.mangopay.core.Money mapMoney(Amount amount) {

    com.mangopay.core.Money money = new com.mangopay.core.Money();

    // set balance
    if (amount != null) {
      money.setAmount(amount.getValue());
      money.setCurrency(
          com.mangopay.core.enumerations.CurrencyIso.valueOf(amount.getCurrency().toString()));
    } else {
      money.setAmount(0);
      money.setCurrency(com.mangopay.core.enumerations.CurrencyIso.NotSpecified);
    }

    return money;
  }

  @Override
  public ProviderResponse createPayInRefund(
      String requestId, String payInId, Refund refund, Transfer refundFee, User user) {

    logService.debug(requestId, "IN", "payInId", payInId);
    logService.debug(requestId, "IN", "refund", Json.toJson(refund).toString());
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Refund providerRefundPayInTransaction =
          mapRefundPayIn(requestId, refund, refundFee, user);

      com.mangopay.entities.Refund providerRefundPayIn =
          api.getPayInApi().createRefund(payInId, providerRefundPayInTransaction);
      logService.error(
          requestId, "L", "providerRefundPayIn", Json.toJson(providerRefundPayIn).toString());
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

      providerResponse.addProviderData("refundId", providerRefundPayIn.getId());
      providerResponse.addProviderData(
          "resultCode",
          (providerRefundPayIn.getResultCode() != null ? providerRefundPayIn.getResultCode() : ""));
      providerResponse.addProviderData(
          "resultMessage",
          (providerRefundPayIn.getResultMessage() != null
              ? providerRefundPayIn.getResultMessage()
              : ""));
      providerResponse.addProviderData("externalReference", "");
      providerResponse.addProviderData("refundReasonMessage", "");

      if (providerRefundPayIn.getRefundReason() != null
          && providerRefundPayIn.getRefundReason().getRefundReasonMessage() != null) {
        providerResponse.addProviderData(
            "refundReasonMessage", providerRefundPayIn.getRefundReason().getRefundReasonMessage());
      }

      // test because fee is not mandatory, it might be missing
      if (refundFee != null) {
        providerResponse.addProviderData("refundFeeId", providerRefundPayIn.getId());
      }

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }

    logService.debug(requestId, "OUT", "response", Json.toJson(providerResponse).toString());
    return providerResponse;
  }

  private com.mangopay.entities.Refund mapRefundPayIn(
      String requestId, Refund refund, Transfer refundFee, User user) {

    logService.debug(requestId, "IN", "start", requestId);
    com.mangopay.entities.Refund providerRefund = new com.mangopay.entities.Refund();

    providerRefund.setAuthorId(user.getProviderId());

    if (refund.getCustomTag() != null) {
      providerRefund.setTag(refund.getCustomTag());
    }

    if (refund.getAmount() != null) {
      Money debitedFunds = new Money();
      debitedFunds.setAmount(refund.getAmount().getValue());
      debitedFunds.setCurrency(CurrencyIso.valueOf(refund.getAmount().getCurrency().toString()));
      providerRefund.setDebitedFunds(debitedFunds);

      Money fees = new Money();
      if (refundFee != null && refundFee.getAmount() != null) {
        fees.setAmount(refundFee.getAmount().getValue());
      } else {
        fees.setAmount(0);
      }
      fees.setCurrency(CurrencyIso.valueOf(refund.getAmount().getCurrency().toString()));
      providerRefund.setFees(fees);
    }

    logService.debug(requestId, "OUT", "end", Json.toJson(providerRefund).toString());
    return providerRefund;
  }

  @Override
  public ProviderResponse getProviderUserDisputes(
      String requestId, String userId, int page, int itemsPerPage) {

    logService.debug(requestId, "IN", "start", requestId);
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      Pagination pagination = new Pagination();
      pagination.setItemsPerPage(itemsPerPage);
      pagination.setPage(page);
      FilterDisputes filters = new FilterDisputes();
      Sorting sorting = new Sorting();
      sorting.addField("CreationDate", SortDirection.desc);
      List<Dispute> userDisputes =
          api.getDisputeApi().getDisputesForUser(userId, pagination, filters, sorting);

      Logger.of(this.getClass())
          .debug("userDisputes: {}", utilsService.prettyPrintObject(userDisputes));
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

      providerResponse.addProviderData("disputes", mapGetDisputesResponse(userDisputes));
      providerResponse.addProviderData("page", pagination.getPage());
      providerResponse.addProviderData("itemsPerPage", pagination.getItemsPerPage());
      providerResponse.addProviderData("totalItems", pagination.getTotalItems());
      providerResponse.addProviderData("totalPages", pagination.getTotalPages());

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }
    return providerResponse;
  }

  private List<ro.iss.lolopay.models.database.Dispute> mapGetDisputesResponse(
      List<Dispute> userDisputes) {

    List<ro.iss.lolopay.models.database.Dispute> disputes = new ArrayList<>();
    for (Dispute dispute : userDisputes) {
      ro.iss.lolopay.models.database.Dispute tempDispute =
          new ro.iss.lolopay.models.database.Dispute();
      tempDispute.setContestDeadlineDate(dispute.getContestDeadlineDate());
      Amount contestedFunds = new Amount();
      contestedFunds.setCurrency(
          CurrencyISO.valueOf(dispute.getContestedFunds().getCurrency().toString()));
      contestedFunds.setValue(dispute.getContestedFunds().getAmount());
      tempDispute.setContestedFunds(contestedFunds);
      tempDispute.setCreatedAt(dispute.getCreationDate());
      Amount disputedFunds = new Amount();
      disputedFunds.setCurrency(
          CurrencyISO.valueOf(dispute.getDisputedFunds().getCurrency().toString()));
      disputedFunds.setValue(dispute.getDisputedFunds().getAmount());
      tempDispute.setDisputedFunds(disputedFunds);
      DisputeReason disputeReason = new DisputeReason();
      disputeReason.setDisputeReasonMessage(dispute.getDisputeReason().getDisputeReasonMessage());
      disputeReason.setDisputeReasonType(
          DisputeReasonType.valueOf(dispute.getDisputeReason().getDisputeReasonType().toString()));
      tempDispute.setDisputeReason(disputeReason);
      tempDispute.setDisputeType(DisputeType.valueOf(dispute.getDisputeType().toString()));
      tempDispute.setInitialTransactionId(dispute.getInitialTransactionId());
      tempDispute.setInitialTransactionType(
          TransactionType.valueOf(dispute.getInitialTransactionType().toString()));
      tempDispute.setRepudiationId(dispute.getRepudiationId());
      tempDispute.setResultCode(dispute.getResultCode());
      tempDispute.setResultMessage(dispute.getResultMessage());
      tempDispute.setStatus(DisputeStatus.valueOf(dispute.getStatus().toString()));
      tempDispute.setStatusMessage(dispute.getStatusMessage());
      tempDispute.setTag(dispute.getTag());

      disputes.add(tempDispute);
    }
    return disputes;
  }

  @Override
  public ProviderResponse getProviderWallet(String requestId, Wallet wallet) {
    logService.debug(requestId, "IN", "start", requestId);
    ProviderResponse providerResponse = new ProviderResponse();

    try {
      com.mangopay.entities.Wallet providerWallet = api.getWalletApi().get(wallet.getProviderId());
      logService.debug(
          requestId, "L", "providerWallet", utilsService.prettyPrintObject(providerWallet));
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
      providerResponse.addProviderData("balance", providerWallet.getBalance().getAmount());

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.addProviderError(e.getMessage());
    }
    return providerResponse;
  }
}
