package ro.iss.lolopay.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.models.classes.TableCollection;

public class SearchResults {
  /** Private list to store all results for each keyword */
  private List<SearchResultItem> searchResults = new ArrayList<SearchResultItem>();

  /**
   * Add a list of records to the big results list
   *
   * @param searchResult
   */
  public void addRecordList(List<? extends TableCollection> searchResult, int searchScore) {

    if (null != searchResult) {
      for (TableCollection record : searchResult) {
        this.addRecord(record, searchScore);
      }
    }
  }

  /**
   * Add one record to the big results list
   *
   * @param record
   */
  public void addRecord(TableCollection record, int searchScore) {

    boolean recordUpdated = false;

    // search to check if same result was retrieved earlier
    for (SearchResultItem searchResultItem : searchResults) {
      // if the same record was retrieved earlier
      if (searchResultItem.getRecord().equals(record)) {
        // increase record score
        searchResultItem.incSearchScore(searchScore);
        recordUpdated = true;
        break;
      }
    }

    // if record was not foudn yet, we create it with it's own score
    if (!recordUpdated) {
      SearchResultItem newSearchResultItem = new SearchResultItem();
      newSearchResultItem.setRecord(record);
      newSearchResultItem.setSearchScore(searchScore);
      searchResults.add(newSearchResultItem);
    }
  }

  /**
   * Get results for production environment
   *
   * @return
   */
  public List<SearchResultItem> getResults() {

    sortResultsByScore();

    if (this.searchResults.size() < ConfigFactory.load().getInt("application.maxSearchSize")) {
      return this.searchResults;
    } else {
      // extract limited results results
      return this.searchResults.subList(
          0, ConfigFactory.load().getInt("application.maxSearchSize"));
    }
  }

  /**
   * Get results for debug purposes
   *
   * @return
   */
  public List<SearchResultItem> getAllSearchResults() {

    sortResultsByScore();

    return this.searchResults;
  }

  /** Perform sorting by score with obtained results */
  private void sortResultsByScore() {

    // sort search results
    if (searchResults.size() > 0) {
      Collections.sort(
          searchResults,
          new Comparator<SearchResultItem>() {
            @Override
            public int compare(final SearchResultItem object1, final SearchResultItem object2) {

              return Integer.compare(object2.getSearchScore(), object1.getSearchScore());
            }
          });
    }
  }
}
