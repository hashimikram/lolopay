package ro.iss.lolopay.controllers;

import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangopay.core.enumerations.CurrencyIso;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.enums.NotificationType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.PayIn;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Callback;
import ro.iss.lolopay.models.main.ProcessedCallback;
import ro.iss.lolopay.models.services.definition.AccountService;
import ro.iss.lolopay.models.services.definition.ApplicationService;
import ro.iss.lolopay.models.services.definition.CallbackService;
import ro.iss.lolopay.models.services.definition.PayInService;
import ro.iss.lolopay.models.services.definition.ProcessedCallbackService;
import ro.iss.lolopay.requests.RequestPFSCompanyWalletHook;
import ro.iss.lolopay.requests.RequestPFSTransactionHook;
import ro.iss.lolopay.requests.RequestPFSTransactionHookData;
import ro.iss.lolopay.responses.ResponseProcessedCallback;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class CallbackController extends Controller {
  @Inject CoreService coreService;

  @Inject AccountService accountService;

  @Inject ApplicationService applicationService;

  @Inject PayInService transactionIncomingService;

  @Inject CallbackService callbackService;

  @Inject UtilsService utilsService;

  @Inject LogService logService;

  @Inject FormFactory formFactory;

  @Inject ProcessedCallbackService processedCallbackService;

  @Inject Config config;

  @CustomStart
  public Result mango(Request request, String accountId, String applicationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "mango: hook started");

    logService.debug(requestId, "IN", "start", "GET POST /callbacks/mango");
    logService.debug(requestId, "IN", "start", "accountId: " + accountId);
    logService.debug(requestId, "IN", "start", "applicationId: " + applicationId);

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(requestId, request);

    logService.debug(requestId, "OUT", "L", "FULL REQUEST: " + fullRequestString);
    logService.debug(requestId, "OUT", "L", "RessourceId:" + request.getQueryString("RessourceId"));
    logService.debug(requestId, "OUT", "L", "EventType:" + request.getQueryString("EventType"));
    logService.debug(requestId, "OUT", "L", "Date:" + request.getQueryString("Date"));

    // perform some validations
    if ((accountId == null) || accountId.equals("")) {
      logService.error(requestId, "L", "errors", "mango: missing account id");
      return badRequest();
    }

    if ((applicationId == null) || applicationId.equals("")) {
      logService.error(requestId, "L", "errors", "mango: missing application id");
      return badRequest();
    }

    if (request.getQueryString("RessourceId") == null) {
      logService.error(requestId, "L", "errors", "mango: missing ressource id");
      return badRequest();
    }

    if (request.getQueryString("EventType") == null) {
      logService.error(requestId, "L", "errors", "mango: missing event type id");
      return badRequest();
    }

    if (request.getQueryString("Date") == null) {
      logService.error(requestId, "L", "errors", "mango: missing date");
      return badRequest();
    }

    // TODO this throws exception instead of returning null
    NotificationType incommingMangoNotification =
        NotificationType.valueOf(request.getQueryString("EventType"));

    if (incommingMangoNotification == null) {
      logService.error(requestId, "L", "errors", "mango: invalid event type");
      return badRequest();
    }

    Account sessionAccount = accountService.getAccountByAccountId(requestId, accountId);
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "mango: account not found");
      return badRequest();
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, applicationId);
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "mango: application not found");
      return badRequest();
    }

    // build callback
    Callback newCallback = new Callback();
    newCallback.setProvider("MANGO");
    newCallback.setAccountId(accountId);
    newCallback.setApplicationId(applicationId);
    newCallback.setNoFails(0);

    // build callback parameters
    HashMap<String, Object> callbackParameters = new HashMap<String, Object>();
    callbackParameters.put("EventType", incommingMangoNotification);
    callbackParameters.put("RessourceId", request.getQueryString("RessourceId"));
    callbackParameters.put("Date", request.getQueryString("Date"));

    newCallback.setParameters(callbackParameters);

    // save callback
    callbackService.saveCallback(requestId, newCallback);

    // reply to mango hook
    logService.debug(requestId, "L", "end", "mango: hook completed");
    return ok();
  }

  @CustomStart
  public Result mangoReturn(Request request, String accountId, String applicationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "mangoReturn: redirect started");

    logService.debug(requestId, "IN", "start", "GET POST mangoReturn");
    logService.debug(requestId, "IN", "start", "accountId: " + accountId);
    logService.debug(requestId, "IN", "start", "applicationId: " + applicationId);

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(requestId, request);
    logService.debug(requestId, "OUT", "start", "FULL REQUEST: " + fullRequestString);
    logService.debug(
        requestId, "OUT", "start", "transactionId:" + request.getQueryString("transactionId"));

    // perform some validations
    if ((accountId == null) || accountId.equals("")) {
      logService.error(requestId, "L", "errors", "mangoReturn: missing account id");

      return badRequest();
    }

    if ((applicationId == null) || applicationId.equals("")) {
      logService.error(requestId, "L", "errors", "mangoReturn: missing application id");

      return badRequest();
    }

    if (request.getQueryString("transactionId") == null) {
      logService.error(requestId, "L", "errors", "mangoReturn: missing transactionId id");
      return badRequest();
    }

    Account sessionAccount = accountService.getAccountByAccountId(requestId, accountId);
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "mangoReturn: account not found");
      return badRequest();
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, applicationId);
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "mangoReturn: application not found");
      return badRequest();
    }

    // process mango return
    PayIn payIn =
        transactionIncomingService.getPayInByProviderId(
            requestId, sessionAccount, request.getQueryString("transactionId"));

    if (payIn == null) {
      logService.error(requestId, "L", "errors", "mangoReturn: pay in not found");
      return badRequest();
    }

    logService.debug(
        requestId,
        "OUT",
        "L",
        "mangoReturn: redirect to "
            + payIn.getReturnURL()
            + "?transactionId="
            + payIn.getId().toString());

    // reply to mangoPay hook
    return redirect(payIn.getReturnURL() + "?transactionId=" + payIn.getId().toString());
  }

  @CustomStart
  public Result pfsRtf(Request request, String accountId, String applicationId) {

    String zoneIdSetting = ConfigFactory.load().getString("pfs.serverZoneId");
    ZoneId pfsServerZoneId =
        StringUtils.isNotBlank(zoneIdSetting) ? ZoneId.of(zoneIdSetting) : ZoneId.systemDefault();

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "GET POST /callbacks/pfsRtf");
    logService.debug(requestId, "IN", "start", "pfs:accountId: " + accountId);
    logService.debug(requestId, "IN", "start", "pfs:applicationId: " + applicationId);

    // initialise variables
    Callback newCallback = new Callback();
    HashMap<String, Object> callbackParameters = new HashMap<String, Object>();

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(requestId, request);
    logService.debug(requestId, "L", "fullRequest", fullRequestString);

    // get JSON body
    // Map<String, String[]> mapBody = request.body().asFormUrlEncoded();

    // Move json to object
    Form<RequestPFSTransactionHook> restForm =
        formFactory
            .form(RequestPFSTransactionHook.class)
            .bindFromRequest(request, "username", "password", "data");

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return badRequest();
    }

    // validate success extract request object
    RequestPFSTransactionHook requestPFSTransactionHook = restForm.value().get();

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    RequestPFSTransactionHookData requestPFSTransactionHookData;
    try {
      requestPFSTransactionHookData =
          mapper.readValue(
              requestPFSTransactionHook.getData(), RequestPFSTransactionHookData.class);
    } catch (IOException e) {
      logService.error(requestId, "L", "errors", "requestPFSTransactionHookData");
      logService.error(requestId, "L", "errors", e.getMessage());
      return badRequest();
    }

    logService.debug(
        requestId, "L", "cardholderId", requestPFSTransactionHookData.getCardholderid());

    // perform some validations
    if ((accountId == null) || accountId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing account id");
      return badRequest();
    }

    if ((applicationId == null) || applicationId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing application id");
      return badRequest();
    }

    Account sessionAccount = accountService.getAccountByAccountId(requestId, accountId);
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "pfs: account not found");
      return badRequest();
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, applicationId);
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "pfs: application not found");
      return badRequest();
    }

    if (requestPFSTransactionHookData.getCardholderid() == null) {
      logService.error(requestId, "L", "errors", "pfs: card holder id is null");
      return badRequest();
    }

    String messageType = requestPFSTransactionHookData.getMessageType();

    if (messageType == null) {
      logService.error(requestId, "L", "errors", "pfs: card message type is null");
      return badRequest();
    }

    messageType = messageType.toLowerCase();
    String transCode = StringUtils.defaultString(requestPFSTransactionHookData.getTransCode());

    if (!shouldSendPFSRtfNotification(requestPFSTransactionHookData)) {
      logService.error(
          requestId,
          "L",
          "errors",
          String.format(
              "pfs: MESSAGETYPE is %s and TRANSCODE is %s, no callback needed",
              messageType, transCode));
      return ok();
    }

    String hookCurrencyCode = requestPFSTransactionHookData.getCurrencyCode();
    String hookTermCurrencyCode = requestPFSTransactionHookData.getTermCurrency();
    String currencyCode = hookCurrencyCode;

    String hookAmount1 = requestPFSTransactionHookData.getAmount1();
    String hookAmount2 = requestPFSTransactionHookData.getAmount2();
    String hookTranAmount = requestPFSTransactionHookData.getTranAmount();
    final String zero = "0000000000.00";
    String amount = hookAmount1;

    // don't save callback if transaction is FX and amount2 is empty
    if (!hookTermCurrencyCode.equals(hookCurrencyCode)) {
      logService.debug(requestId, "L", "errors", "pfs: currencies differ");

      if (hookTermCurrencyCode.equals("000")) {
        logService.debug(requestId, "L", "errors", "TermCurrency is 000");
        // if transaction is not fx, save Amount as amount1
        amount = hookAmount1;
      } else {
        // if transaction is fx, and amount 2 is not empty save Amount as amount2
        if (!zero.equals(hookAmount2)) {
          amount = hookAmount2;
        } else if (!zero.equals(hookTranAmount)) {
          amount = hookTranAmount;
          currencyCode = hookTermCurrencyCode;
        }
      }
    } else {
      // if transaction is not fx, save Amount as amount1
      amount = hookAmount1;
    }

    // build callback parameters
    callbackParameters.put("Amount", amount);
    // search for currency code in config
    String currency = getCurrency(requestId, currencyCode);
    callbackParameters.put("Currency", currency);

    callbackParameters.put("CardholderId", requestPFSTransactionHookData.getCardholderid());
    String dateFormat = "yyyyMMddHHmmss";
    callbackParameters.put(
        "Date",
        utilsService.stringDateToUTC(
            requestPFSTransactionHookData.getTransLogDateTime(), dateFormat, pfsServerZoneId));
    callbackParameters.put("EventType", NotificationType.PFS_CALLBACK_TRANSACTION);

    // build callback
    newCallback.setProvider("PFS");
    newCallback.setAccountId(accountId);
    newCallback.setApplicationId(applicationId);
    newCallback.setNoFails(0);
    newCallback.setTag(requestPFSTransactionHookData.getCardholderid());

    newCallback.setParameters(callbackParameters);

    // save callback
    callbackService.saveCallback(requestId, newCallback);
    logService.debug(requestId, "L", "callBackId", newCallback.getId());

    // reply to pfs hook
    logService.debug(requestId, "OUT", "end", "pfs: hook completed");
    return ok();
  }

  /**
   * The pairs MESSAGETYPE/TRANSCODE for whom to send notifications. For any other combination, we
   * will not send a notification.
   */
  private static Map<String, String[]> transactionsWithNotifications =
      new HashMap<String, String[]>();

  static {
    transactionsWithNotifications.put("15IR", new String[] {"320000"});
    transactionsWithNotifications.put("27IR", new String[] {"320000"});
    transactionsWithNotifications.put("15JR", new String[] {"230000"});
    transactionsWithNotifications.put("27JR", new String[] {"230000"});
    transactionsWithNotifications.put("27XR", new String[] {"370000"});
    transactionsWithNotifications.put(
        "0110", new String[] {"00..00", "01..00", "11..00", "2000.."});
    transactionsWithNotifications.put(
        "0430", new String[] {"00..00", "01..00", "11..00", "2000.."});
    transactionsWithNotifications.put("0410", new String[] {"01..00"});
  }

  /**
   * Decides if a notification (callback) will be sent for a PFSRtf event.
   *
   * @param requestPFSTransactionHookData
   * @return
   */
  private boolean shouldSendPFSRtfNotification(
      RequestPFSTransactionHookData requestPFSTransactionHookData) {
    String messageType = requestPFSTransactionHookData.getMessageType().toUpperCase();
    String transCode = StringUtils.defaultString(requestPFSTransactionHookData.getTransCode());

    if (transactionsWithNotifications.containsKey(messageType)) {
      String[] allowedTransCodes = transactionsWithNotifications.get(messageType);
      for (String allowedTransCode : allowedTransCodes) {
        if (transCode.matches(allowedTransCode)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Finds the currency ISO code for the request currency
   *
   * @param requestId
   * @param requestCurrency
   * @return
   */
  private String getCurrency(String requestId, String requestCurrency) {

    String currency = null;
    try {
      currency = config.getString("currencyCode." + requestCurrency);
      logService.error(requestId, "L", "currency", currency);
    } catch (Exception e) {
      logService.error(
          requestId, "L", "errors", "config not found: currencyCode." + requestCurrency);
      for (CurrencyIso currencyIso : CurrencyIso.values()) {
        if (String.valueOf(currencyIso).equals(requestCurrency)) {
          currency = requestCurrency;
          logService.info(requestId, "L", "exception currency", currency);
        }
      }
    }
    return currency;
  }

  @CustomStart
  public Result pfs(Request request, String accountId, String applicationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "POST /callbacks/pfs");
    logService.debug(requestId, "IN", "start", "pfs:accountId: " + accountId);
    logService.debug(requestId, "IN", "start", "pfs:applicationId: " + applicationId);

    // extract request string
    String fullRequestString = coreService.parseRequestToPlainText(requestId, request);
    logService.debug(requestId, "OUT", "fullRequest", fullRequestString);

    // Move json to object
    Form<RequestPFSCompanyWalletHook> restForm =
        formFactory.form(RequestPFSCompanyWalletHook.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return badRequest();
    }

    // validate success extract request object
    RequestPFSCompanyWalletHook requestPFSCompanyWalletHook = restForm.value().get();

    logService.debug(requestId, "L", "cardholderId", requestPFSCompanyWalletHook.getCardholderId());
    logService.debug(requestId, "L", "amount", requestPFSCompanyWalletHook.getAmount());
    logService.debug(requestId, "L", "currency", requestPFSCompanyWalletHook.getCurrency());
    logService.debug(
        requestId, "L", "transactionId", requestPFSCompanyWalletHook.getTransactionId());
    logService.debug(requestId, "L", "trantype", requestPFSCompanyWalletHook.getTrantype());
    logService.debug(requestId, "L", "balance", requestPFSCompanyWalletHook.getBalance());

    // perform some validations
    if ((accountId == null) || accountId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing account id");
      return badRequest();
    }

    if ((applicationId == null) || applicationId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing application id");
      return badRequest();
    }

    Account sessionAccount = accountService.getAccountByAccountId(requestId, accountId);
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "pfs: account not found");
      return badRequest();
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, applicationId);
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "pfs: application not found");
      return badRequest();
    }

    // build callback
    Callback newCallback = new Callback();
    newCallback.setProvider("PFS");
    newCallback.setAccountId(accountId);
    newCallback.setApplicationId(applicationId);
    newCallback.setNoFails(0);
    newCallback.setTag(requestPFSCompanyWalletHook.getCardholderId());

    // build callback parameters
    HashMap<String, Object> callbackParameters = new HashMap<String, Object>();
    callbackParameters.put("CardholderId", requestPFSCompanyWalletHook.getCardholderId());
    callbackParameters.put("Amount", requestPFSCompanyWalletHook.getAmount());
    callbackParameters.put("Currency", requestPFSCompanyWalletHook.getCurrency());
    callbackParameters.put("TransactionId", requestPFSCompanyWalletHook.getTransactionId());
    callbackParameters.put("Trantype", requestPFSCompanyWalletHook.getTrantype());
    callbackParameters.put("Balance", requestPFSCompanyWalletHook.getBalance());
    callbackParameters.put("EventType", NotificationType.PFS_CALLBACK_COMPANY_WALLET);

    newCallback.setParameters(callbackParameters);

    // save callback
    callbackService.saveCallback(requestId, newCallback);
    logService.debug(requestId, "L", "callBackId", newCallback.getId());

    // reply to mango hook
    logService.debug(requestId, "OUT", "end", "pfs: hook completed");
    return ok();
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getProcessedCallback(
      Request request, String accountId, String applicationId, String callbackId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId,
        "IN",
        "start",
        "GET /callbacks/processed/" + accountId + "/" + applicationId + "/" + callbackId);

    if ((accountId == null) || accountId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing account id");
      return badRequest();
    }

    if ((applicationId == null) || applicationId.equals("")) {
      logService.error(requestId, "L", "errors", "pfs: missing application id");
      return badRequest();
    }

    Account sessionAccount = accountService.getAccountByAccountId(requestId, accountId);
    if (sessionAccount == null) {
      logService.error(requestId, "L", "errors", "pfs: account not found");
      return badRequest();
    }

    // get session application
    Application sessionApplication =
        applicationService.getApplicationByApplicationId(requestId, sessionAccount, applicationId);
    if (sessionApplication == null) {
      logService.error(requestId, "L", "errors", "pfs: application not found");
      return badRequest();
    }

    // check callbackId
    if ((callbackId == null) || callbackId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INVALID_CALLBACKID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INVALID_CALLBACKID, requestId);
    }

    // check id is valid
    if (!callbackId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INVALID_CALLBACKID_FORMAT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INVALID_CALLBACKID_FORMAT, requestId);
    }

    ProcessedCallback processedCallback =
        processedCallbackService.getProcessedCallback(requestId, callbackId);
    if (processedCallback == null) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INEXISTENT_PROCESSEDCALLBACK);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETPROCESSEDCALLBACK_INEXISTENT_PROCESSEDCALLBACK, requestId);
    }

    // return transaction
    ResponseProcessedCallback responseProcessedCallback = new ResponseProcessedCallback();
    responseProcessedCallback.setProcessedCallback(processedCallback);

    return coreService.getResponse(responseProcessedCallback, requestId);
  }
}
