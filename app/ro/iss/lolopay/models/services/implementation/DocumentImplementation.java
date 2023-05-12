package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.Logger;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.enums.QueryFieldOperator;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.DocumentService;
import ro.iss.lolopay.services.definition.DatabaseService;

@Singleton
public class DocumentImplementation implements DocumentService {

  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public DocumentImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  @Override
  public void deleteUserDocuments(String requestId, Account account, String userId) {
    Datastore datastore = databaseService.getConnection(account.getId());

    Query<Document> query = datastore.createQuery(Document.class);
    query.filter("userId", userId);
    datastore.delete(query);
  }

  @Override
  public Document getDocument(String requestId, Account account, String documentId) {

    return (Document) databaseService.getRecord(requestId, account, documentId, Document.class);
  }

  @Override
  public void saveDocument(String requestId, Account account, Document document) {

    // save record in database
    databaseService.getConnection(account.getId().toString()).save(document);
  }

  @Override
  public PaginatedList getDocumentsPerUser(
      String requestId, Account account, String userId, int page, int pageSize) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("userId", userId);

    // return result
    return databaseService.getRecords(
        requestId, account, filters, QueryFieldOperator.AND, page, pageSize, Document.class);
  }

  @Override
  public PaginatedList getDocuments(String requestId, Account account, int page, int pageSize) {

    // return result
    return databaseService.getRecords(
        requestId, account, null, null, page, pageSize, Document.class);
  }

  @Override
  public Document getDocumentByProviderId(String requestId, Account account, String providerId) {

    // create filters
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put("providerId", providerId);

    // return result
    return (Document) databaseService.getRecord(requestId, account, filters, Document.class);
  }
}
