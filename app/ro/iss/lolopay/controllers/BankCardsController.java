package ro.iss.lolopay.controllers;

import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.classes.ExecuteCardWalletsTrade;
import ro.iss.lolopay.classes.FxQuote;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.BankCardType;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.UserType;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.BankCard;
import ro.iss.lolopay.models.database.BankCardTransaction;
import ro.iss.lolopay.models.database.BankCardWallet;
import ro.iss.lolopay.models.database.CompanyBankCardWallet;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.BankCardService;
import ro.iss.lolopay.models.services.definition.BankCardWalletService;
import ro.iss.lolopay.models.services.definition.CompanyBankCardWalletService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.requests.RequestAddBankCardCurrency;
import ro.iss.lolopay.requests.RequestBankCardSendPin;
import ro.iss.lolopay.requests.RequestBankCardTransfer;
import ro.iss.lolopay.requests.RequestBankPayment;
import ro.iss.lolopay.requests.RequestChangeBankCardStatus;
import ro.iss.lolopay.requests.RequestCreateBankCard;
import ro.iss.lolopay.requests.RequestExecuteCardWalletsTrade;
import ro.iss.lolopay.requests.RequestFxQuote;
import ro.iss.lolopay.requests.RequestLockUnlockCard;
import ro.iss.lolopay.requests.RequestReplaceCard;
import ro.iss.lolopay.requests.RequestUpdateBankCard;
import ro.iss.lolopay.requests.RequestUpgradeBankCard;
import ro.iss.lolopay.responses.ResponseBankCard;
import ro.iss.lolopay.responses.ResponseBankCardWallet;
import ro.iss.lolopay.responses.ResponseBankCardWalletTransactions;
import ro.iss.lolopay.responses.ResponseCardCvv;
import ro.iss.lolopay.responses.ResponseCardExpiryDate;
import ro.iss.lolopay.responses.ResponseCardNumber;
import ro.iss.lolopay.responses.ResponseCompantBankCardWallets;
import ro.iss.lolopay.responses.ResponseExecuteCardWalletsTrade;
import ro.iss.lolopay.responses.ResponseFxQuote;
import ro.iss.lolopay.responses.ResponseRecordId;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.UtilsService;

