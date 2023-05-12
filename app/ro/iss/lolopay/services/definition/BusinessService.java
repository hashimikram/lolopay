package ro.iss.lolopay.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.CardRegistration;
import ro.iss.lolopay.classes.ExecuteCardWalletsTrade;
import ro.iss.lolopay.classes.FxQuote;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.Ubo;
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
import ro.iss.lolopay.requests.RequestAddBankCardCurrency;
import ro.iss.lolopay.requests.RequestAmount;
import ro.iss.lolopay.requests.RequestBankCardTransfer;
import ro.iss.lolopay.requests.RequestBankPayment;
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
import ro.iss.lolopay.services.implementation.BusinessImplementation;

@ImplementedBy(BusinessImplementation.class)
public interface BusinessService {

  /**
   * Anonymizez user data.
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @return user User
   */
  public User anonymizeUser(String requestId, Account account, Application application, User user);

  /**
   * Create request for pay in
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param creditWallet
   * @param requestedAmount
   * @param requestedFees
   * @param requestedFeeModel
   * @param customTag
   * @param returnURL
   * @param cardType
   * @param secureMode
   * @param culture
   * @param templateURL
   * @param statementDescriptor
   * @return
   */
  public List<Transaction> createPayIn(
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
      String statementDescriptor);

  /**
   * Register deposit refund, this one comes from provider only
   *
   * @param requestId
   * @param account
   * @param application
   * @param providerRefundId
   * @param payInRefund
   */
  public void closeDepositRefund(
      String requestId,
      Account account,
      Application application,
      String providerRefundId,
      Wallet userWallet,
      Refund payInRefund,
      Transfer transferFee,
      Wallet accountWallet);

  /**
   * Register withdraw refund, this one comes from provider only
   *
   * @param requestId
   * @param account
   * @param application
   * @param providerRefundId
   */
  public void registerWithdrawRefund(
      String requestId, Account account, Application application, String providerRefundId);

  /**
   * Register settlement, this one comes from provider only
   *
   * @param requestId
   * @param account
   * @param application
   * @param providerRefundId
   */
  public void registerSettlement(
      String requestId, Account account, Application application, String providerRefundId);

  /**
   * Reject or accept a pay in transaction
   *
   * @param requestId
   * @param account
   * @param application
   * @param payIn
   * @param payInFee
   * @param creditWallet
   * @param accountWallet
   */
  public void closePayIn(
      String requestId,
      Account account,
      Application application,
      PayIn payIn,
      Transfer payInFee,
      Wallet creditWallet,
      Wallet accountWallet);

  /**
   * Initiate withdraw operation
   *
   * @param requestId
   * @param account
   * @param application
   * @param debitUser
   * @param debitWallet
   * @param bankAccount
   * @param requestedAmount
   * @param requestedFees
   * @param requestedFeeModel
   * @param customTag
   * @param bankWireRef
   * @return
   */
  public List<Transaction> createPayOut(
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
      String bankWireRef);

  /**
   * Close pay out operation
   *
   * @param requestId
   * @param account
   * @param application
   * @param payOut
   * @param payOutFee
   * @param debitUser
   * @param debitWallet
   * @param accountWallet
   */
  public void closePayOut(
      String requestId,
      Account account,
      Application application,
      PayOut payOut,
      Transfer payOutFee,
      User debitUser,
      Wallet debitWallet,
      Wallet accountWallet);

  /**
   * Submit user document for approval
   *
   * @param requestId
   * @param account
   * @param application
   * @param document
   */
  public void submitDocument(
      String requestId, Account account, Application application, Document document);

