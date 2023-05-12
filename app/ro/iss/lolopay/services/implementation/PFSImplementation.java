package ro.iss.lolopay.services.implementation;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mangopay.core.enumerations.CurrencyIso;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import ro.iss.lolopay.classes.provider.ProviderOperationStatus;
import ro.iss.lolopay.classes.provider.ProviderRequestAddCardCurrency;
import ro.iss.lolopay.classes.provider.ProviderRequestBankPayment;
import ro.iss.lolopay.classes.provider.ProviderRequestCardInquiry;
import ro.iss.lolopay.classes.provider.ProviderRequestCardIssue;
import ro.iss.lolopay.classes.provider.ProviderRequestCardSensitive;
import ro.iss.lolopay.classes.provider.ProviderRequestCardUpdate;
import ro.iss.lolopay.classes.provider.ProviderRequestChangeCardStatus;
import ro.iss.lolopay.classes.provider.ProviderRequestCurrencyFXTrade;
import ro.iss.lolopay.classes.provider.ProviderRequestFxQuote;
import ro.iss.lolopay.classes.provider.ProviderRequestGetCardWallet;
import ro.iss.lolopay.classes.provider.ProviderRequestReplaceCard;
import ro.iss.lolopay.classes.provider.ProviderRequestTransfer;
import ro.iss.lolopay.classes.provider.ProviderRequestUpgradeCard;
import ro.iss.lolopay.classes.provider.ProviderRequestViewStatement;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.classes.provider.ProviderUDF;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.Amount;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.BankCardTransactionDirection;
import ro.iss.lolopay.models.classes.BankCardTransactionOrigin;
import ro.iss.lolopay.models.classes.BankCardTransactionStatus;
import ro.iss.lolopay.models.classes.BankCardType;
import ro.iss.lolopay.models.classes.CardUserInfo;
import ro.iss.lolopay.models.classes.CardUserInfoEmploymentStatus;
import ro.iss.lolopay.models.classes.CardUserInfoEstate;
import ro.iss.lolopay.models.classes.CardUserInfoMonthlyIncome;
import ro.iss.lolopay.models.classes.CardUserInfoOccupation;
import ro.iss.lolopay.models.classes.CardUserInfoPurpose;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.ProviderOperation;
import ro.iss.lolopay.models.classes.TransactionDate;
import ro.iss.lolopay.models.classes.Ubo;
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
import ro.iss.lolopay.requests.RequestCreateCardRegistration;
import ro.iss.lolopay.requests.RequestUpdateCardRegistration;
import ro.iss.lolopay.responses.ResponseError;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.PFSService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class PFSImplementation implements PFSService {
  @Inject private WSClient ws;

  @Inject private UtilsService utilsService;

  @Inject private LogService logService;

  @Inject Config config;

  private String pfsInterfaceUrl;

  private int pfsConnectionTimeOut;

  private static SecretKey secretKey;

  private static final String secretKeyAlgorithm =
      ConfigFactory.load().getString("pfs.secretKeyAlgorithm");

  private static final String cipherAlgorithm =
      ConfigFactory.load().getString("pfs.cipherAlgorithm");

  private ZoneId pfsServerZoneId;

  public PFSImplementation() {

    pfsInterfaceUrl = ConfigFactory.load().getString("pfs.baseUrl");
    pfsConnectionTimeOut = ConfigFactory.load().getInt("pfs.connectionTimeout");
    secretKey =
        new SecretKeySpec(
            ConfigFactory.load().getString("pfs.secretKey").getBytes(), secretKeyAlgorithm);
    String zoneIdSetting = ConfigFactory.load().getString("pfs.serverZoneId");
    pfsServerZoneId =
        StringUtils.isNotBlank(zoneIdSetting) ? ZoneId.of(zoneIdSetting) : ZoneId.systemDefault();
  }

  @Override
  public ProviderResponse createProviderTransferRefund(
      String requestId,
      Refund refundTransaction,
      Transfer refundFeeTransaction,
      Transfer originalTransaction,
      User originalDebitUser) {

    return null;
  }

  @Override
  public ProviderResponse createProviderNaturalUser(String requestId, User user) {

    return null;
  }

  @Override
  public ProviderResponse saveProviderNaturalUser(String requestId, User user) {

    return null;
  }

  @Override
  public ProviderResponse createProviderLegalUser(String requestId, User legalUser) {

    return null;
  }

  @Override
  public ProviderResponse saveProviderLegalUser(String requestId, User legalUser) {

    return null;
  }

  @Override
  public ProviderResponse deactivateProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    return null;
  }

  @Override
  public ProviderResponse createProviderWallet(String requestId, User owner, Wallet wallet) {

    return null;
  }

  @Override
  public ProviderResponse createProviderDocument(
      String requestId, Account account, Application application, User owner, Document document) {

    return null;
  }

  @Override
  public ProviderResponse createProviderDocumentPage(
      String requestId,
      Account account,
      Application application,
      User owner,
      Document document,
      String fileContent) {

    return null;
  }

  @Override
  public ProviderResponse submitProviderDocument(String requestId, Document document) {

    return null;
  }

  @Override
  public ProviderResponse getProviderDocument(String requestId, User owner, Document document) {

    return null;
  }

  @Override
  public ProviderResponse createProviderUboDeclaration(
      String requestId, Account account, Application application, User owner) {

    return null;
  }

  @Override
  public ProviderResponse getProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    return null;
  }

  @Override
  public ProviderResponse submitProviderUboDeclaration(
      String requestId, User owner, UboDeclaration uboDeclaration) {

    return null;
  }

  @Override
  public ProviderResponse createProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo) {

    return null;
  }

  @Override
  public ProviderResponse updateProviderUbo(
      String requestId,
      Account account,
      Application application,
      User owner,
      UboDeclaration uboDeclaration,
      Ubo ubo) {

    return null;
  }

  @Override
  public ProviderResponse createProviderPayIn(
      String requestId,
      Account account,
      Application application,
      User user,
      Wallet userWallet,
      PayIn mainTransaction,
      Transfer feeTransaction) {

    return null;
  }

  @Override
  public ProviderResponse createProviderDirectPayIn(
      String requestId,
      DepositCard depositCard,
      User user,
      Wallet creditWallet,
      PayIn payIn,
      Transfer feeTransaction) {

    return null;
  }

  @Override
  public ProviderResponse getProviderPayInStatus(
      String requestId, Account account, Application application, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse createProviderTransfer(
      String requestId,
      Account account,
      Application application,
      Transfer mainTransaction,
      Transfer feeTransaction,
      User debitUser,
      Wallet debitWallet,
      User creditUser,
      Wallet creditWallet) {

    return null;
  }

  @Override
  public ProviderResponse getProviderTransferStatus(String requestId, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse getProviderPayOutStatus(String requestId, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse createProviderPayOut(
      String requestId,
      Account account,
      Application application,
      BankAccount bankAccount,
      User debitUser,
      Wallet debitWallet,
      PayOut mainTransaction,
      Transfer feeTransaction) {

    return null;
  }

  @Override
  public ProviderResponse createProviderBankAccount(
      String requestId, User user, BankAccount bankAccount) {

    return null;
  }

  @Override
  public ProviderResponse getProviderDepositRefund(String requestId, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse getProviderWithdrawRefund(String requestId, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse getProviderSettlement(String requestId, String settlementId) {

    return null;
  }

  @Override
  public ProviderResponse getProviderUser(String requestId, User user) {

    return null;
  }

  @Override
  public ProviderResponse getProviderTransferRefundStatus(String requestId, String providerId) {

    return null;
  }

  @Override
  public ProviderResponse createCardRegistrations(
      String requestId, User user, RequestCreateCardRegistration requestCreateCardRegistration) {

    return null;
  }

  @Override
  public ProviderResponse updateCardRegistrations(
      String requestId,
      String cardRegistrationId,
      RequestUpdateCardRegistration requestUpdateCardRegistration) {

    return null;
  }

  @Override
  public ProviderResponse createProviderBankCard(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardIssueRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.CREATEBANKCARD, requestString);
  }

  @Override
  public ProviderResponse updateProviderBankCard(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardUpdateRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.UPDATEBANKCARD, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCard(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardHolderIdOnlyRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETCARD, requestString);
  }

  @Override
  public ProviderResponse sendProviderPin(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardHolderIdOnlyRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.SENDCARDPIN, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCardNumber(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardHolderIdOnlyRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETCARDNUMBER, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCardExpiryDate(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardSensitiveRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETCARDEXPDATE, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCardCvv(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardSensitiveRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETCARDCVV, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCardWallet(
      String requestId, BankCard bankCard, BankCardWallet bankCardWallet) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getCardBalanceRequestString(bankCard, bankCardWallet);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETCARDWALLET, requestString);
  }

  @Override
  public ProviderResponse upgradeProviderBankCard(String requestId, BankCard bankCard) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());

    // create provider request object
    JsonNode requestString = getUpgradeCardRequestString(bankCard);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.UPGRADEBANKCARD, requestString);
  }

  @Override
  public ProviderResponse changeStatusProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "oldStatus", oldStatus);
    logService.debug(requestId, "IN", "newStatus", newStatus);

    // create provider request object
    JsonNode requestString = getStatusChangeRequestString(bankCard, oldStatus, newStatus);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.CHANGECARDSTATUS, requestString);
  }

  @Override
  public ProviderResponse lockUnlockProviderBankCard(
      String requestId, BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "oldStatus", oldStatus);
    logService.debug(requestId, "IN", "newStatus", newStatus);

    // create provider request object
    JsonNode requestString = getStatusChangeRequestString(bankCard, oldStatus, newStatus);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.LOCKUNLOCKCARD, requestString);
  }

  @Override
  public ProviderResponse addProviderBankCardCurrency(
      String requestId, BankCard bankCard, String currency) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "currency", currency);

    // create provider request object
    JsonNode requestString = getAddCardCurrencyRequestString(bankCard, currency);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.ADDCARDCURRENCY, requestString);
  }

  @Override
  public ProviderResponse getProviderBankCardWalletTransaction(
      String requestId, BankCard bankCard, TransactionDate transactionDate) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(
        requestId, "IN", "startDate", Instant.ofEpochSecond(transactionDate.getStartDate()));
    logService.debug(
        requestId, "IN", "endDate", Instant.ofEpochSecond(transactionDate.getEndDate()));

    // create provider request object
    JsonNode requestString =
        getProviderBankCardWalletTransactionRequestString(bankCard, transactionDate);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(
        requestId, ProviderOperation.GETCARDWALLETTRANSACTIONS, requestString);
  }

  @Override
  public ProviderResponse providerTransferToBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "currency", currency);
    logService.debug(requestId, "IN", "amount", amount);

    // create provider request object
    JsonNode requestString = getTransferRequestString(bankCard, currency, amount);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.TRANSFERTOCARD, requestString);
  }

  @Override
  public ProviderResponse providerTransferFromBankCard(
      String requestId, BankCard bankCard, String currency, Integer amount) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "currency", currency);
    logService.debug(requestId, "IN", "amount", amount);

    // create provider request object
    JsonNode requestString = getTransferRequestString(bankCard, currency, amount);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.TRANSFERFROMCARD, requestString);
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

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "beneficiaryName", beneficiaryName);
    logService.debug(requestId, "IN", "creditorIBAN", creditorIBAN);
    logService.debug(requestId, "IN", "creditorBIC", creditorBIC);
    logService.debug(requestId, "IN", "paymentAmount", paymentAmount);
    logService.debug(requestId, "IN", "reference", reference);

    // create provider request object
    JsonNode requestString =
        getBankPaymentRequestString(
            bankCard, beneficiaryName, creditorIBAN, creditorBIC, paymentAmount, reference);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.EXECUTEBANKPAYMENT, requestString);
  }

  @Override
  public ProviderResponse getProviderCurrencyFxQuote(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "currencyFrom", currencyFrom);
    logService.debug(requestId, "IN", "currencyTo", currencyTo);
    logService.debug(requestId, "IN", "amount", amount);

    // create provider request object
    JsonNode requestString =
        getCurrencyFxQuoteRequestString(bankCard, currencyFrom, currencyTo, amount);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.GETFXQUOTE, requestString);
  }

  @Override
  public ProviderResponse replaceProviderCard(String requestId, BankCard bankCard, String reason) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "reason", reason);

    // create provider request object
    JsonNode requestString = getReplaceCardRequestString(bankCard, reason);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.REPLACECARD, requestString);
  }

  @Override
  public ProviderResponse executeProviderCurrencyFXTrade(
      String requestId, BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    logService.debug(requestId, "IN", "bankCard", bankCard.getId());
    logService.debug(requestId, "IN", "currencyFrom", currencyFrom);
    logService.debug(requestId, "IN", "currencyTo", currencyTo);
    logService.debug(requestId, "IN", "amount", amount);

    // create provider request object
    JsonNode requestString =
        getProviderCurrencyFXTradeString(bankCard, currencyFrom, currencyTo, amount);
    logService.debug(requestId, "L", "requestString", requestString);

    // perform post operation and return result
    return performPostToProvider(requestId, ProviderOperation.FXTRADE, requestString);
  }

  /**
   * Execute post operation to provider
   *
   * @param providerOperation
   * @param requestString
   * @return
   */
  private ProviderResponse performPostToProvider(
      String requestId, ProviderOperation providerOperation, JsonNode requestString) {

    logService.debug(requestId, "IN", "providerOperation", providerOperation);
    logService.debug(requestId, "IN", "requestString", requestString);

    // init URL path
    String urlPath = "";

    // determine path based on provider operation to be performed
    switch (providerOperation) {
      case CREATEBANKCARD:
        urlPath = "/cardIssue";
        break;

      case GETCARD:
        urlPath = "/cardInquiry";
        break;

      case UPGRADEBANKCARD:
        urlPath = "/updateCard";
        break;

      case CHANGECARDSTATUS:
        urlPath = "/changeCardStatus";
        break;

      case ADDCARDCURRENCY:
        urlPath = "/addCardCurrency";
        break;

      case GETCARDWALLET:
        urlPath = "/getCardBalance";
        break;

      case GETCARDWALLETTRANSACTIONS:
        urlPath = "/viewStatement";
        break;

      case GETCARDNUMBER:
        urlPath = "/getCardNumber";
        break;

      case GETCARDEXPDATE:
        urlPath = "/getExpDate";
        break;

      case GETCARDCVV:
        urlPath = "/getCvv";
        break;

      case TRANSFERTOCARD:
        urlPath = "/depositToCard";
        break;

      case SENDCARDPIN:
        urlPath = "/pinReminder";
        break;

      case LOCKUNLOCKCARD:
        urlPath = "/lockUnlock";
        break;

      case TRANSFERFROMCARD:
        urlPath = "/purchaseOnUs";
        break;

      case UPDATEBANKCARD:
        urlPath = "/updateCard";
        break;

      case EXECUTEBANKPAYMENT:
        urlPath = "/bankPayment";
        break;

      case GETFXQUOTE:
        urlPath = "/currencyFXQuote";
        break;

      case REPLACECARD:
        urlPath = "/replaceCard";
        break;

      case FXTRADE:
        urlPath = "/currencyFXTrade";
        break;

      default:
        break;
    }

    logService.debug(requestId, "L", "urlPath", urlPath);

    try {
      // Initialise the request object
      WSRequest request =
          ws.url(pfsInterfaceUrl.concat(urlPath))
              .addHeader("RequestId", requestId)
              .setRequestTimeout(Duration.of(pfsConnectionTimeOut, ChronoUnit.MILLIS));

      WSResponse wsResponse = request.post(requestString).toCompletableFuture().get();
      logService.debug(requestId, "L", "responseStatus", wsResponse.getStatus());
      if (!providerOperation.equals(ProviderOperation.GETCARDNUMBER)
          && !providerOperation.equals(ProviderOperation.GETCARDEXPDATE)
          && !providerOperation.equals(ProviderOperation.GETCARDCVV)) {
        logService.debug(requestId, "L", "responseBody", wsResponse.getBody());
      } else {
        logService.debug(requestId, "L", "responseBody", "* - sensitive data");
      }

      return mapResponse(requestId, wsResponse.getBody(), providerOperation, requestString);

    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));

      // create error lists
      List<ResponseError> responseErrorsList = new ArrayList<ResponseError>();

      // create return error object
      ResponseError responseError = new ResponseError();
      responseError.setErrorCode(ErrorMessage.ERROR_PROVIDER);
      responseError.setErrorDescription(e.getMessage());

      // add error object to list
      responseErrorsList.add(responseError);

      // set negative response
      ProviderResponse providerResponse = new ProviderResponse();
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.setProviderErrors(responseErrorsList);

      return providerResponse;
    }
  }

  /**
   * Map a pfs response body string to a standard framework provider response based on response type
   * determined by urlPath
   *
   * @param responseBody
   * @param urlPath
   * @return
   */
  private ProviderResponse mapResponse(
      String requestId,
      String responseBody,
      ProviderOperation providerOperation,
      JsonNode requestString) {

    logService.debug(requestId, "IN", "providerOperation", providerOperation);

    if (!providerOperation.equals(ProviderOperation.GETCARDNUMBER)
        && !providerOperation.equals(ProviderOperation.GETCARDEXPDATE)
        && !providerOperation.equals(ProviderOperation.GETCARDCVV)) {
      logService.debug(requestId, "IN", "responseBody", responseBody);
    }

    // init provider response
    ProviderResponse providerResponse = new ProviderResponse();

    // get response to json
    JsonNode jsonResponse = Json.parse(responseBody);

    // get response status
    boolean responseSuccess = jsonResponse.findPath("success").asBoolean();
    logService.debug(requestId, "L", "responseSuccess", responseSuccess);

    if (responseSuccess) {
      // get response body
      JsonNode jsonResponseBody = jsonResponse.findPath("body");

      // init provider data
      HashMap<String, Object> providerData = null;

      switch (providerOperation) {
        case CREATEBANKCARD:
          logService.debug(requestId, "L", "operation", "CREATEBANKCARD");

          // get card issue node variables
          providerData = mapCardIssueResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARD:
          logService.debug(requestId, "L", "operation", "GETCARD");

          // get card issue node variables
          providerData = mapCardInquiryResponseNode(requestId, jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case UPGRADEBANKCARD:
          logService.debug(requestId, "L", "operation", "UPGRADEBANKCARD");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case CHANGECARDSTATUS:
          logService.debug(requestId, "L", "operation", "CHANGECARDSTATUS");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case ADDCARDCURRENCY:
          logService.debug(requestId, "L", "operation", "ADDCARDCURRENCY");

          // get card issue node variables
          providerData = mapAddCardCurrencyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARDWALLET:
          logService.debug(requestId, "L", "operation", "GETCARDWALLET");

          // get card issue node variables
          providerData = mapGetCardBalanceResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARDWALLETTRANSACTIONS:
          logService.debug(requestId, "L", "operation", "GETCARDWALLET");

          // get card issue node variables
          providerData = mapViewStatementResponseNode(requestId, jsonResponseBody, requestString);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARDNUMBER:
          logService.debug(requestId, "L", "operation", "GETCARDNUMBER");

          // get card issue node variables
          providerData = mapGetCardNumberResponseNode(requestId, jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARDEXPDATE:
          logService.debug(requestId, "L", "operation", "GETCARDEXPDATE");

          // get card issue node variables
          providerData = mapGetCardExpDateResponseNode(requestId, jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETCARDCVV:
          logService.debug(requestId, "L", "operation", "GETCARDCVV");

          // get card issue node variables
          providerData = mapGetCardCvvResponseNode(requestId, jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case TRANSFERTOCARD:
          logService.debug(requestId, "L", "operation", "TRANSFERTOCARD");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case SENDCARDPIN:
          logService.debug(requestId, "L", "operation", "SENDCARDPIN");

          // get card issuemapResponse node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case LOCKUNLOCKCARD:
          logService.debug(requestId, "L", "operation", "LOCKUNLOCKCARD");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case TRANSFERFROMCARD:
          logService.debug(requestId, "L", "operation", "TRANSFERFROMCARD");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case UPDATEBANKCARD:
          logService.debug(requestId, "L", "operation", "UPDATEBANKCARD");

          // get card issue node variables
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.UPDATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case EXECUTEBANKPAYMENT:
          logService.debug(requestId, "L", "operation", "EXECUTEBANKPAYMENT");

          // nothing to be mapped in here
          providerData = mapReferenceIdOnlyResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case GETFXQUOTE:
          logService.debug(requestId, "L", "operation", "GETFXQUOTE");

          // get card issue node variables
          providerData = mapGetFxQuoteResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case REPLACECARD:
          logService.debug(requestId, "L", "operation", "REPLACECARD");

          // get card issue node variables
          providerData = mapReplaceCardResponseNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.CREATED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        case FXTRADE:
          logService.debug(requestId, "L", "operation", "FXTRADE");

          // get card issue node variables
          providerData = mapCurrencyFXTradeNode(jsonResponseBody);

          // set provider operation status
          providerResponse.setProviderOperationStatus(ProviderOperationStatus.RETRIEVED);

          // set provider data
          providerResponse.setProviderData(providerData);

          // exit switch
          break;

        default:
          break;
      }
    } else {
      logService.error(requestId, "L", "result", "error");

      // get response errors
      ArrayNode jsonErrors =
          jsonResponse.has("errors") ? (ArrayNode) jsonResponse.findPath("errors") : null;

      // create error lists
      List<ResponseError> responseErrorsList = new ArrayList<ResponseError>();

      for (JsonNode jsonNode : jsonErrors) {
        // create return error object
        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ErrorMessage.ERROR_PROVIDER);
        responseError.setErrorDescription(jsonNode.asText());

        // add error object to list
        responseErrorsList.add(responseError);
      }

      // set negative response
      providerResponse.setProviderOperationStatus(ProviderOperationStatus.ERROR);
      providerResponse.setProviderErrors(responseErrorsList);
    }

    return providerResponse;
  }

  private HashMap<String, Object> mapReplaceCardResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    response.put("referenceId", jsonResponseBody.findPath("referenceId").asText());

    JsonNode jsonNodeCardIssue = jsonResponseBody.findPath("cardIssue");

    response.put("cardHolderId", jsonNodeCardIssue.findPath("cardHolderId").asText());
    response.put("availableBalance", jsonNodeCardIssue.findPath("availableBalance").asText());
    response.put("ledgerBalance", jsonNodeCardIssue.findPath("ledgerBalance").asText());

    // return response
    return response;
  }

  private HashMap<String, Object> mapGetFxQuoteResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("referenceId");
    response.put("referenceId", jsonNodeReferenceId.asText());

    JsonNode jsonNodeCurrencyFXQuote = jsonResponseBody.findPath("currencyFXQuote");

    response.put("currencyFrom", jsonNodeCurrencyFXQuote.findPath("currencyFrom").asText());
    response.put("currencyTo", jsonNodeCurrencyFXQuote.findPath("currencyTo").asText());
    response.put("amount", jsonNodeCurrencyFXQuote.findPath("amount").asText());
    response.put("rate", jsonNodeCurrencyFXQuote.findPath("rate").asText());

    // return response
    return response;
  }

  private HashMap<String, Object> mapCurrencyFXTradeNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("referenceId");
    response.put("referenceId", jsonNodeReferenceId.asText());

    JsonNode jsonNodeCurrencyFXTrade = jsonResponseBody.findPath("currencyFXTrade");

    response.put("fromCurrency", jsonNodeCurrencyFXTrade.findPath("fromCurrency").asText());
    response.put("toCurrency", jsonNodeCurrencyFXTrade.findPath("toCurrency").asText());
    response.put("amount", jsonNodeCurrencyFXTrade.findPath("amount").asText());
    response.put("rate", jsonNodeCurrencyFXTrade.findPath("rate").asText());

    // return response
    return response;
  }

  /**
   * Map response containing only referenceId as response body
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapReferenceIdOnlyResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("referenceId");
    response.put("referenceId", jsonNodeReferenceId.asText());

    // return response
    return response;
  }

  /**
   * Map view statement response
   *
   * @param requestId request id
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapViewStatementResponseNode(
      String requestId, JsonNode jsonResponseBody, JsonNode requestString) {

    // init variables
    HashMap<String, Object> response = new HashMap<String, Object>();
    List<BankCardTransaction> listBankCardTransactions = new ArrayList<BankCardTransaction>();
    int recordCounter = 0;

    // get data from cardBalance node
    JsonNode jsonNodeViewStatement = jsonResponseBody.findPath("viewStatement");
    JsonNode jsonNodeCardHolderStatementDetails =
        jsonNodeViewStatement.findPath("cardHolderStatementDetails");
    JsonNode jsonNodeCardPan = jsonNodeCardHolderStatementDetails.findPath("cardPan");
    String accountCurrency = jsonNodeCardHolderStatementDetails.findPath("currency").asText();
    JsonNode jsonNodeCardAccount = jsonNodeCardHolderStatementDetails.findPath("cardAccount");

    // populate response list
    if (!jsonNodeCardAccount.isNull()) {
      ArrayNode arrayTransactions = (ArrayNode) jsonNodeCardPan.findPath("cardAccount");

      // get currency
      for (final JsonNode transactionNode : arrayTransactions) {
        recordCounter++;

        JsonNode transaction = transactionNode.findPath("transaction");

        // create bank transaction object
        BankCardTransaction bankCardTransaction = new BankCardTransaction();
        bankCardTransaction.setTransactionId(transaction.findPath("authNum").asText());

        String rspCode = transaction.findPath("rspCode").asText();
        bankCardTransaction.setTransactionStatus(
            "000".equals(rspCode)
                ? BankCardTransactionStatus.SUCCEEDED
                : BankCardTransactionStatus.FAILED);

        Long transactionDatePfs =
            utilsService.stringDateToTimeStamp(
                transaction.findPath("date").asText(), "MM/dd/yyyy hh:mm:ss a", pfsServerZoneId);

        // register 7 hours delay from PFS
        // bankCardTransaction.setDate(transactionDatePfs + (60 * 60 * 7));
        bankCardTransaction.setDate(transactionDatePfs);

        bankCardTransaction.setCurrency(
            CurrencyISO.valueOf(requestString.get("currency").asText().toUpperCase()));

        bankCardTransaction.setBankCardProviderId(requestString.get("cardHolderId").asText());

        Amount availableBalance = new Amount();
        availableBalance.setCurrency(CurrencyISO.valueOf(accountCurrency));
        availableBalance.setValue(transaction.findPath("availableBalance").asInt());
        bankCardTransaction.setAvailableBalance(availableBalance);

        Amount ledgerBalance = new Amount();
        ledgerBalance.setCurrency(CurrencyISO.valueOf(accountCurrency));
        ledgerBalance.setValue(transaction.findPath("ledgerBalance").asInt());
        bankCardTransaction.setLedgerBalance(ledgerBalance);

        Amount amount = new Amount();
        amount.setCurrency(CurrencyISO.valueOf(accountCurrency));
        amount.setValue(Math.abs(transaction.findPath("amount").asInt()));
        bankCardTransaction.setAmount(amount);

        Amount fee = new Amount();
        fee.setCurrency(CurrencyISO.valueOf(accountCurrency));
        fee.setValue(Math.abs(transaction.findPath("fee").asInt()));
        bankCardTransaction.setFee(fee);

        bankCardTransaction.setDescription(
            (transaction.findPath("description") != null
                ? transaction.findPath("description").asText()
                : ""));

        if (transaction.findPath("transactionOrigin") != null) {
          if (transaction.findPath("transactionOrigin").asText().equals("P")) {
            bankCardTransaction.setOrigin(BankCardTransactionOrigin.API);
          } else {
            bankCardTransaction.setOrigin(BankCardTransactionOrigin.ATM_POS);
          }
        }

        bankCardTransaction.setOriginalCurrency(
            getCurrencyFromCurrencyCode(requestId, transaction.findPath("termCurrency").asText()));
        bankCardTransaction.setOriginalAmount(
            Math.abs(transaction.findPath("origTransAmt").asInt()));
        String terminalLocation = transaction.findPath("termNameLocation").asText();
        if (terminalLocation.matches("^0+$")) {
          // if only zeroes, set to empty string
          terminalLocation = "";
        }
        bankCardTransaction.setTerminalName(terminalLocation);
        bankCardTransaction.setTerminalCity(transaction.findPath("termCity").asText());
        bankCardTransaction.setTerminalCountry(transaction.findPath("termCountry").asText());

        String mti = "";
        if (transaction.findPath("mti") != null) {
          mti = transaction.findPath("mti").asText();
        }

        String transactionType = "";
        if (transaction.findPath("transactionType") != null) {
          transactionType = transaction.findPath("transactionType").asText();
        }

        // DEBIT direction rules
        if (mti.startsWith("15")) {
          bankCardTransaction.setDirection(BankCardTransactionDirection.DEBIT);
        }

        if (mti.equals("0110")) {
          if (transactionType.matches("00[a-zA-Z0-9]{2}00")
              || transactionType.matches("01[a-zA-Z0-9]{2}00")
              || transactionType.matches("11[a-zA-Z0-9]{2}00")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.DEBIT);
          }
        }

        if (mti.equals("0210")) {
          if (transactionType.matches("00[a-zA-Z0-9]{2}00")
              || transactionType.matches("01[a-zA-Z0-9]{2}00")
              || transactionType.matches("11[a-zA-Z0-9]{2}00")
              || transactionType.equals("5000000")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.DEBIT);
          }

          if (transactionType.matches("2000[a-zA-Z0-9]{2}") || transactionType.equals("550000")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.CREDIT);
          }
        }

        if (mti.equals("0230")) {
          if (transactionType.matches("00[a-zA-Z0-9]{2}00")
              || transactionType.matches("01[a-zA-Z0-9]{2}00")
              || transactionType.matches("19[a-zA-Z0-9]{2}00")
              || transactionType.matches("26[a-zA-Z0-9]{2}00")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.DEBIT);
          }

          if (transactionType.matches("29[a-zA-Z0-9]{2}00")
              || transactionType.matches("22[a-zA-Z0-9]{2}00")
              || transactionType.equals("200000")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.CREDIT);
          }
        }

        if (mti.equals("0430")) {
          if (transactionType.matches("2000[a-zA-Z0-9]{2}")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.DEBIT);
          }

          if (transactionType.matches("00[a-zA-Z0-9]{2}00")
              || transactionType.matches("01[a-zA-Z0-9]{2}00")
              || transactionType.matches("11[a-zA-Z0-9]{2}00")
              || transactionType.matches("30[a-zA-Z0-9]{2}00")
              || transactionType.matches("31[a-zA-Z0-9]{2}00")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.CREDIT);
          }
        }

        if (mti.equals("0410")) {
          if (transactionType.matches("01[a-zA-Z0-9]{2}00")
              || transactionType.matches("11[a-zA-Z0-9]{2}00")
              || transactionType.matches("30[a-zA-Z0-9]{2}00")) {
            bankCardTransaction.setDirection(BankCardTransactionDirection.CREDIT);
          }
        }

        if (mti.startsWith("27")) {
          bankCardTransaction.setDirection(BankCardTransactionDirection.CREDIT);
        }

        bankCardTransaction.setRecordNo(recordCounter);

        // add transaction to list
        listBankCardTransactions.add(bankCardTransaction);
      }
    }

    // format response
    response.put("listBankCardTransactions", listBankCardTransactions);

    // return response
    return response;
  }

  /**
   * Gets the currency from a code (numeric or string)
   *
   * @param requestId request id
   * @param currencyCode currency code, may come as numeric or as string (ISO code)
   * @return the currency code as string, null if not found
   */
  private String getCurrencyFromCurrencyCode(String requestId, String currencyCode) {

    String currency = new String();

    if (currencyCode.matches("[0-9]+")) {
      try {
        currency = config.getString("currencyCode." + currencyCode);
      } catch (Exception e) {
        logService.error(
            requestId, "L", "errors", "config not found: currencyCode." + currencyCode);
      }

    } else {
      for (CurrencyIso currencyIso : CurrencyIso.values()) {
        if (String.valueOf(currencyIso).equals(currencyCode)) {
          currency = currencyCode;
        }
      }
    }

    return currency;
  }

  /**
   * Map card wallet available balance
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapGetCardBalanceResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from cardBalance node
    JsonNode jsonNodeCardBalance = jsonResponseBody.findPath("cardBalance");

    // get available balance
    String availableBalance = jsonNodeCardBalance.findPath("availableBalance").asText();
    String ledgerBalance = jsonNodeCardBalance.findPath("ledgerBalance").asText();

    response.put("availableBalance", availableBalance);
    response.put("ledgerBalance", ledgerBalance);

    // return response
    return response;
  }

  /**
   * Map add card currency response body
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapAddCardCurrencyResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("cardCurrencies");
    response.put("cardCurrencies", jsonNodeReferenceId.asText());

    // return response
    return response;
  }

  /**
   * Map card issue response body
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapCardIssueResponseNode(JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("referenceId");
    response.put("referenceId", jsonNodeReferenceId.asText());

    // get data from cardIssue
    JsonNode jsonNodeCardIssue = jsonResponseBody.findPath("cardIssue");

    // get card holder id
    String cardHolderId = jsonNodeCardIssue.findPath("cardHolderId").asText();

    // add card holder id to response
    response.put("cardHolderId", cardHolderId);

    // return response
    return response;
  }

  /**
   * Map get card expiry date response
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapGetCardExpDateResponseNode(
      String requestId, JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();
    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("expDate");
    response.put("expDate", decrypt(requestId, jsonNodeReferenceId.asText()));

    return response;
  }

  /**
   * Map get card cvv response
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapGetCardCvvResponseNode(
      String requestId, JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("cvv");

    response.put("cvv", decrypt(requestId, jsonNodeReferenceId.asText()));

    // return response
    return response;
  }

  /**
   * Map get card number response
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapGetCardNumberResponseNode(
      String requestId, JsonNode jsonResponseBody) {

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("cardNumber");
    response.put("cardNumber", decrypt(requestId, jsonNodeReferenceId.asText()));

    // return response
    return response;
  }

  /**
   * Map card inquiry response body to
   *
   * @param jsonResponseBody
   * @return
   */
  private HashMap<String, Object> mapCardInquiryResponseNode(
      String requestId, JsonNode jsonResponseBody) {

    logService.debug(requestId, "IN", "jsonResponseBody", jsonResponseBody);

    HashMap<String, Object> response = new HashMap<String, Object>();

    // get data from referenceId
    JsonNode jsonNodeReferenceId = jsonResponseBody.findPath("referenceId");
    response.put("referenceId", jsonNodeReferenceId.asText());

    // get data from cardInquiry
    JsonNode jsonNodeCardInquiry = jsonResponseBody.findPath("cardInquiry");

    // get data from cardInfo
    JsonNode jsonNodeCardInfo = jsonNodeCardInquiry.findPath("cardInfo");
    JsonNode jsonNodeCardHolder = jsonNodeCardInquiry.findPath("cardHolder");
    ArrayNode jsonNodeCardCurrencies = (ArrayNode) jsonNodeCardInquiry.findPath("currency");

    // get cardInfo
    response.put("accountBaseCurrency", jsonNodeCardInfo.findPath("accountBaseCurrency").asText());
    response.put("cardType", jsonNodeCardInfo.findPath("cardType").asText());
    response.put("accountNumber", jsonNodeCardInfo.findPath("accountNumber").asText());
    response.put("cardStatus", jsonNodeCardInfo.findPath("cardStatus").asText());
    response.put("pinTriesExceeded", jsonNodeCardInfo.findPath("pinTriesExceeded").asText());
    response.put("badPinTries", jsonNodeCardInfo.findPath("badPinTries").asText());
    response.put("expirationDate", jsonNodeCardInfo.findPath("expirationDate").asText());
    response.put("cardStyle", jsonNodeCardInfo.findPath("cardStyle").asText());
    response.put("deliveryType", jsonNodeCardInfo.findPath("deliveryType").asText());
    response.put("bic", jsonNodeCardInfo.findPath("bic").asText());
    response.put("iban", jsonNodeCardInfo.findPath("iban").asText());
    response.put("expirationDate", jsonNodeCardInfo.findPath("expirationDate").asText());

    // get cardHolder
    response.put("firstName", jsonNodeCardHolder.findPath("firstName").asText());
    response.put("lastName", jsonNodeCardHolder.findPath("lastName").asText());
    response.put("address1", jsonNodeCardHolder.findPath("address1").asText());
    response.put("address2", jsonNodeCardHolder.findPath("address2").asText());
    response.put("address3", jsonNodeCardHolder.findPath("address3").asText());
    response.put("address4", jsonNodeCardHolder.findPath("address4").asText());
    response.put("city", jsonNodeCardHolder.findPath("city").asText());
    response.put("countyName", jsonNodeCardHolder.findPath("countyName").asText());
    response.put("zip", jsonNodeCardHolder.findPath("zip").asText());
    response.put("countryCode", jsonNodeCardHolder.findPath("countryCode").asText());
    response.put("phone", jsonNodeCardHolder.findPath("phone").asText());
    response.put("emailAddr", jsonNodeCardHolder.findPath("emailAddr").asText());
    response.put("dob", jsonNodeCardHolder.findPath("dob").asText());
    response.put("embossName", jsonNodeCardHolder.findPath("embossName").asText());

    Map<String, String> udfFields = new HashMap<String, String>();
    JsonNode jsonNodeUDFs = jsonNodeCardHolder.findPath("udfs").findPath("udfDataList");
    if (jsonNodeUDFs != null) {
      jsonNodeUDFs.forEach(
          udfField -> {
            String fieldName = udfField.findPath("name").asText().trim();
            String fieldValue = udfField.findPath("value").asText().trim();
            udfFields.put(fieldName, fieldValue);
          });
    }
    response.put("udfFields", udfFields);

    // log cardInfo
    logService.debug(
        requestId,
        "IN",
        "accountBaseCurrency",
        jsonNodeCardInfo.findPath("accountBaseCurrency").asText());
    logService.debug(requestId, "IN", "cardType", jsonNodeCardInfo.findPath("cardType").asText());
    logService.debug(
        requestId, "IN", "accountNumber", jsonNodeCardInfo.findPath("accountNumber").asText());
    logService.debug(
        requestId, "IN", "cardStatus", jsonNodeCardInfo.findPath("cardStatus").asText());
    logService.debug(
        requestId,
        "IN",
        "pinTriesExceeded",
        jsonNodeCardInfo.findPath("pinTriesExceeded").asText());
    logService.debug(
        requestId, "IN", "badPinTries", jsonNodeCardInfo.findPath("badPinTries").asText());
    logService.debug(
        requestId, "IN", "expirationDate", jsonNodeCardInfo.findPath("expirationDate").asText());
    logService.debug(requestId, "IN", "cardStyle", jsonNodeCardInfo.findPath("cardStyle").asText());
    logService.debug(
        requestId, "IN", "deliveryType", jsonNodeCardInfo.findPath("deliveryType").asText());
    logService.debug(requestId, "IN", "bic", jsonNodeCardInfo.findPath("bic").asText());
    logService.debug(requestId, "IN", "iban", jsonNodeCardInfo.findPath("iban").asText());

    // get cardHolder
    logService.debug(
        requestId, "IN", "firstName", jsonNodeCardHolder.findPath("firstName").asText());
    logService.debug(requestId, "IN", "lastName", jsonNodeCardHolder.findPath("lastName").asText());
    logService.debug(requestId, "IN", "address1", jsonNodeCardHolder.findPath("address1").asText());
    logService.debug(requestId, "IN", "address2", jsonNodeCardHolder.findPath("address2").asText());
    logService.debug(requestId, "IN", "address3", jsonNodeCardHolder.findPath("address3").asText());
    logService.debug(requestId, "IN", "address4", jsonNodeCardHolder.findPath("address4").asText());
    logService.debug(requestId, "IN", "city", jsonNodeCardHolder.findPath("city").asText());
    logService.debug(
        requestId, "IN", "countyName", jsonNodeCardHolder.findPath("countyName").asText());
    logService.debug(requestId, "IN", "zip", jsonNodeCardHolder.findPath("zip").asText());
    logService.debug(
        requestId, "IN", "countryCode", jsonNodeCardHolder.findPath("countryCode").asText());
    logService.debug(requestId, "IN", "zipCode", jsonNodeCardHolder.findPath("zipCode").asText());
    logService.debug(requestId, "IN", "phone", jsonNodeCardHolder.findPath("phone").asText());
    logService.debug(
        requestId, "IN", "emailAddr", jsonNodeCardHolder.findPath("emailAddr").asText());
    logService.debug(requestId, "IN", "dob", jsonNodeCardHolder.findPath("dob").asText());
    logService.debug(
        requestId, "IN", "embossName", jsonNodeCardHolder.findPath("embossName").asText());

    // get currency
    List<String> currency = new ArrayList<String>();
    for (final JsonNode jsonNodeCurrency : jsonNodeCardCurrencies) {
      currency.add(jsonNodeCurrency.asText());
      logService.debug(requestId, "IN", "currency", jsonNodeCurrency.asText());
    }
    response.put("currency", currency);

    // return response
    return response;
  }

  private JsonNode getProviderBankCardWalletTransactionRequestString(
      BankCard bankCard, TransactionDate transactionDate) {

    ProviderRequestViewStatement providerRequestViewStatement = new ProviderRequestViewStatement();
    providerRequestViewStatement.setCardHolderId(bankCard.getProviderId());
    providerRequestViewStatement.setCurrency(transactionDate.getCurrency().toString());
    providerRequestViewStatement.setStartDate(
        utilsService.timeStampToDate(transactionDate.getStartDate(), "dd/MM/yyyy"));
    providerRequestViewStatement.setEndDate(
        utilsService.timeStampToDate(transactionDate.getEndDate(), "dd/MM/yyyy"));

    // prepare response
    JsonNode response = Json.toJson(providerRequestViewStatement);

    // return response
    return response;
  }

  private JsonNode getTransferRequestString(BankCard bankCard, String currency, Integer amount) {

    ProviderRequestTransfer providerRequestTransfer = new ProviderRequestTransfer();
    providerRequestTransfer.setCardHolderId(bankCard.getProviderId());
    providerRequestTransfer.setCurrencyCode(currency);
    providerRequestTransfer.setAmount(amount.toString());

    // prepare response
    JsonNode response = Json.toJson(providerRequestTransfer);

    // return response
    return response;
  }

  private JsonNode getReplaceCardRequestString(BankCard bankCard, String reason) {

    ProviderRequestReplaceCard providerRequestReplaceCard = new ProviderRequestReplaceCard();
    providerRequestReplaceCard.setCardHolderId(bankCard.getProviderId());
    providerRequestReplaceCard.setReason(reason);

    // prepare response
    JsonNode response = Json.toJson(providerRequestReplaceCard);

    // return response
    return response;
  }

  private JsonNode getCurrencyFxQuoteRequestString(
      BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    ProviderRequestFxQuote providerRequestFxQuote = new ProviderRequestFxQuote();
    providerRequestFxQuote.setCardHolderId(bankCard.getProviderId());
    providerRequestFxQuote.setCurrencyFrom(currencyFrom);
    providerRequestFxQuote.setCurrencyTo(currencyTo);
    providerRequestFxQuote.setAmount(amount);

    // prepare response
    JsonNode response = Json.toJson(providerRequestFxQuote);

    // return response
    return response;
  }

  private JsonNode getProviderCurrencyFXTradeString(
      BankCard bankCard, String currencyFrom, String currencyTo, String amount) {

    ProviderRequestCurrencyFXTrade providerCurrencyFXTrade = new ProviderRequestCurrencyFXTrade();
    providerCurrencyFXTrade.setAmount(amount);
    providerCurrencyFXTrade.setCardHolderId(bankCard.getProviderId());
    providerCurrencyFXTrade.setCurrencyFrom(currencyFrom);
    providerCurrencyFXTrade.setCurrencyTo(currencyTo);

    // prepare response
    JsonNode response = Json.toJson(providerCurrencyFXTrade);

    // return response
    return response;
  }

  private JsonNode getBankPaymentRequestString(
      BankCard bankCard,
      String beneficiaryName,
      String creditorIBAN,
      String creditorBIC,
      Integer paymentAmount,
      String reference) {

    ProviderRequestBankPayment providerRequestBankPayment = new ProviderRequestBankPayment();
    providerRequestBankPayment.setCardHolderId(bankCard.getProviderId());
    providerRequestBankPayment.setBeneficiaryName(beneficiaryName);
    providerRequestBankPayment.setPaymentAmount(paymentAmount.toString());
    providerRequestBankPayment.setReference(reference);
    providerRequestBankPayment.setCreditorBic(creditorBIC);
    providerRequestBankPayment.setCreditorIban(creditorIBAN);

    // prepare response
    JsonNode response = Json.toJson(providerRequestBankPayment);

    // return response
    return response;
  }

  private JsonNode getAddCardCurrencyRequestString(BankCard bankCard, String currency) {

    ProviderRequestAddCardCurrency providerRequestAddCardCurrency =
        new ProviderRequestAddCardCurrency();
    providerRequestAddCardCurrency.setCardHolderId(bankCard.getProviderId());
    providerRequestAddCardCurrency.setCurrency(currency);

    // prepare response
    JsonNode response = Json.toJson(providerRequestAddCardCurrency);

    // return response
    return response;
  }

  private JsonNode getStatusChangeRequestString(
      BankCard bankCard, BankCardStatus oldStatus, BankCardStatus newStatus) {

    ProviderRequestChangeCardStatus providerRequestChangeCardStatus =
        new ProviderRequestChangeCardStatus();
    providerRequestChangeCardStatus.setCardHolderId(bankCard.getProviderId());

    switch (oldStatus) {
      case ISSUED:
        providerRequestChangeCardStatus.setOldStatus("0");
        break;

      case OPEN:
        providerRequestChangeCardStatus.setOldStatus("1");
        break;

      case LOST:
        providerRequestChangeCardStatus.setOldStatus("2");
        break;

      case STOLEN:
        providerRequestChangeCardStatus.setOldStatus("3");
        break;

      case BLOCKED_PAYOUT:
        providerRequestChangeCardStatus.setOldStatus("4");
        break;

      case BLOCKED_FINAL:
        providerRequestChangeCardStatus.setOldStatus("9");
        break;

      case EXPIRED:
        providerRequestChangeCardStatus.setOldStatus("E");
        break;

      case BLOCKED_PIN:
        providerRequestChangeCardStatus.setOldStatus("Q");
        break;

      case BLOCKED_FRAUD:
        providerRequestChangeCardStatus.setOldStatus("C");
        break;
    }

    switch (newStatus) {
      case ISSUED:
        providerRequestChangeCardStatus.setNewStatus("0");
        break;

      case OPEN:
        providerRequestChangeCardStatus.setNewStatus("1");
        break;

      case LOST:
        providerRequestChangeCardStatus.setNewStatus("2");
        break;

      case STOLEN:
        providerRequestChangeCardStatus.setNewStatus("3");
        break;

      case BLOCKED_PAYOUT:
        providerRequestChangeCardStatus.setNewStatus("4");
        break;

      case BLOCKED_FINAL:
        providerRequestChangeCardStatus.setNewStatus("9");
        break;

      case EXPIRED:
        providerRequestChangeCardStatus.setNewStatus("E");
        break;

      case BLOCKED_PIN:
        providerRequestChangeCardStatus.setNewStatus("Q");
        break;

      case BLOCKED_FRAUD:
        providerRequestChangeCardStatus.setNewStatus("C");
        break;
    }

    // prepare response
    JsonNode response = Json.toJson(providerRequestChangeCardStatus);

    // return response
    return response;
  }

  /**
   * Map BankCard collection to a provider specific json for request card sensitive data (CVV and
   * Exp Date only) api
   *
   * @param bankCard
   * @return
   */
  private JsonNode getCardSensitiveRequestString(BankCard bankCard) {

    ProviderRequestCardSensitive providerRequestCardSensitive = new ProviderRequestCardSensitive();
    providerRequestCardSensitive.setCardHolderId(bankCard.getProviderId());
    providerRequestCardSensitive.setPhone1(bankCard.getPhone());

    // prepare response
    JsonNode response = Json.toJson(providerRequestCardSensitive);

    // return response
    return response;
  }

  /**
   * Map BankCard collection to a provider specific json for request card inquiry api
   *
   * @param bankCard
   * @return
   */
  private JsonNode getCardHolderIdOnlyRequestString(BankCard bankCard) {

    ProviderRequestCardInquiry providerRequestCardInquiry = new ProviderRequestCardInquiry();
    providerRequestCardInquiry.setCardHolderId(bankCard.getProviderId());

    // prepare response
    JsonNode response = Json.toJson(providerRequestCardInquiry);

    // return response
    return response;
  }

  private JsonNode getCardBalanceRequestString(BankCard bankCard, BankCardWallet bankCardWallet) {

    ProviderRequestGetCardWallet providerRequestGetCardWallet = new ProviderRequestGetCardWallet();
    providerRequestGetCardWallet.setCardHolderId(bankCard.getProviderId());
    providerRequestGetCardWallet.setCurrency(bankCardWallet.getCurrency().toString());

    // prepare response
    JsonNode response = Json.toJson(providerRequestGetCardWallet);

    // return response
    return response;
  }

  private JsonNode getUpgradeCardRequestString(BankCard bankCard) {

    // create request data object
    ProviderRequestUpgradeCard providerRequestUpgradeCard = new ProviderRequestUpgradeCard();

    providerRequestUpgradeCard.setCardHolderId(bankCard.getProviderId());
    providerRequestUpgradeCard.setFirstName(bankCard.getFirstName());
    providerRequestUpgradeCard.setLastName(bankCard.getLastName());
    providerRequestUpgradeCard.setAddress1(bankCard.getAddress1());
    providerRequestUpgradeCard.setAddress2(bankCard.getAddress2());
    providerRequestUpgradeCard.setAddress3(bankCard.getAddress3());
    providerRequestUpgradeCard.setAddress4(bankCard.getAddress4());
    providerRequestUpgradeCard.setCity(bankCard.getCity());
    providerRequestUpgradeCard.setCountyName(bankCard.getCountyName());
    providerRequestUpgradeCard.setZipCode(bankCard.getZipCode());
    providerRequestUpgradeCard.setCountryCode(bankCard.getCountryCode());
    providerRequestUpgradeCard.setPhone(bankCard.getPhone());
    providerRequestUpgradeCard.setEmail(bankCard.getEmail());
    providerRequestUpgradeCard.setDob(bankCard.getDob());
    providerRequestUpgradeCard.setDeliveryType("PC"); // always upgrade to physical

    // prepare response
    JsonNode response = Json.toJson(providerRequestUpgradeCard);

    // return response
    return response;
  }

  /**
   * Map BankCard collection to a provider specific json for request card update api
   *
   * @param bankCard
   * @return
   */
  private JsonNode getCardUpdateRequestString(BankCard bankCard) {

    // create request data object - we do not provide Emboss name anymore
    ProviderRequestCardUpdate providerRequestCardUpdate = new ProviderRequestCardUpdate();
    providerRequestCardUpdate.setCardHolderId(bankCard.getProviderId());
    providerRequestCardUpdate.setFirstName(bankCard.getFirstName());
    providerRequestCardUpdate.setLastName(bankCard.getLastName());
    providerRequestCardUpdate.setAddress1(bankCard.getAddress1());
    providerRequestCardUpdate.setAddress2(bankCard.getAddress2());
    providerRequestCardUpdate.setAddress3(bankCard.getAddress3());
    providerRequestCardUpdate.setAddress4(bankCard.getAddress4());
    providerRequestCardUpdate.setCity(bankCard.getCity());
    providerRequestCardUpdate.setCountyName(bankCard.getCountyName());
    providerRequestCardUpdate.setZipCode(bankCard.getZipCode());
    providerRequestCardUpdate.setCountryCode(bankCard.getCountryCode());
    providerRequestCardUpdate.setPhone(bankCard.getPhone());
    providerRequestCardUpdate.setEmail(bankCard.getEmail());
    providerRequestCardUpdate.setDob(bankCard.getDob());

    CardUserInfo cardUserInfo = bankCard.getCardUserInfo();
    if (cardUserInfo != null) {
      List<ProviderUDF> udfData = new ArrayList<>();
      udfData.add(
          new ProviderUDF(
              CardUserInfoEmploymentStatus.FIELD_NAME,
              cardUserInfo.getEmploymentStatus().getLabel()));
      udfData.add(
          new ProviderUDF(CardUserInfoPurpose.FIELD_NAME, cardUserInfo.getPurpose().getLabel()));
      udfData.add(
          new ProviderUDF(
              CardUserInfoOccupation.FIELD_NAME, cardUserInfo.getOccupation().getLabel()));
      udfData.add(
          new ProviderUDF(
              CardUserInfoMonthlyIncome.FIELD_NAME, cardUserInfo.getMonthlyIncome().getLabel()));
      udfData.add(
          new ProviderUDF(CardUserInfoEstate.FIELD_NAME, cardUserInfo.getEstate().getLabel()));
      providerRequestCardUpdate.setUdfData(udfData);
    }
    // prepare response
    JsonNode response = Json.toJson(providerRequestCardUpdate);

    // return response
    return response;
  }

  /**
   * Map BankCard collection to a provider specific json for request card issue api
   *
   * @param bankCard
   * @return
   */
  private JsonNode getCardIssueRequestString(BankCard bankCard) {

    // create request data object
    ProviderRequestCardIssue providerRequestCardIssue = new ProviderRequestCardIssue();
    if (bankCard.getType() != null && bankCard.getType().equals(BankCardType.PHYSICAL)) {
      providerRequestCardIssue.setCardType("P");
    } else {
      providerRequestCardIssue.setCardType("V");
    }
    providerRequestCardIssue.setFirstName(bankCard.getFirstName());
    providerRequestCardIssue.setLastName(bankCard.getLastName());
    providerRequestCardIssue.setEmbossName(bankCard.getEmbossName());
    providerRequestCardIssue.setAddress1(bankCard.getAddress1());
    providerRequestCardIssue.setAddress2(bankCard.getAddress2());
    providerRequestCardIssue.setAddress3(bankCard.getAddress3());
    providerRequestCardIssue.setAddress4(bankCard.getAddress4());
    providerRequestCardIssue.setCity(bankCard.getCity());
    providerRequestCardIssue.setCountyName(bankCard.getCountyName());
    providerRequestCardIssue.setZipCode(bankCard.getZipCode());
    providerRequestCardIssue.setCountryCode(bankCard.getCountryCode());
    providerRequestCardIssue.setPhone(bankCard.getPhone());
    providerRequestCardIssue.setPhone2("00" + bankCard.getPhone());
    providerRequestCardIssue.setEmail(bankCard.getEmail());
    providerRequestCardIssue.setDob(bankCard.getDob());

    CardUserInfo cardUserInfo = bankCard.getCardUserInfo();
    if (cardUserInfo != null) {
      List<ProviderUDF> udfData = new ArrayList<>();
      udfData.add(
          new ProviderUDF(
              CardUserInfoEmploymentStatus.FIELD_NAME,
              cardUserInfo.getEmploymentStatus().getLabel()));
      udfData.add(
          new ProviderUDF(CardUserInfoPurpose.FIELD_NAME, cardUserInfo.getPurpose().getLabel()));
      udfData.add(
          new ProviderUDF(
              CardUserInfoOccupation.FIELD_NAME, cardUserInfo.getOccupation().getLabel()));
      udfData.add(
          new ProviderUDF(
              CardUserInfoMonthlyIncome.FIELD_NAME, cardUserInfo.getMonthlyIncome().getLabel()));
      udfData.add(
          new ProviderUDF(CardUserInfoEstate.FIELD_NAME, cardUserInfo.getEstate().getLabel()));
      providerRequestCardIssue.setUdfData(udfData);
    }

    // prepare response
    JsonNode response = Json.toJson(providerRequestCardIssue);

    // return response
    return response;
  }

  private String decrypt(String requestId, String ecryptedString) {
    char[] ch = ecryptedString.toCharArray();
    try {
      Cipher cipher = Cipher.getInstance(cipherAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      String decrypted = new String(cipher.doFinal(Hex.decodeHex(ch)));
      decrypted = decrypted.replaceAll("(\\r|\\n|\\f|\\u0010)", "");
      return decrypted;
    } catch (Exception e) {
      logService.error(requestId, "L", "error", e.getMessage());
      logService.error(requestId, "L", "errorStack", ExceptionUtils.getStackTrace(e));
    }
    return "";
  }

  @Override
  public ProviderResponse getProviderDepositCard(String requestId, String cardId) {

    return null;
  }

  @Override
  public ProviderResponse deactivateProviderDepositCard(String requestId, DepositCard depositCard) {

    return null;
  }

  @Override
  public ProviderResponse createPayInRefund(
      String requestId, String payInId, Refund refund, Transfer refundFee, User user) {

    return null;
  }

  @Override
  public ProviderResponse getProviderUserDisputes(
      String requestId, String userId, int page, int itemsPerPage) {

    return null;
  }

  @Override
  public ProviderResponse getProviderWallet(String requestId, Wallet wallet) {
    return null;
  }
}
