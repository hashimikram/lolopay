package ro.iss.lolopay.services.implementation;

import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import play.libs.Json;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.classes.TransactionStatus;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
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
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;
import ro.iss.lolopay.services.definition.InternalProviderService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.NotificationService;

@Singleton
public class InternalProviderImplementation implements InternalProviderService {

  @Inject LogService logService;

  @Inject NotificationService notificationService;

  @Inject PayInService payInService;

  // TODO review code for internal provider - make it work

  public InternalProviderImplementation() {

    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public ProviderResponse createProviderTransferRefund(
      String requestId,
      Refund refund,
      Transfer refundFee,
      Transfer originalTransfer,
      User originalDebitUser) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

    providerResponse.addProviderData("refundId", refund.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("refundReasonMessage", "");

    // test because fee is not mandatory, it might be missing
    if (refundFee != null) {
      providerResponse.addProviderData("refundFeeId", refundFee.getId());
    }

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderNaturalUser(String requestId, User user)
      throws GenericRestException {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", user.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse saveProviderNaturalUser(String requestId, User user) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("providerId", user.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderLegalUser(String requestId, User legalUser) {

    logService.debug(requestId, "IN", "requestId", requestId);

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", legalUser.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse saveProviderLegalUser(String requestId, User legalUser) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("providerId", legalUser.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse deactivateProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("providerId", bankAccount.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderWallet(String requestId, User owner, Wallet wallet) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", wallet.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderDocument(
      String requestId, Account account, Application application, User owner, Document document) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", document.getId());

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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", document.getId());

    // notify self
    notificationService.notifySelf(
        requestId, account, application, NotificationType.KYC_SUCCEEDED, document.getId());
    return providerResponse;
  }

  @Override
  public ProviderResponse submitProviderDocument(String requestId, Document document) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("providerId", document.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderUboDeclaration(
      String requestId, Account account, Application application, User owner) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("status", DocumentStatus.VALIDATED);
    providerResponse.addProviderData("rejectionReasonMessage", null);
    providerResponse.addProviderData("rejectionReasonType", null);

    return providerResponse;
  }

  @Override
  public ProviderResponse submitProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("providerId", uboDeclaration.getId());

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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

    providerResponse.addProviderData("payInId", payIn.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("redirectUrl", "https://docs.mangopay.com");

    // test because fee is not mandatory, it might be missing
    if (payInFee != null) {
      providerResponse.addProviderData("payInFeeId", payInFee.getId());
    }

    // notify self
    notificationService.notifySelf(
        requestId, account, application, NotificationType.PAYIN_NORMAL_SUCCEEDED, payIn.getId());

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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

    providerResponse.addProviderData("payInId", payIn.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("redirectUrl", "https://docs.mangopay.com");

    // test because fee is not mandatory, it might be missing
    if (feeTransaction != null) {
      providerResponse.addProviderData("payInFeeId", feeTransaction.getId());
    }
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("payInId", providerId);
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("payInStatus", TransactionStatus.SUCCEEDED);
    providerResponse.addProviderData("securityInfo", "NO_CHECK");

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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("transferId", transfer.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");

    // test because fee is not mandatory, it might be missing
    if (transferFee != null) {
      providerResponse.addProviderData("transferFeeId", transferFee.getId());
    }

    // transfer.setCreatedAt(createdAt);
    // notify self
    notificationService.notifySelf(
        requestId,
        account,
        application,
        NotificationType.TRANSFER_NORMAL_SUCCEEDED,
        transfer.getId());

    logService.debug(requestId, "L", "transfer", Json.toJson(transfer).toString());
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderTransferStatus(String requestId, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("transferId", providerId);
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("transferStatus", TransactionStatus.SUCCEEDED);

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderTransferRefundStatus(String requestId, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("refundId", providerId);
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderPayOutStatus(String requestId, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("payOutId", providerId);
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("payOutStatus", TransactionStatus.SUCCEEDED);

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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("payOutId", payOut.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");

    // test because fee is not mandatory, it might be missing
    if (payOutFee != null) {
      providerResponse.addProviderData("payOutFeeId", payOutFee.getId());
    }

    // notify self
    notificationService.notifySelf(
        requestId, account, application, NotificationType.PAYOUT_NORMAL_SUCCEEDED, payOut.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("providerId", bankAccount.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderUser(String requestId, User user) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("providerId", user.getId());
    if (user.getType().equals(UserType.NATURAL)) {
      providerResponse.addProviderData("firstName", user.getFirstName());
      providerResponse.addProviderData("lastName", user.getLastName());
      providerResponse.addProviderData("occupation", user.getOccupation());
      if (user.getAddress() != null) {
        providerResponse.addProviderData("addressLine1", user.getAddress().getAddressLine1());
        providerResponse.addProviderData("addressLine2", user.getAddress().getAddressLine2());
        providerResponse.addProviderData("city", user.getAddress().getCity());
        providerResponse.addProviderData("county", user.getAddress().getCounty());
      }
    }
    providerResponse.addProviderData("kycLevel", KYCLevel.VERIFIED);

    return providerResponse;
  }

  @Override
  public ProviderResponse createProviderBankCard(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("cardHolderId", bankCard.getId());

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCard(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("accountBaseCurrency", "accountBaseCurrency");
    providerResponse.addProviderData("cardType", "cardType");
    providerResponse.addProviderData("accountNumber", "accountNumber");
    providerResponse.addProviderData("cardStatus", "cardStatus");
    providerResponse.addProviderData("pinTriesExceeded", "pinTriesExceeded");
    providerResponse.addProviderData("badPinTries", "badPinTries");
    providerResponse.addProviderData("expirationDate", "expirationDate");
    providerResponse.addProviderData("bic", "bic");
    providerResponse.addProviderData("iban", "iban");

    return providerResponse;
  }

  @Override
  public ProviderResponse upgradeProviderBankCard(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse changeStatusProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse addProviderBankCardCurrency(
      String requestId, BankCard bankCard, String currency) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("cardCurrencies", currency);

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCardWallet(
      String requestId, BankCard bankCard, BankCardWallet bankCardWallet) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData(
        "availableBalance", bankCardWallet.getAvailableBalance().getValue());

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCardWalletTransaction(
      String requestId, BankCard bankCard, TransactionDate transactionDate) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData(
        "listBankCardTransactions", new ArrayList<BankCardTransaction>());

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCardNumber(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("cardNumber", "12345678ABCDEFGH");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCardExpiryDate(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("expDate", "2208");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderBankCardCvv(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("cvv", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse providerTransferToBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse providerTransferFromBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse sendProviderPin(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse lockUnlockProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("referenceId", "123");

    return providerResponse;
  }

  @Override
  public ProviderResponse updateProviderBankCard(String requestId, BankCard bankCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);
    providerResponse.addProviderData("referenceId", "123XX");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDocument(String requestId, User owner, Document document) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("status", DocumentStatus.VALIDATED);
    providerResponse.addProviderData("rejectionReasonMessage", null);
    providerResponse.addProviderData("rejectionReasonType", null);

    return providerResponse;
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

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);
    providerResponse.addProviderData("referenceId", "123XX");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderCurrencyFxQuote(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

    providerResponse.addProviderData("currencyFrom", "");
    providerResponse.addProviderData("currencyTo", "");
    providerResponse.addProviderData("amount", "");
    providerResponse.addProviderData("rate", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse replaceProviderCard(String requestId, BankCard bankCard, String reason) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

    providerResponse.addProviderData("cardHolderId", "");
    providerResponse.addProviderData("availableBalance", "");
    providerResponse.addProviderData("ledgerBalance", "");
    return providerResponse;
  }

  @Override
  public ProviderResponse executeProviderCurrencyFXTrade(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

    providerResponse.addProviderData("fromCurrency", "");
    providerResponse.addProviderData("toCurrency", "");
    providerResponse.addProviderData("amount", "");
    providerResponse.addProviderData("rate", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDepositRefund(String requestId, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

    providerResponse.addProviderData("Id", "");
    providerResponse.addProviderData("Tag", "");
    providerResponse.addProviderData("DebitedWalletId", "");
    providerResponse.addProviderData("CreditedFundsAmount", "");
    providerResponse.addProviderData("CreditedFundsCurrency", "");
    providerResponse.addProviderData("FeesAmount", "");
    providerResponse.addProviderData("FeesCurrency", "");
    providerResponse.addProviderData("Status", "");
    providerResponse.addProviderData("ResultCode", "");
    providerResponse.addProviderData("ResultMessage", "");
    providerResponse.addProviderData("CreationDate", "");
    providerResponse.addProviderData("ExecutionDate", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderWithdrawRefund(String requestId, String providerId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

    providerResponse.addProviderData("Id", "");
    providerResponse.addProviderData("Tag", "");
    providerResponse.addProviderData("CreditedWalletId", "");
    providerResponse.addProviderData("DebitedFundsCurrency", "");
    providerResponse.addProviderData("DebitedFundsAmount", "");
    providerResponse.addProviderData("FeesAmount", "");
    providerResponse.addProviderData("FeesCurrency", "");
    providerResponse.addProviderData("Status", "");
    providerResponse.addProviderData("ResultCode", "");
    providerResponse.addProviderData("ResultMessage", "");
    providerResponse.addProviderData("CreationDate", "");
    providerResponse.addProviderData("ExecutionDate", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderSettlement(String requestId, String settlementId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

    providerResponse.addProviderData("Id", "");
    providerResponse.addProviderData("Tag", "");
    providerResponse.addProviderData("AuthorId", "");
    providerResponse.addProviderData("DebitedWalletId", "");
    providerResponse.addProviderData("Status", "");
    providerResponse.addProviderData("DebitedFundsAmount", "");
    providerResponse.addProviderData("DebitedFundsCurrency", "");
    providerResponse.addProviderData("CreditedFundsCurrency", "");
    providerResponse.addProviderData("CreditedFundsAmount", "");
    providerResponse.addProviderData("FeesAmount", "");
    providerResponse.addProviderData("ResultCode", "");
    providerResponse.addProviderData("FeesCurrency", "");
    providerResponse.addProviderData("ResultCode", "");
    providerResponse.addProviderData("ResultMessage", "");
    providerResponse.addProviderData("ExecutionDate", "");
    providerResponse.addProviderData("CreationDate", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse createCardRegistrations(
      String requestId, User user, RequestCreateCardRegistration requestCreateCardRegistration) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("id", "");
    providerResponse.addProviderData("tag", "");
    providerResponse.addProviderData("creationDate", "");
    providerResponse.addProviderData("userId", "");
    providerResponse.addProviderData("accessKey", "");
    providerResponse.addProviderData("preregistrationData", "");
    providerResponse.addProviderData("cardRegistrationUrl", "");
    providerResponse.addProviderData("cardId", "");
    providerResponse.addProviderData("registrationData", "");
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("currency", "");
    providerResponse.addProviderData("status", "");
    providerResponse.addProviderData("cardType", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse updateCardRegistrations(
      String requestId,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);
    providerResponse.addProviderData("id", cardRegistrationId);
    providerResponse.addProviderData("tag", requestUpdateCardRegistration.getTag());
    providerResponse.addProviderData("creationDate", "");
    providerResponse.addProviderData("userId", "");
    providerResponse.addProviderData("accessKey", "");
    providerResponse.addProviderData("preregistrationData", "");
    providerResponse.addProviderData("cardRegistrationUrl", "");
    providerResponse.addProviderData("cardId", "");
    providerResponse.addProviderData(
        "registrationData", requestUpdateCardRegistration.getRegistrationData());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("currency", "");
    providerResponse.addProviderData("status", "");
    providerResponse.addProviderData("cardType", "");

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderDepositCard(String requestId, String cardId) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.addProviderData("active", "");
    providerResponse.addProviderData("alias", "");
    providerResponse.addProviderData("cardProvider", "");
    providerResponse.addProviderData("cardType", "");
    providerResponse.addProviderData("country", "");
    providerResponse.addProviderData("currency", "");
    providerResponse.addProviderData("customTag", "");
    providerResponse.addProviderData("expirationDate", "");
    providerResponse.addProviderData("fingerprint", "");
    providerResponse.addProviderData("providerId", "");
    providerResponse.addProviderData("userProviderId", "");
    providerResponse.addProviderData("validity", "");
    return providerResponse;
  }

  @Override
  public ProviderResponse deactivateProviderDepositCard(String requestId, DepositCard depositCard) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.addProviderData("active", "");
    providerResponse.addProviderData("alias", "");
    providerResponse.addProviderData("cardProvider", "");
    providerResponse.addProviderData("cardType", "");
    providerResponse.addProviderData("country", "");
    providerResponse.addProviderData("currency", "");
    providerResponse.addProviderData("customTag", "");
    providerResponse.addProviderData("expirationDate", "");
    providerResponse.addProviderData("fingerprint", "");
    providerResponse.addProviderData("providerId", "");
    providerResponse.addProviderData("userProviderId", "");
    providerResponse.addProviderData("validity", "");
    return providerResponse;
  }

  @Override
  public ProviderResponse createPayInRefund(
      String requestId, String payInId, Refund refund, Transfer refundFee, User user) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

    providerResponse.addProviderData("refundId", refund.getId());
    providerResponse.addProviderData("resultCode", "");
    providerResponse.addProviderData("resultMessage", "");
    providerResponse.addProviderData("externalReference", "");
    providerResponse.addProviderData("refundReasonMessage", "");

    // test because fee is not mandatory, it might be missing
    if (refundFee != null) {
      providerResponse.addProviderData("refundFeeId", refundFee.getId());
    }

    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderUserDisputes(
      String requestId, String userId, int page, int itemsPerPage) {

    // create response directly, there is no external provider to call
    ProviderResponse providerResponse = new ProviderResponse();

    providerResponse.addProviderData("disputes", "");
    providerResponse.addProviderData("page", "");
    providerResponse.addProviderData("itemsPerPage", "");
    providerResponse.addProviderData("totalItems", "");
    providerResponse.addProviderData("totalPages", "");
    return providerResponse;
  }

  @Override
  public ProviderResponse getProviderWallet(String requestId, Wallet wallet) {
    return null;
  }
}
