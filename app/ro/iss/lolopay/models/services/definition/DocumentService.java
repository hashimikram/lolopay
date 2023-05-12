package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.models.database.Document;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.DocumentImplementation;

@ImplementedBy(DocumentImplementation.class)
public interface DocumentService {
  public Document getDocument(String requestId, Account account, String documentId);

  public Document getDocumentByProviderId(String requestId, Account account, String providerId);

  public void saveDocument(String requestId, Account account, Document document);

  public PaginatedList getDocumentsPerUser(
      String requestId, Account account, String userId, int page, int pageSize);

  public PaginatedList getDocuments(String requestId, Account account, int page, int pageSize);

  public void deleteUserDocuments(String requestId, Account account, String userId);
}
