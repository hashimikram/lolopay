package ro.iss.lolopay.services.definition;

import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankAccount;
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
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;

public interface ProviderService {
  public ProviderResponse createProviderPayIn(
      String requestId,
      Account account,
      Application application,
      User user,
      Wallet userWallet,
      PayIn mainTransaction,
      Transfer feeTransaction);

  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String providerId);

  public ProviderResponse createProviderPayOut(
      String requestId,
      Account account,
      Application application,
      BankAccount bankAccount,
      User debitUser,
      Wallet debitWallet,
      PayOut mainTransaction,
      Transfer feeTransaction);

  public ProviderResponse getProviderPayOutStatus(String requestId, String providerId);

  public ProviderResponse createProviderTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer mainTransaction,
      Transfer feeTransaction,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet);

  public ProviderResponse getProviderTransferStatus(String requestId, String providerId);

  public ProviderResponse createProviderTransferRefund(
      String requestId,
      Refund refundTransaction,
      Transfer refundFeeTransaction,
      Transfer originalTransaction,
      User originalDebitUser);

  public ProviderResponse getProviderTransferRefundStatus(String requestId, String providerId);

  public ProviderResponse getProviderDepositRefund(String requestId, String providerId);

  public ProviderResponse getProviderWithdrawRefund(String requestId, String providerId);

  public ProviderResponse getProviderSettlement(String requestId, String settlementId);

  public ProviderResponse createProviderNaturalUser(String requestId, User user);

  public ProviderResponse saveProviderNaturalUser(String requestId, User user);

  public ProviderResponse createProviderLegalUser(String requestId, User legalUser);

  public ProviderResponse saveProviderLegalUser(String requestId, User legalUser);

  public ProviderResponse getProviderUser(String requestId, User user);

  public ProviderResponse createProviderBankAccount(
      String requestId, User user, BankAccount bankAccount);

  public ProviderResponse deactivateProviderBankAccount(
      String requestId, User user, BankAccount bankAccount);

  public ProviderResponse createProviderWallet(String requestId, User owner, Wallet wallet);

  public ProviderResponse createProviderDocument(
      String requestId, Account account, Application application, User owner, Document document);

  public ProviderResponse createProviderDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String fileContent);

  public ProviderResponse getProviderDocument(String requestId, User owner, Document document);

  public ProviderResponse submitProviderDocument(String requestId, Document document);

  public ProviderResponse createProviderUboDeclaration(
      String requestId, Account account, Application application, User owner);

  public ProviderResponse getProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration);

  public ProviderResponse submitProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration);

  public ProviderResponse createProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo);

  public ProviderResponse updateProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo);

  public ProviderResponse createProviderBankCard(String requestId, BankCard bankCard);

  public ProviderResponse getProviderBankCard(String requestId, BankCard bankCard);

  public ProviderResponse upgradeProviderBankCard(String requestId, BankCard bankCard);

  public ProviderResponse changeStatusProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus);

  public ProviderResponse addProviderBankCardCurrency(
      String requestId, BankCard bankCard, String currency);

  public ProviderResponse getProviderBankCardWallet(
      String requestId, BankCard bankCard, BankCardWallet bankCardWallet);

  public ProviderResponse getProviderBankCardWalletTransaction(
      String requestId, BankCard bankCard, TransactionDate transactionDate);

  public ProviderResponse getProviderBankCardNumber(String requestId, BankCard bankCard);

  public ProviderResponse getProviderBankCardExpiryDate(String requestId, BankCard bankCard);

  public ProviderResponse getProviderBankCardCvv(String requestId, BankCard bankCard);

  public ProviderResponse providerTransferToBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount);

  public ProviderResponse providerTransferFromBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount);

  public ProviderResponse sendProviderPin(String requestId, BankCard bankCard);

  public ProviderResponse lockUnlockProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus);

  public ProviderResponse updateProviderBankCard(String requestId, BankCard bankCard);

  public ProviderResponse executeProviderBankPayment(
      String requestId,
      BankCard bankCard,
      String beneficiaryName,
      String creditorIBAN,
      String creditorBIC,
      Integer paymentAmount,
      String reference);

  public ProviderResponse getProviderCurrencyFxQuote(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount);

  public ProviderResponse replaceProviderCard(String requestId, BankCard bankCard, String reason);

  public ProviderResponse executeProviderCurrencyFXTrade(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount);

  public ProviderResponse createCardRegistrations(
      String requestId, User user, RequestCreateCardRegistration requestCreateCardRegistration);

  public ProviderResponse updateCardRegistrations(
      String requestId,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration);

  public ProviderResponse createProviderDirectPayIn(
      String requestId,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      PayIn mainTransaction,
      Transfer feeTransaction);

  public ProviderResponse getProviderDepositCard(String requestId, String cardId);

  public ProviderResponse deactivateProviderDepositCard(String requestId, DepositCard depositCard);

  public ProviderResponse createPayInRefund(
      String requestId, String payInId, Refund refund, Transfer refundFee, User user);

  public ProviderResponse getProviderUserDisputes(
      String requestId, String userId, int page, int itemsPerPage);

  public ProviderResponse getProviderWallet(String requestId, Wallet wallet);
}