  /**
   * Add a new page to a document
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param document
   * @param file
   */
  public void createDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String file);

  /**
   * Register KYC Document to financial provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param requestCreateDocument
   * @return
   */
  public Document createDocument(
      String requestId,
      Account account,
      Application application,
      User owner,
      RequestCreateDocument requestCreateDocument);

  /**
   * Register Ubo declaration to financial provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @return
   */
  public UboDeclaration createUboDeclaration(
      String requestId, Account account, Application application, User owner);

  /**
   * Submit Ubo declaration for approval
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   */
  public void submitUboDeclaration(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration);

  /**
   * Creates an Ubo inside an Ubo declaration
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @param requestUbo
   * @return
   */
  public Ubo createUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      RequestUbo requestUbo);

  /**
   * Updates an Ubo inside an Ubo declaration
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param uboDeclaration
   * @param uboId
   * @param requestUbo
   * @return
   */
  public Ubo updateUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      String uboId,
      RequestUbo requestUbo);

  /**
   * Update Ubo declaration validation status
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param uboDeclaration
   */
  public void updateUboDeclarationStatus(
      String requestId,
      Account account,
      Application application,
      User user,
      UboDeclaration uboDeclaration);

  /**
   * Register wallet to financial provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param owner
   * @param requestCreateWallet
   * @return
   */
  public Wallet createWallet(
      String requestId,
      Account account,
      Application application,
      User owner,
      RequestCreateWallet requestCreateWallet);

  /**
   * Create bank account IBAN
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestCreateBankAccountIBAN
   * @return
   */
  public BankAccountIBAN createBankAccountIBAN(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_IBAN requestCreateBankAccountIBAN);

  /**
   * Create bank account US
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestCreateBankAccountUS
   * @return
   */
  public BankAccountUS createBankAccountUS(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_US requestCreateBankAccountUS);

  /**
   * Create bank account GB
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestCreateBankAccountGB
   * @return
   */
  public BankAccountGB createBankAccountGB(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_GB requestCreateBankAccountGB);

  /**
   * Create bank account CA
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestCreateBankAccountCA
   * @return
   */
  public BankAccountCA createBankAccountCA(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_CA requestCreateBankAccountCA);

  /**
   * Create bank account OTHER
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestCreateBankAccountOTHER
   * @return
   */
  public BankAccountOTHER createBankAccountOTHER(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestCreateBankAccount_OTHER requestCreateBankAccountOTHER);

  /**
   * Deactivate bank account
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param bankAccount
   */
  public void deactivateBankAccount(
      String requestId,
      Account account,
      Application application,
      User user,
      BankAccount bankAccount);

  /**
   * Create a natural user
   *
   * @param requestId
   * @param account
   * @param application
   * @param requestCreateNaturalUser
   * @return
   */
  public User createNaturalUser(
      String requestId,
      Account account,
      Application application,
      RequestCreateNaturalUser requestCreateNaturalUser);

  /**
   * Save / update natural user
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param requestSaveNaturalUser
   */
  public void saveNaturalUser(
      String requestId,
      Account account,
      Application application,
      User user,
      RequestSaveNaturalUser requestSaveNaturalUser);

  /**
   * Create a legal user
   *
   * @param requestId
   * @param account
   * @param application
   * @param requestCreateLegalUser
   * @return
   */
  public User createLegalUser(
      String requestId,
      Account account,
      Application application,
      RequestCreateLegalUser requestCreateLegalUser);

  /**
   * Save legal user
   *
   * @param requestId
   * @param account
   * @param application
   * @param legalUser
   * @param requestSaveLegalUser
   */
  public void saveLegalUser(
      String requestId,
      Account account,
      Application application,
      User legalUser,
      RequestSaveLegalUser requestSaveLegalUser);

  /**
   * Update user KYC from provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   */
  public void updateUserKyc(String requestId, Account account, Application application, User user);

  /**
   * Update user data from provider, called in case local db is altered. It only retrieves basic
   * natural user data
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   */
  public void updateUserBasicDataFromProvider(
      String requestId, Account account, Application application, User user);

  /**
   * Update document validation status
   *
   * @param requestId
   * @param account
   * @param application
   * @param user
   * @param document
   */
  public void updateDocumentStatus(
      String requestId, Account account, Application application, User user, Document document);

  /**
   * Create normal wallet to wallet transfer
   *
   * @param requestId
   * @param account
   * @param application
   * @param debitUser
   * @param debitWallet
   * @param creditUser
   * @param creditWallet
   * @param requestedAmount
   * @param requestedFees
   * @param requestedFeeModel
   * @param customTag
   * @return
   */
  public List<Transaction> createTransfer(
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
      String customTag);

  /**
   * Create a failed transfer which is not going to be register to provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param debitWallet
   * @param creditWallet
   * @param requestedAmount
   * @param customTag
   * @param errorMessage
   * @return
   */
  public List<Transaction> createFailedTransfer(
      String requestId,
      Account account,
      Application application,
      Wallet debitWallet,
      Wallet creditWallet,
      RequestAmount requestedAmount,
      String customTag,
      String errorMessage);

  /**
   * Close a transfer
   *
   * @param requestId
   * @param account
   * @param application
   * @param transfer
   * @param transferFee
   * @param debitUser
   * @param debitWallet
   * @param creditUser
   * @param creditWallet
   * @param accountWallet
   */
  public void closeTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer transfer,
      Transfer transferFee,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet,
      Wallet accountWallet);

  /**
   * Refund normal wallet to wallet transfer
   *
   * @param account
   * @param application
   * @param originalDebitUser
   * @param originalDebitWallet
   * @param originalCreditUser
   * @param originalCreditWallet
   * @param originalTransaction
   * @param customTag
   * @return
   */
  public List<Transaction> createTransferRefund(
      String requestId,
      Account account,
      Application application,
      User originalDebitUser,
      Wallet originalDebitWallet,
      User originalCreditUser,
      Wallet originalCreditWallet,
      Transfer originalTransaction,
      String customTag);

  /**
   * Create a failed refund which is not going to be register to provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param originalDebitWallet
   * @param originalCreditWallet
   * @param originalTransaction
   * @param customTag
   * @param errorMessage
   * @return
   */
  public List<Transaction> createFailedRefund(
      String requestId,
      Account account,
      Application application,
      Wallet originalDebitWallet,
      Wallet originalCreditWallet,
      Transfer originalTransaction,
      String customTag,
      String errorMessage);

  /**
   * Close a refund
   *
   * @param account
   * @param application
   * @param refund
   * @param transactionFee
   * @param originalCreditUser
   * @param originalCreditWallet
   * @param originalDebitUser
   * @param originalDebitWallet
   * @param accountWallet
   */
  public void closeRefund(
      String requestId,
      Account account,
      Application application,
      Refund refund,
      Transfer transactionFee,
      User originalCreditUser,
      Wallet originalCreditWallet,
      User originalDebitUser,
      Wallet originalDebitWallet,
      Wallet accountWallet);

  /**
   * Register bank card in the system and to financial provider
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCardOwner
   * @param requestCreateBankCard
   * @return
   */
  public BankCard createBankCard(
      String requestId,
      Account account,
      Application application,
      User bankCardOwner,
      RequestCreateBankCard requestCreateBankCard);

  /**
   * Get the card from financial provider and update it's data
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   */
  public void getBankCard(
      String requestId, Account account, Application application, BankCard bankCard);

  /**
   * Retrieves the manually reissued card from financial provider and creates it in the database.
   *
   * @param requestId
   * @param account
   * @param application
   * @param existingBankCard
   * @param reissuedCardProviderId
   * @return the reissued card
   */
  public BankCard retrieveManuallyReissuedCard(
      String requestId,
      Account account,
      Application application,
      BankCard existingBankCard,
      String reissuedCardProviderId);

  /**
   * Retrieves the bank card wallet from financial provider for a manually reissued card.
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param currencyCode
   */
  public void retrieveWalletForManuallyReissuedCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      String currencyCode);

  /**
   * Upgrade a card from virtual to physical
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param requestUpgradeBankCard
   */
  public void upgradeBankCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      RequestUpgradeBankCard requestUpgradeBankCard);

  /**
   * Change bank card status
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param oldStatus
   * @param newStatus
   */
  public void changeStatusBankCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus);

  /**
   * Add new currency (wallet) to card
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param requestAddBankCardCurrency
   */
  public void addBankCardCurrency(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      RequestAddBankCardCurrency requestAddBankCardCurrency);

  /**
   * Get bank card wallet
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param bankCardWallet
   */
  public void getBankCardWallet(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardWallet bankCardWallet);

  /**
   * Retrieve card transactions per wallet and period days
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @param bankCardWallet
   * @param startDate
   * @param endDate
   * @return
   */
  public List<BankCardTransaction> getBankCardWalletTransaction(
      String requestId,
      Account account,
      BankCard bankCard,
      BankCardWallet bankCardWallet,
      Application application,
      long startDate,
      long endDate);

  /**
   * Retrieve card number
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @return
   */
  public String getBankCardNumber(
      String requestId, Account account, Application application, BankCard bankCard);

  /**
   * Retrieve card expiration date
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @return
   */
  public String getBankCardExpiryDate(String requestId, Account account, BankCard bankCard);

  /**
   * Retrieve card cvv number
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @return
   */
  public String getBankCardCVV(String requestId, Account account, BankCard bankCard);

  /**
   * Deposit money to provider bank card (Money taken from our account registered to provider)
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @param requestBankCardTransferTo
   */
  public void transferToBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      RequestBankCardTransfer requestBankCardTransferTo);

  /**
   * Send a new pin to card owner
   *
   * @param requestId
   * @param account
   * @param bankCard
   */
  public void sendPin(String requestId, Account account, BankCard bankCard);

  /**
   * Lock / Unlock bank card
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param oldStatus
   * @param newStatus
   */
  public void lockUnlockCard(
      String requestId,
      Account account,
      Application application,
      BankCard bankCard,
      BankCardStatus oldStatus,
      BankCardStatus newStatus);

  /**
   * Withdraw money from bank card to company account at provider
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @param requestBankCardTransfer
   */
  public void transferFromBankCard(
      String requestId,
      Account account,
      BankCard bankCard,
      RequestBankCardTransfer requestBankCardTransfer);

  /**
   * Update user bank card
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCardOwner
   * @param bankCard
   * @param requestUpdateBankCard
   */
  public void updateBankCard(
      String requestId,
      Account account,
      Application application,
      User bankCardOwner,
      BankCard bankCard,
      RequestUpdateBankCard requestUpdateBankCard);

  /**
   * Perform a bank transfer as a pay out, from the user card to a specified IBAN
   *
   * @param requestId
   * @param account
   * @param application
   * @param bankCard
   * @param requestBankPayment
   */
  public void executeBankPayment(
      String requestId, Account account, BankCard bankCard, RequestBankPayment requestBankPayment);

  /**
   * Perform a request to obtain an FX quote
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @param requestFxQuote
   * @return
   */
  public FxQuote getCurrencyFxQuote(
      String requestId, Account account, BankCard bankCard, RequestFxQuote requestFxQuote);

  /**
   * Perform a request to obtain a new card
   *
   * @param requestId
   * @param account
   * @param bankCard
   * @return BankCard new card
   */
  public BankCard replaceCard(
      String requestId, Account account, Application application, BankCard bankCard, String reason);

  /**
   * Executes a trade between two wallets of one card
   *
   * @param requestId
   * @param sessionAccount
   * @param bankCard
   * @param requestExecuteCardWalletsTrade
   * @return
   */
  public ExecuteCardWalletsTrade executeCardWalletsTrade(
      String requestId,
      Account sessionAccount,
      BankCard bankCard,
      RequestExecuteCardWalletsTrade requestExecuteCardWalletsTrade);

  /**
   * Performs a request to register a card
   *
   * @param requestId
   * @param sessionAccount
   * @param application
   * @param user
   * @return
   */
  public CardRegistration createCardRegistrations(
      String requestId,
      Account sessionAccount,
      User user,
      RequestCreateCardRegistration requestCreateCardRegistration);

  /**
   * Performs an update of a registered card
   *
   * @param requestId
   * @param sessionAccount
   * @param requestUpdateCardRegistration
   * @return
   */
  public CardRegistration updateCardRegistrations(
      String requestId,
      Account sessionAccount,
      Application application,
      User user,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration);

  /**
   * Performs a direct PayIn
   *
   * @param requestId
   * @param sessionAccount
   * @param sessionApplication
   * @param creditUser
   * @param creditWallet
   * @param requestCreateDirectPayIn
   * @return
   */
  public List<Transaction> createDirectPayIn(
      String requestId,
      Account sessionAccount,
      Application sessionApplication,
      DepositCard depositCard,
      User creditUser,
      Wallet creditWallet,
      RequestCreateDirectPayIn requestCreateDirectPayIn);

  /**
   * Gets deposit card object from provider
   *
   * @param requestId
   * @param sessionAccount
   * @param cardProviderId
   * @return
   */
  public DepositCard getProviderDepositCard(
      String requestId, Account sessionAccount, String cardProviderId);

  /**
   * Deactivates a deposit card
   *
   * @param requestId
   * @param sessionAccount
   * @param sessionApplication
   * @param depositCard
   * @return
   */
  public DepositCard deactivateDepositCard(
      String requestId,
      Account sessionAccount,
      Application sessionApplication,
      DepositCard depositCard);

  /**
   * Creates a refund of a payIn
   *
   * @param requestId
   * @param account
   * @param application
   * @param formRefundPayIn
   * @param originalPayIn
   * @return
   */
  public List<Transaction> createPayInRefund(
      String requestId,
      Account account,
      Application application,
      RequestRefundPayIn formRefundPayIn,
      PayIn originalPayIn,
      Wallet originalCreditWallet,
      User originalCreditUser);

  /**
   * Gets status of a payin
   *
   * @param requestId
   * @param account
   * @param payInProviderId
   * @return
   */
  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String payInProviderId);

  /**
   * Performs a direct PayIn with address verification system
   *
   * @param requestId
   * @param sessionAccount
   * @param sessionApplication
   * @param creditUser
   * @param creditWallet
   * @param requestCreateDirectPayIn
   * @return
   */
  public List<Transaction> createAVSDirectPayIn(
      String requestId,
      Account account,
      Application application,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      RequestCreateAVSDirectPayIn requestCreateAVSDirectPayIn)
      throws GenericRestException;

  /**
   * Gets dispute list of a user
   *
   * @param requestId
   * @param sessionAccount
   * @param userId
   * @param page
   * @param pageSize
   * @return
   */
  public PaginatedList getUserDisputes(
      String requestId, Account sessionAccount, User userId, int page, int pageSize);

  /**
   * Updates balance of company wallets,.
   *
   * @param requestId
   * @param account
   * @param application
   * @param wallet
   */
  public void updateCompanyWalletBalance(
      String requestId, Account account, Application application, Wallet wallet);
}