public class BankCardsController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject UserService userService;

  @Inject BusinessService businessService;

  @Inject BankCardService bankCardService;

  @Inject BankCardWalletService bankCardWalletService;

  @Inject UtilsService utilsService;

  @Inject LogService logService;

  @Inject CompanyBankCardWalletService companyBankCardWalletService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result addCurrency(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/addCurrency");

    // Move json to object
    Form<RequestAddBankCardCurrency> restForm =
        formFactory.form(RequestAddBankCardCurrency.class).bindFromRequest(request);

    // validate input per form
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestAddBankCardCurrency requestAddBankCardCurrency = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(
            requestId, sessionAccount, requestAddBankCardCurrency.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_ADDCARDCURRENCY_INEXISTENT_CARD);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_ADDCARDCURRENCY_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_ADDCARDCURRENCY_CARD_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_ADDCARDCURRENCY_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get card wallets from database
    PaginatedList paginatedList =
        bankCardWalletService.getBankCardWallets(requestId, sessionAccount, bankCard.getId());

    // map card wallets to list
    @SuppressWarnings("unchecked")
    List<BankCardWallet> currentCardWallets = (List<BankCardWallet>) paginatedList.getList();

    for (BankCardWallet bankCardWallet : currentCardWallets) {
      if (bankCardWallet
          .getCurrency()
          .toString()
          .equals(requestAddBankCardCurrency.getCurrency())) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_ADDCARDCURRENCY_DUPLICATED_CURRENCY);
        return coreService.getErrorResponse(
            ErrorMessage.ERROR_ADDCARDCURRENCY_DUPLICATED_CURRENCY, requestId);
      }
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.addBankCardCurrency(
          requestId, sessionAccount, sessionApplication, bankCard, requestAddBankCardCurrency);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result changeStatus(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/changeStatus");

    // Move json to object
    Form<RequestChangeBankCardStatus> restForm =
        formFactory.form(RequestChangeBankCardStatus.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestChangeBankCardStatus requestChangeBankCardStatus = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(
            requestId, sessionAccount, requestChangeBankCardStatus.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CHANGECARDSTATUS_INEXISTENT_CARD);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CHANGECARDSTATUS_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CHANGECARDSTATUS_CARD_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CHANGECARDSTATUS_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card status (Old card status from request must match current card status to be
    // changed)
    if (!bankCard.getStatus().toString().equals(requestChangeBankCardStatus.getOldStatus())) {
      logService.debug(requestId, "L", "currentStatus", bankCard.getStatus());
      logService.debug(
          requestId, "L", "requestedOldStatus", requestChangeBankCardStatus.getOldStatus());

      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CHANGECARDSTATUS_CARD_STATUS_MISSMATCH);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CHANGECARDSTATUS_CARD_STATUS_MISSMATCH, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.changeStatusBankCard(
          requestId,
          sessionAccount,
          sessionApplication,
          bankCard,
          BankCardStatus.valueOf(requestChangeBankCardStatus.getOldStatus()),
          BankCardStatus.valueOf(requestChangeBankCardStatus.getNewStatus()));

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result upgradeCard(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/upgrade");

    // Move json to object
    Form<RequestUpgradeBankCard> restForm =
        formFactory.form(RequestUpgradeBankCard.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestUpgradeBankCard requestUpgradeBankCard = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestUpgradeBankCard.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPGRADEBANKCARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPGRADEBANKCARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (!bankCard.getType().equals(BankCardType.VIRTUAL)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_NOT_VIRTUAL);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_NOT_VIRTUAL, requestId);
    }

    // test bank card type
    if (!bankCard.getStatus().equals(BankCardStatus.OPEN)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPGRADEBANKCARD_CARD_INVALID_STATUS, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.upgradeBankCard(
          requestId, sessionAccount, sessionApplication, bankCard, requestUpgradeBankCard);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createCard(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/create");

    // Move json to object
    Form<RequestCreateBankCard> restForm =
        formFactory.form(RequestCreateBankCard.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateBankCard requestCreateBankCard = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateBankCard.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKCARD_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKCARD_USERID_INEXISTENT, requestId);
    }

    // test user type
    if (!existingUser.getType().equals(UserType.NATURAL)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKCARD_INVALID_USER_TYPE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKCARD_INVALID_USER_TYPE, requestId);
    }

    // check if user is registered to provider
    if ((existingUser.getProviderId() == null) || (existingUser.getProviderId().equals(""))) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKCARD_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKCARD_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check if user has address
    if (existingUser.getAddress() == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEBANKCARD_USER_MISSING_ADDRESS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEBANKCARD_USER_MISSING_ADDRESS, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      BankCard newBankCard =
          businessService.createBankCard(
              requestId, sessionAccount, sessionApplication, existingUser, requestCreateBankCard);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(newBankCard.getId());

      // return response
      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result executeBankPayment(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/bankPayment");

    // Move json to object
    Form<RequestBankPayment> restForm =
        formFactory.form(RequestBankPayment.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestBankPayment requestBankPayment = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestBankPayment.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_BANKPAYMENT_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_BANKPAYMENT_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_BANKPAYMENT_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_BANKPAYMENT_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (!bankCard.getStatus().equals(BankCardStatus.OPEN)
        && !bankCard.getStatus().equals(BankCardStatus.ISSUED)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_BANKPAYMENT_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_BANKPAYMENT_CARD_INVALID_STATUS, requestId);
    }

    try {
      businessService.executeBankPayment(requestId, sessionAccount, bankCard, requestBankPayment);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result replaceCard(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/getCurrencyFxQuote");

    // Move json to object
    Form<RequestReplaceCard> restForm =
        formFactory.form(RequestReplaceCard.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestReplaceCard requestReplaceCard = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestReplaceCard.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_REPLACECARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REPLACECARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REPLACECARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REPLACECARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (!bankCard.getStatus().equals(BankCardStatus.OPEN)
        && !bankCard.getStatus().equals(BankCardStatus.BLOCKED_PAYOUT)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_REPLACECARD_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_REPLACECARD_CARD_INVALID_STATUS, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // get new card holder id created
      BankCard bankCardNew =
          businessService.replaceCard(
              requestId,
              sessionAccount,
              sessionApplication,
              bankCard,
              requestReplaceCard.getReason());

      // create response
      ResponseBankCard responseBankCard = new ResponseBankCard();
      responseBankCard.setBankCard(bankCardNew);

      // return response
      return coreService.getResponse(responseBankCard, requestId);

    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCurrencyFxQuote(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/getCurrencyFxQuote");

    // Move json to object
    Form<RequestFxQuote> restForm = formFactory.form(RequestFxQuote.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestFxQuote requestFxQuote = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestFxQuote.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_FXQUOTE_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_FXQUOTE_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_FXQUOTE_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_FXQUOTE_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      FxQuote fxQuote =
          businessService.getCurrencyFxQuote(requestId, sessionAccount, bankCard, requestFxQuote);

      logService.debug(requestId, "L", "fxQuote.currencyFrom", fxQuote.getCurrencyFrom());
      logService.debug(requestId, "L", "fxQuote.currencyTo", fxQuote.getCurrencyTo());
      logService.debug(requestId, "L", "fxQuote.amount", fxQuote.getAmount());
      logService.debug(requestId, "L", "fxQuote.rate", fxQuote.getRate());

      // create response
      ResponseFxQuote responseFxQuote = new ResponseFxQuote();
      responseFxQuote.setFxQuote(fxQuote);

      logService.debug(requestId, "L", "responseFxQuote", responseFxQuote);

      // return response
      return coreService.getResponse(responseFxQuote, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result executeCardWalletsTrade(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/executeCardWalletsTrade");

    // Move json to object
    Form<RequestExecuteCardWalletsTrade> restForm =
        formFactory.form(RequestExecuteCardWalletsTrade.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());
      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestExecuteCardWalletsTrade requestExecuteCardWalletsTrade = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(
            requestId, sessionAccount, requestExecuteCardWalletsTrade.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_INEXISTENT_CARD);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARD_NOT_REGISTERED2PROVIDER);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (!bankCard.getStatus().equals(BankCardStatus.OPEN)
        && !bankCard.getStatus().equals(BankCardStatus.ISSUED)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_EXECUTECARDWALLETSTRADE_CARD_INVALID_STATUS, requestId);
    }

    try {
      ExecuteCardWalletsTrade executeCardWalletsTrade =
          businessService.executeCardWalletsTrade(
              requestId, sessionAccount, bankCard, requestExecuteCardWalletsTrade);

      // create response
      ResponseExecuteCardWalletsTrade responseExecuteCardWalletsTrade =
          new ResponseExecuteCardWalletsTrade();
      responseExecuteCardWalletsTrade.setExecuteCardWalletsTrade(executeCardWalletsTrade);

      logService.debug(
          requestId, "L", "responseExecuteCardWalletsTrade", responseExecuteCardWalletsTrade);

      return coreService.getResponse(responseExecuteCardWalletsTrade, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result get(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId);

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.getBankCard(requestId, sessionAccount, sessionApplication, bankCard);
    } catch (GenericRestException gre) {
      // TODO smart log in here
      logService.error(
          requestId,
          "L",
          "errors",
          "card:get: failed getting data from provider (last version of the record returned)"
              + gre.getResponseErrors().get(0).getErrorDescription());
    }

    // create response
    ResponseBankCard responseBankCard = new ResponseBankCard();
    responseBankCard.setBankCard(bankCard);

    // return response
    return coreService.getResponse(responseBankCard, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result retrieveManuallyReissuedCard(
      Request request, String cardId, String reissuedCardProviderId) {
    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId,
        "IN",
        "start",
        "GET /cards/" + cardId + "/retrieveManuallyReissuedCard/" + reissuedCardProviderId);

    // check reissuedCardProviderId
    if (StringUtils.isBlank(reissuedCardProviderId)) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_RETRIEVE_MAN_REISSUED_CARD_INVALID_PROVIDERID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_RETRIEVE_MAN_REISSUED_CARD_INVALID_PROVIDERID, requestId);
    }

    // check card id
    if (StringUtils.isBlank(cardId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    BankCard reissuedCard =
        bankCardService.getBankCardByProviderId(requestId, sessionAccount, reissuedCardProviderId);
    if (null != reissuedCard) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_RETRIEVE_MAN_REISSUED_CARD_ALREADY_RETRIEVED);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_RETRIEVE_MAN_REISSUED_CARD_ALREADY_RETRIEVED, requestId);
    }

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // retrieve the reissued card from provider
      reissuedCard =
          businessService.retrieveManuallyReissuedCard(
              requestId, sessionAccount, sessionApplication, bankCard, reissuedCardProviderId);

      if (reissuedCard.getCurrencies() != null) {
        // retrieve the wallets for the reissued card from provider
        for (String currencyCode : reissuedCard.getCurrencies()) {
          businessService.retrieveWalletForManuallyReissuedCard(
              requestId, sessionAccount, sessionApplication, reissuedCard, currencyCode);
        }
      }
    } catch (GenericRestException gre) {
      logService.error(
          requestId,
          "L",
          "errors",
          "card:get: failed getting data from provider (last version of the record returned)"
              + gre.getResponseErrors().get(0).getErrorDescription());
      return coreService.getErrorResponse(gre, requestId);
    }

    // create response
    ResponseBankCard responseBankCard = new ResponseBankCard();
    responseBankCard.setBankCard(reissuedCard);

    // return response
    return coreService.getResponse(responseBankCard, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardWallet(Request request, String cardId, String currency) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId + "/wallet/" + currency);

    // TODO Can't enter here
    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_INVALID_CARDID, requestId);
    }

    // TODO Can't enter here
    // check currency 1
    if ((currency == null) || currency.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_INVALID_CURRENCY);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_INVALID_CURRENCY, requestId);
    }

    // check currency 2
    if (!currency.equals("EUR") && !currency.equals("USD") && !currency.equals("GBP")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_CURRENCY_NOT_ALLOWED);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_CURRENCY_NOT_ALLOWED, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get card wallet from database
    BankCardWallet bankCardWallet =
        bankCardWalletService.getBankCardWallet(
            requestId, sessionAccount, cardId, CurrencyISO.valueOf(currency));

    // test if such requested wallet exists
    if (bankCardWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLET_INEXISTENT_CARDWALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLET_INEXISTENT_CARDWALLET, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // update card from provider
      businessService.getBankCardWallet(
          requestId, sessionAccount, sessionApplication, bankCard, bankCardWallet);
    } catch (GenericRestException gre) {
      // TODO smart log in here
      logService.error(
          requestId,
          "L",
          "errors",
          "cardWallet:get: failed getting data from provider (last version of the record returned)"
              + gre.getResponseErrors().get(0).getErrorDescription());
    }

    // create response
    ResponseBankCardWallet responseBankCardWallet = new ResponseBankCardWallet();
    responseBankCardWallet.setBankCardWallet(bankCardWallet);

    // return response
    return coreService.getResponse(responseBankCardWallet, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardWalletTransactions(
      Request request, String cardId, String currency, long startDate, long endDate) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(
        requestId,
        "IN",
        "start",
        "GET /cards/"
            + cardId
            + "/wallet/"
            + currency
            + "/transactions/"
            + startDate
            + "/"
            + endDate);

    // check start date
    if (!utilsService.isValidTransactionTimeStamp(startDate)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_STARTDATE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_STARTDATE, requestId);
    }

    // check end date
    if (!utilsService.isValidTransactionTimeStamp(endDate)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_ENDDATE);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_ENDDATE, requestId);
    }

    // check end date
    if (startDate > endDate) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_PERIOD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_PERIOD, requestId);
    }

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CARDID, requestId);
    }

    // check currency 1
    if ((currency == null) || currency.equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INVALID_CURRENCY, requestId);
    }

    // check currency 2
    if (!currency.equals("EUR") && !currency.equals("USD") && !currency.equals("GBP")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_CURRENCY_NOT_ALLOWED);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_CURRENCY_NOT_ALLOWED, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_GETCARDWALLETTRANS_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get card wallet from database
    BankCardWallet bankCardWallet =
        bankCardWalletService.getBankCardWallet(
            requestId, sessionAccount, cardId, CurrencyISO.valueOf(currency));

    // test if such requested wallet exists
    if (bankCardWallet == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDWALLETTRANS_INEXISTENT_CARDWALLET);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDWALLETTRANS_INEXISTENT_CARDWALLET, requestId);
    }

    // get running application
    Application application = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // update card from provider
      List<BankCardTransaction> bankCardWalletTransactions =
          businessService.getBankCardWalletTransaction(
              requestId, sessionAccount, bankCard, bankCardWallet, application, startDate, endDate);

      // create response
      ResponseBankCardWalletTransactions responseBankCardWalletTransactions =
          new ResponseBankCardWalletTransactions();
      responseBankCardWalletTransactions.setBankCardTransactions(bankCardWalletTransactions);

      // return response
      return coreService.getResponse(responseBankCardWalletTransactions, requestId);

    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardNumberLocal(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId + "/numberLocal");

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNOLOCAL_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDNOLOCAL_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNOLOCAL_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDNOLOCAL_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNOLOCAL_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDNOLOCAL_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNOLOCAL_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDNOLOCAL_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      // create response
      ResponseCardNumber responseCardNumber = new ResponseCardNumber();
      if (bankCard.getSensitiveCardNumber() != null) {
        responseCardNumber.setCardNumber(bankCard.getSensitiveCardNumber());
      } else {
        responseCardNumber.setCardNumber("**** **** **** ****");
      }

      return coreService.getResponse(responseCardNumber, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardNumber(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId + "/number");

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNO_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDNO_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNO_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDNO_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNO_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDNO_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDNO_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDNO_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      String bankCardNo =
          businessService.getBankCardNumber(
              requestId, sessionAccount, sessionApplication, bankCard);

      // create response
      ResponseCardNumber responseCardNumber = new ResponseCardNumber();
      responseCardNumber.setCardNumber(bankCardNo);

      // clean sensitive variable
      bankCardNo = null;

      return coreService.getResponse(responseCardNumber, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardExpiryDate(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId + "/expiryDate");

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDEXPDATE_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDEXPDATE_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDEXPDATE_INVALID_CARDID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDEXPDATE_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDEXPDATE_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDEXPDATE_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDEXPDATE_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDEXPDATE_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      String bankCardExpiryDate =
          businessService.getBankCardExpiryDate(requestId, sessionAccount, bankCard);

      // create response
      ResponseCardExpiryDate responseCardExpiryDate = new ResponseCardExpiryDate();
      responseCardExpiryDate.setExpiryDate(bankCardExpiryDate);

      // clean sensitive variable
      bankCardExpiryDate = null;

      return coreService.getResponse(responseCardExpiryDate, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getCardCVV(Request request, String cardId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/" + cardId + "/cvv");

    // check card id
    if ((cardId == null) || cardId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDCVV_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDCVV_INVALID_CARDID, requestId);
    }

    // check id is valid
    if (!cardId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDCVV_INVALID_CARDID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDCVV_INVALID_CARDID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard = bankCardService.getBankCard(requestId, sessionAccount, cardId);

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETCARDCVV_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETCARDCVV_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETCARDCVV_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETCARDCVV_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      String bankCardCvv = businessService.getBankCardCVV(requestId, sessionAccount, bankCard);

      // create response
      ResponseCardCvv responseCardCvv = new ResponseCardCvv();
      responseCardCvv.setCvv(bankCardCvv);

      // clean sensitive variable
      bankCardCvv = null;

      return coreService.getResponse(responseCardCvv, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result sendPin(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/sendPin");

    // Move JSON to object
    Form<RequestBankCardSendPin> restForm =
        formFactory.form(RequestBankCardSendPin.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestBankCardSendPin requestBankCardSendPin = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestBankCardSendPin.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SENDPIN_INEXISTENT_CARD);

      return coreService.getErrorResponse(ErrorMessage.ERROR_SENDPIN_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SENDPIN_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SENDPIN_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    try {
      businessService.sendPin(requestId, sessionAccount, bankCard);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result lockUnlock(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/lockUnlock");

    // Move json to object
    Form<RequestLockUnlockCard> restForm =
        formFactory.form(RequestLockUnlockCard.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestLockUnlockCard requestLockUnlockCard = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestLockUnlockCard.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card status (Old card status from request must match current card status to be
    // changed)
    if (!bankCard.getStatus().toString().equals(requestLockUnlockCard.getOldStatus())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH, requestId);
    }

    // test bank card status (Old card status can be OPEN or BLOCKED_PAYOUT)
    if (!requestLockUnlockCard.getOldStatus().equals(BankCardStatus.OPEN.toString())
        && !requestLockUnlockCard.getOldStatus().equals(BankCardStatus.BLOCKED_PAYOUT.toString())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH, requestId);
    }

    // test bank card status (New card status can be OPEN or BLOCKED_PAYOUT)
    if (!requestLockUnlockCard.getNewStatus().equals(BankCardStatus.OPEN.toString())
        && !requestLockUnlockCard.getNewStatus().equals(BankCardStatus.BLOCKED_PAYOUT.toString())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_MISSMATCH, requestId);
    }

    // test bank card status (for old status OPEN only BLOCKED_PAYOUT is accepted)
    if (bankCard.getStatus().equals(BankCardStatus.OPEN)
        && !requestLockUnlockCard.getNewStatus().equals(BankCardStatus.BLOCKED_PAYOUT.toString())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_INVALID, requestId);
    }

    // test bank card status (for old status BLOCKED_PAYOUT only OPEN is accepted)
    if (bankCard.getStatus().equals(BankCardStatus.BLOCKED_PAYOUT)
        && !requestLockUnlockCard.getNewStatus().equals(BankCardStatus.OPEN.toString())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_LOCKUNLOCKCARD_CARD_STATUS_INVALID, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.lockUnlockCard(
          requestId,
          sessionAccount,
          sessionApplication,
          bankCard,
          BankCardStatus.valueOf(requestLockUnlockCard.getOldStatus()),
          BankCardStatus.valueOf(requestLockUnlockCard.getNewStatus()));

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result transferTo(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/transferTo");

    // Move JSON to object
    Form<RequestBankCardTransfer> restForm =
        formFactory.form(RequestBankCardTransfer.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestBankCardTransfer requestBankCardTransferTo = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(
            requestId, sessionAccount, requestBankCardTransferTo.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (!bankCard.getStatus().equals(BankCardStatus.ISSUED)
        && !bankCard.getStatus().equals(BankCardStatus.OPEN)
        && !bankCard.getStatus().equals(BankCardStatus.BLOCKED_PAYOUT)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CARD_INVALID_STATUS, requestId);
    }

    // test allowed currencies only
    if (!requestBankCardTransferTo.getCurrency().equals("EUR")
        && !requestBankCardTransferTo.getCurrency().equals("USD")
        && !requestBankCardTransferTo.getCurrency().equals("GBP")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_NOT_ALLOWED);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_NOT_ALLOWED, requestId);
    }

    // check if there is a wallet created for this currency
    if ((bankCard.getCurrencies() != null)
        && !bankCard.getCurrencies().contains(requestBankCardTransferTo.getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_WALLET_CURRENCY);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_WALLET_CURRENCY, requestId);
    }

    // check transfer amounts from request
    if (requestBankCardTransferTo.getAmount() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_INVALID, requestId);
    }

    try {
      businessService.transferToBankCard(
          requestId, sessionAccount, bankCard, requestBankCardTransferTo);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result transferFrom(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/transferFrom");

    // Move JSON to object
    Form<RequestBankCardTransfer> restForm =
        formFactory.form(RequestBankCardTransfer.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestBankCardTransfer requestBankCardTransferTo = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard bankCard =
        bankCardService.getBankCard(
            requestId, sessionAccount, requestBankCardTransferTo.getCardId());

    // test record
    if (bankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((bankCard.getProviderId() == null) || bankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test bank card type
    if (bankCard.getStatus().equals(BankCardStatus.LOST)
        || bankCard.getStatus().equals(BankCardStatus.STOLEN)
        || bankCard.getStatus().equals(BankCardStatus.EXPIRED)
        || bankCard.getStatus().equals(BankCardStatus.BLOCKED_FINAL)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CARD_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CARD_INVALID_STATUS, requestId);
    }

    // test allowed currencies only
    if (!requestBankCardTransferTo.getCurrency().equals("EUR")
        && !requestBankCardTransferTo.getCurrency().equals("USD")
        && !requestBankCardTransferTo.getCurrency().equals("GBP")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_NOT_ALLOWED);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_CURRENCY_NOT_ALLOWED, requestId);
    }

    // check if there is a wallet created for this currency
    if ((bankCard.getCurrencies() != null)
        && !bankCard.getCurrencies().contains(requestBankCardTransferTo.getCurrency())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_WALLET_CURRENCY);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_INEXISTENT_WALLET_CURRENCY, requestId);
    }

    // check transfer amounts from request
    if (requestBankCardTransferTo.getAmount() <= 0) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_INVALID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CARDTRANSFER_AMOUNT_INVALID, requestId);
    }

    try {
      businessService.transferFromBankCard(
          requestId, sessionAccount, bankCard, requestBankCardTransferTo);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(bankCard.getId());

      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result update(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /cards/update");

    // Move json to object
    Form<RequestUpdateBankCard> restForm =
        formFactory.form(RequestUpdateBankCard.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestUpdateBankCard requestUpdateBankCard = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get card
    BankCard existingBankCard =
        bankCardService.getBankCard(requestId, sessionAccount, requestUpdateBankCard.getCardId());

    // test record
    if (existingBankCard == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_UPDATEBANKCARD_INEXISTENT_CARD);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATEBANKCARD_INEXISTENT_CARD, requestId);
    }

    // test provider id
    if ((existingBankCard.getProviderId() == null) || existingBankCard.getProviderId().equals("")) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPDATEBANKCARD_CARD_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATEBANKCARD_CARD_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, existingBankCard.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_UPDATEBANKCARD_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_UPDATEBANKCARD_USERID_INEXISTENT, requestId);
    }

    if (existingBankCard.isPhysicalCard() && !existingBankCard.deliveryAddressIsValid()) {
      logService.info(
          requestId,
          "L",
          "deliveryAddress not valid",
          "this might be ok for PHYSICAL card applications created before api7");
    }

    if (!existingBankCard.isPhysicalCard() || !existingBankCard.deliveryAddressIsValid()) {
      // check if user has address for VIRTUAL cards or for PHYSICAL cards applications created
      // before api7
      if (existingUser.getAddress() == null) {
        logService.error(
            requestId, "L", "errors", ErrorMessage.ERROR_UPDATEBANKCARD_USER_MISSING_ADDRESS);

        return coreService.getErrorResponse(
            ErrorMessage.ERROR_UPDATEBANKCARD_USER_MISSING_ADDRESS, requestId);
      }
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      businessService.updateBankCard(
          requestId,
          sessionAccount,
          sessionApplication,
          existingUser,
          existingBankCard,
          requestUpdateBankCard);

      // create response
      ResponseRecordId responseRecordId = new ResponseRecordId();
      responseRecordId.setId(existingBankCard.getId());

      // return response
      return coreService.getResponse(responseRecordId, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result companyWallets(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /cards/companyWallets");

    // retrieve account from session
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get company wallets
    List<CompanyBankCardWallet> companyBankCardWalletsList =
        companyBankCardWalletService.getCompanyBankCardWallets(requestId, sessionAccount);

    ResponseCompantBankCardWallets responseCompantBankCardWallets =
        new ResponseCompantBankCardWallets();
    responseCompantBankCardWallets.setCompanyBankCardWallets(companyBankCardWalletsList);

    // return response
    return coreService.getResponse(responseCompantBankCardWallets, requestId);
  }
}
