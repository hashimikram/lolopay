package ro.iss.lolopay.controllers;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.BodyParser;
import play.mvc.Http.Request;
import play.mvc.Result;
import ro.iss.lolopay.annotations.CustomAuth;
import ro.iss.lolopay.annotations.CustomStart;
import ro.iss.lolopay.annotations.CustomValidJson;
import ro.iss.lolopay.annotations.CustomValidRequest;
import ro.iss.lolopay.application.Attrs;
import ro.iss.lolopay.classes.Json5MB;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.Ubo;
import ro.iss.lolopay.models.classes.UboDeclarationStatus;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.UboDeclaration;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.UboService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.requests.RequestUbo;
import ro.iss.lolopay.responses.ResponseUbo;
import ro.iss.lolopay.responses.ResponseUboDeclaration;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;

public class UboController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject UserService userService;

  @Inject UboService uboService;

  @Inject BusinessService businessService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createUboDeclaration(Request request, String userId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", request.method() + "" + request.uri());

    // validate input

    // check user id
    if (StringUtils.isBlank(userId)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_REQUIRED);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_REQUIRED, requestId);
    }

    // check user id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_INVALID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_INVALID, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser = userService.getUser(requestId, sessionAccount, userId);

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBODECLARATION_USERID_INEXISTENT, requestId);
    }

    if (StringUtils.isEmpty(existingUser.getProviderId())) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEUBODECLARATION_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBODECLARATION_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save ubo declaration
      UboDeclaration newUboDeclaration =
          businessService.createUboDeclaration(
              requestId, sessionAccount, sessionApplication, existingUser);

      // create response
      ResponseUboDeclaration responseUboDeclaration = new ResponseUboDeclaration();
      responseUboDeclaration.setDocument(newUboDeclaration);

      // return response
      return coreService.getResponse(responseUboDeclaration, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result submitUboDeclaration(Request request, String userId, String declarationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", request.method() + "" + request.uri());

    // validate input

    // check user id
    if (StringUtils.isBlank(userId)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_REQUIRED);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_REQUIRED, requestId);
    }

    // check user id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INVALID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INVALID, requestId);
    }

    // check declarationId id
    if (StringUtils.isBlank(declarationId)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_REQUIRED);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_REQUIRED, requestId);
    }

    // check user id is valid
    if (!declarationId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INVALID);
      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INVALID, requestId);
    }

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser = userService.getUser(requestId, sessionAccount, userId);

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_USERID_INEXISTENT, requestId);
    }

    if (StringUtils.isEmpty(existingUser.getProviderId())) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_SUBMITUBODECLARATION_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if we have a valid ubo declaration
    UboDeclaration existingUboDeclaration =
        uboService.getUboDeclaration(requestId, sessionAccount, declarationId);

    // test ubo declaration existence
    if (existingUboDeclaration == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCID_INEXISTENT, requestId);
    }

    // test if Ubo declaration has a valid status
    if (!(existingUboDeclaration.getStatus().equals(UboDeclarationStatus.CREATED)
        || existingUboDeclaration.getStatus().equals(UboDeclarationStatus.INCOMPLETE))) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITUBODECLARATION_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_INVALID_STATUS, requestId);
    }

    // test if ubo declaration is registered to provider
    if ((existingUboDeclaration.getProviderId() == null)
        || existingUboDeclaration.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCUMENT_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITUBODECLARATION_DOCUMENT_NOT_REGISTERED2PROVIDER, requestId);
    }

    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // process user save
      businessService.submitUboDeclaration(
          requestId, sessionAccount, sessionApplication, existingUser, existingUboDeclaration);

      // create response
      ResponseUboDeclaration responseUboDeclaration = new ResponseUboDeclaration();
      responseUboDeclaration.setDocument(existingUboDeclaration);

      // return response
      return coreService.getResponse(responseUboDeclaration, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result getUboDeclaration(Request request, String declarationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", request.method() + "" + request.uri());

    // check document id
    if (StringUtils.isBlank(declarationId)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETUBODECLARATION_INVALID_DECLARATIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETUBODECLARATION_INVALID_DECLARATIONID, requestId);
    }

    // check id is valid
    if (!declarationId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETUBODECLARATION_INVALID_DECLARATIONID);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETUBODECLARATION_INVALID_DECLARATIONID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get document
    UboDeclaration uboDeclaration =
        uboService.getUboDeclaration(requestId, sessionAccount, declarationId);

    // test record
    if (uboDeclaration == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETUBODECLARATION_INEXISTENT_DECLARATION);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETUBODECLARATION_INEXISTENT_DECLARATION, requestId);
    }

    // create response
    ResponseUboDeclaration responseUboDeclaration = new ResponseUboDeclaration();
    responseUboDeclaration.setDocument(uboDeclaration);

    // return response
    return coreService.getResponse(responseUboDeclaration, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @BodyParser.Of(Json5MB.class)
  public Result createUbo(Request request, String userId, String declarationId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", request.method() + "" + request.uri());

    // Move json to object
    Form<RequestUbo> restForm = formFactory.form(RequestUbo.class).bindFromRequest(request);

    // validate input

    // check user id
    if (StringUtils.isBlank(userId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_REQUIRED);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_USERID_REQUIRED, requestId);
    }

    // check user id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_USERID_INVALID, requestId);
    }

    // check declarationId id
    if (StringUtils.isBlank(declarationId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_REQUIRED);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_REQUIRED, requestId);
    }

    // check declaration id is valid
    if (!declarationId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_INVALID, requestId);
    }

    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestUbo requestCreateUbo = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser = userService.getUser(requestId, sessionAccount, userId);

    // test user existence
    if (existingUser == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_USERID_INEXISTENT, requestId);
    }

    if (StringUtils.isBlank(existingUser.getProviderId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if we have a valid ubo declaration
    UboDeclaration existingUboDeclaration =
        uboService.getUboDeclaration(requestId, sessionAccount, declarationId);

    // test ubo declaration existence
    if (existingUboDeclaration == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_INEXISTENT);

      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_INEXISTENT, requestId);
    }

    // test if ubo declaration has a valid status
    if (!existingUboDeclaration.getStatus().equals(UboDeclarationStatus.CREATED)
        && !existingUboDeclaration.getStatus().equals(UboDeclarationStatus.INCOMPLETE)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_INVALID_STATUS);

      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_INVALID_STATUS, requestId);
    }

    // test if ubo declaration is registered to provider
    if (StringUtils.isBlank(existingUboDeclaration.getProviderId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCUMENT_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_DOCUMENT_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // add page to document
      Ubo ubo =
          businessService.createUbo(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              existingUboDeclaration,
              requestCreateUbo);

      // create response
      ResponseUbo responseUbo = new ResponseUbo();
      responseUbo.setUbo(ubo);

      // return response
      return coreService.getResponse(responseUbo, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @BodyParser.Of(Json5MB.class)
  public Result updateUbo(Request request, String userId, String declarationId, String uboId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", request.method() + "" + request.uri());

    // check id
    if (StringUtils.isBlank(uboId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID, requestId);
    }

    // Move json to object
    Form<RequestUbo> restForm = formFactory.form(RequestUbo.class).bindFromRequest(request);

    // validate input

    // check user id
    if (StringUtils.isBlank(userId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_REQUIRED);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_USERID_REQUIRED, requestId);
    }

    // check user id is valid
    if (!userId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_USERID_INVALID, requestId);
    }

    // check declarationId id
    if (StringUtils.isBlank(declarationId)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_REQUIRED);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_REQUIRED, requestId);
    }

    // check declaration id is valid
    if (!declarationId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_INVALID, requestId);
    }

    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestUbo requestUbo = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser = userService.getUser(requestId, sessionAccount, userId);

    // test user existence
    if (existingUser == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_USERID_INEXISTENT, requestId);
    }

    if (StringUtils.isBlank(existingUser.getProviderId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if we have a valid ubo declaration
    UboDeclaration existingUboDeclaration =
        uboService.getUboDeclaration(requestId, sessionAccount, declarationId);

    // test ubo declaration existence
    if (existingUboDeclaration == null) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCID_INEXISTENT);

      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_DOCID_INEXISTENT, requestId);
    }

    // test if ubo declaration has a valid status
    if (!existingUboDeclaration.getStatus().equals(UboDeclarationStatus.CREATED)
        && !existingUboDeclaration.getStatus().equals(UboDeclarationStatus.INCOMPLETE)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_INVALID_STATUS);

      return coreService.getErrorResponse(ErrorMessage.ERROR_CREATEUBO_INVALID_STATUS, requestId);
    }

    // test if ubo declaration is registered to provider
    if (StringUtils.isBlank(existingUboDeclaration.getProviderId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEUBO_DOCUMENT_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEUBO_DOCUMENT_NOT_REGISTERED2PROVIDER, requestId);
    }

    // check id
    boolean uboExists = false;
    for (Ubo existingUbo : existingUboDeclaration.getUbos()) {
      if (existingUbo.getProviderId().equals(uboId)) {
        uboExists = true;
        break;
      }
    }
    if (!uboExists) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID);
      return coreService.getErrorResponse(ErrorMessage.ERROR_UPDATEUBO_UBOID_INVALID, requestId);
    }
    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // add page to ubo declaration
      Ubo ubo =
          businessService.updateUbo(
              requestId,
              sessionAccount,
              sessionApplication,
              existingUser,
              existingUboDeclaration,
              uboId,
              requestUbo);

      // create response
      ResponseUbo responseUbo = new ResponseUbo();
      responseUbo.setUbo(ubo);

      // return response
      return coreService.getResponse(responseUbo, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }
}
