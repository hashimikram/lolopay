package ro.iss.lolopay.controllers;

import java.util.List;
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
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.RestController;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.exceptions.GenericRestException;
import ro.iss.lolopay.models.classes.DocumentStatus;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.database.User;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.DocumentService;
import ro.iss.lolopay.models.services.definition.UserService;
import ro.iss.lolopay.requests.RequestCreateDocument;
import ro.iss.lolopay.requests.RequestCreateDocumentPage;
import ro.iss.lolopay.requests.RequestSubmitDocument;
import ro.iss.lolopay.responses.ResponseDocument;
import ro.iss.lolopay.responses.ResponseDocuments;
import ro.iss.lolopay.services.definition.BusinessService;
import ro.iss.lolopay.services.definition.CoreService;
import ro.iss.lolopay.services.definition.LogService;

public class DocumentController extends RestController {
  @Inject FormFactory formFactory;

  @Inject CoreService coreService;

  @Inject UserService userService;

  @Inject DocumentService documentService;

  @Inject BusinessService businessService;

  @Inject LogService logService;

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result createDocument(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);

    logService.debug(requestId, "IN", "start", "POST /documents/create");

    // Move json to object
    Form<RequestCreateDocument> restForm =
        formFactory.form(RequestCreateDocument.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateDocument requestCreateDocument = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateDocument.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDOCUMENT_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDOCUMENT_USERID_INEXISTENT, requestId);
    }

    if (StringUtils.isEmpty(existingUser.getProviderId())) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDOCUMENT_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDOCUMENT_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // save document
      Document newDocument =
          businessService.createDocument(
              requestId, sessionAccount, sessionApplication, existingUser, requestCreateDocument);

      // create response
      ResponseDocument responseDocument = new ResponseDocument();
      responseDocument.setDocument(newDocument);

      // return response
      return coreService.getResponse(responseDocument, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @BodyParser.Of(Json5MB.class)
  public Result createDocumentPage(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /documents/create/page");

    // Move json to object
    Form<RequestCreateDocumentPage> restForm =
        formFactory.form(RequestCreateDocumentPage.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestCreateDocumentPage requestCreateDocumentPage = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid user
    User existingUser =
        userService.getUser(requestId, sessionAccount, requestCreateDocumentPage.getUserId());

    // test user existence
    if (existingUser == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USERID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USERID_INEXISTENT, requestId);
    }

    if ((existingUser.getProviderId() == null) || existingUser.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USER_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDOCUMENTPAGE_USER_NOT_REGISTERED2PROVIDER, requestId);
    }

    // test if we have a valid document
    Document existingDocument =
        documentService.getDocument(
            requestId, sessionAccount, requestCreateDocumentPage.getDocumentId());

    // test document existence
    if (existingDocument == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_CREATEDOCUMENTPAGE_DOCID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_CREATEDOCUMENTPAGE_DOCID_INEXISTENT, requestId);
    }

    // test if document has a valid status
    if (!existingDocument.getStatus().equals(DocumentStatus.CREATED)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SUBMITDOCUMENT_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITDOCUMENT_INVALID_STATUS, requestId);
    }

    // test if document is registered to provider
    if ((existingDocument.getProviderId() == null) || existingDocument.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_SUBMITDOCUMENT_DOCUMENT_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITDOCUMENT_DOCUMENT_NOT_REGISTERED2PROVIDER, requestId);
    }

    // get running application
    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // add page to document
      businessService.createDocumentPage(
          requestId,
          sessionAccount,
          sessionApplication,
          existingUser,
          existingDocument,
          requestCreateDocumentPage.getFile());

      // remove big variable reference - it does not do shit
      requestCreateDocumentPage = null;

      // create response
      ResponseDocument responseDocument = new ResponseDocument();
      responseDocument.setDocument(existingDocument);

      // return response
      return coreService.getResponse(responseDocument, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result submitDocument(Request request) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "POST /documents/submit");

    // Move json to object
    Form<RequestSubmitDocument> restForm =
        formFactory.form(RequestSubmitDocument.class).bindFromRequest(request);

    // validate input
    if (restForm.hasErrors()) {
      logService.error(requestId, "L", "errors", restForm.errorsAsJson().toString());

      return coreService.getErrorResponse(
          coreService.getErrorResponseList(restForm.errors(), requestId), requestId);
    }

    // validate success extract request object
    RequestSubmitDocument requestSubmitDocument = restForm.value().get();

    // get session account and application
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // test if we have a valid document
    Document existingDocument =
        documentService.getDocument(
            requestId, sessionAccount, requestSubmitDocument.getDocumentId());

    // test document existence
    if (existingDocument == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_SUBMITDOCUMENT_DOCID_INEXISTENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITDOCUMENT_DOCID_INEXISTENT, requestId);
    }

    // test if document has a valid status
    if (!existingDocument.getStatus().equals(DocumentStatus.CREATED)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_SUBMITDOCUMENT_INVALID_STATUS);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITDOCUMENT_INVALID_STATUS, requestId);
    }

    // test if document is registered to provider
    if ((existingDocument.getProviderId() == null) || existingDocument.getProviderId().equals("")) {
      logService.error(
          requestId,
          "L",
          "errors",
          ErrorMessage.ERROR_SUBMITDOCUMENT_DOCUMENT_NOT_REGISTERED2PROVIDER);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_SUBMITDOCUMENT_DOCUMENT_NOT_REGISTERED2PROVIDER, requestId);
    }

    Application sessionApplication = request.attrs().get(Attrs.APPLICATION_NAME);

    try {
      // process user save
      businessService.submitDocument(
          requestId, sessionAccount, sessionApplication, existingDocument);

      // create response
      ResponseDocument responseDocument = new ResponseDocument();
      responseDocument.setDocument(existingDocument);

      // return response
      return coreService.getResponse(responseDocument, requestId);
    } catch (GenericRestException gre) {
      return coreService.getErrorResponse(gre, requestId);
    }
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  public Result get(Request request, String documentId) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /documents/" + documentId);

    // check document id
    if ((documentId == null) || documentId.equals("")) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENT_INVALID_DOCID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETDOCUMENT_INVALID_DOCID, requestId);
    }

    // check id is valid
    if (!documentId.matches(ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID)) {
      logService.error(requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENT_INVALID_DOCID);

      return coreService.getErrorResponse(ErrorMessage.ERROR_GETDOCUMENT_INVALID_DOCID, requestId);
    }

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get document
    Document document = documentService.getDocument(requestId, sessionAccount, documentId);

    // test record
    if (document == null) {
      logService.error(
          requestId, "L", "errors", ErrorMessage.ERROR_GETDOCUMENT_INEXISTENT_DOCUMENT);

      return coreService.getErrorResponse(
          ErrorMessage.ERROR_GETDOCUMENT_INEXISTENT_DOCUMENT, requestId);
    }

    // create response
    ResponseDocument responseDocument = new ResponseDocument();
    responseDocument.setDocument(document);

    // return response
    return coreService.getResponse(responseDocument, requestId);
  }

  @CustomStart
  @CustomValidJson
  @CustomValidRequest
  @CustomAuth
  @SuppressWarnings("unchecked")
  public Result getAllDocuments(Request request, int page, int pageSize) {

    // get request id - will be used in responses
    String requestId = coreService.getRequestId(request);
    logService.debug(requestId, "IN", "start", "GET /documents");

    // get session account
    Account sessionAccount = request.attrs().get(Attrs.ACCOUNT_NAME);

    // get documents from database
    PaginatedList paginatedList =
        documentService.getDocuments(requestId, sessionAccount, page, pageSize);

    // create response list
    ResponseDocuments responseDocuments = new ResponseDocuments();
    responseDocuments.setDocuments((List<Document>) paginatedList.getList());

    // add pagination headers
    Result result = coreService.getResponse(responseDocuments, requestId);
    result = result.withHeader("page", String.valueOf(paginatedList.getPage()));
    result = result.withHeader("pageSize", String.valueOf(paginatedList.getPageSize()));
    result = result.withHeader("totalPages", String.valueOf(paginatedList.getTotalPages()));
    result = result.withHeader("totalRecords", String.valueOf(paginatedList.getTotalRecords()));

    // return response
    return result;
  }
}
