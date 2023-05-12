package ro.iss.lolopay.services.definition;

import java.util.List;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.SearchResultItem;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.implementation.SearchImplementation;

@ImplementedBy(SearchImplementation.class)
public interface SearchService {
  public String cleanUpKeyword(String keyword);

  public List<SearchResultItem> search(
      Account account, String searchString, Class<? extends TableCollection> collectionType);

  public List<SearchResultItem> searchDebug(
      Account account, String searchString, Class<? extends TableCollection> collectionType);
}
