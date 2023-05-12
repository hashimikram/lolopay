package ro.iss.lolopay.services.implementation;

import java.util.List;
import javax.inject.Inject;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import ro.iss.lolopay.classes.SearchResultItem;
import ro.iss.lolopay.classes.SearchResults;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.SearchService;

public class SearchImplementation implements SearchService {
  private final DatabaseService databaseService;

  /** Log singleton creation moment */
  @Inject
  public SearchImplementation(DatabaseService databaseService) {

    this.databaseService = databaseService;
    Logger.of(this.getClass()).debug("Singleton created");
  }

  /**
   * Remove all non alphabetic chars from string and non numerics as well (between code 48 and 57 -
   * meaning from 0 to 9)
   */
  @Override
  public String cleanUpKeyword(String keyword) {

    // Create a string builder
    StringBuilder builder = new StringBuilder();

    // iterate each letter of provided string
    for (char ch : keyword.toCharArray()) {
      // test if letter from any alphabet or digit
      if (Character.isAlphabetic(ch) || (Integer.valueOf(ch) >= 48 && Integer.valueOf(ch) <= 57)) {
        // promote as a valid character
        builder.append(ch);
      }
    }
    return builder.toString();
  }

  /** Search method for live */
  @Override
  public List<SearchResultItem> search(
      Account account, String searchString, Class<? extends TableCollection> collectionType) {

    Logger.of(this.getClass()).debug("search:accountId: " + account.getId().toString());
    Logger.of(this.getClass()).debug("search:searchString: " + searchString);
    Logger.of(this.getClass()).debug("search:modelType: " + collectionType);

    SearchResults searchResults =
        applySearchAlgo(account.getId().toString(), searchString, collectionType, false);
    return searchResults.getResults();
  }

  /** Search method for development - to verify the algorithm */
  @Override
  public List<SearchResultItem> searchDebug(
      Account account, String searchString, Class<? extends TableCollection> collectionType) {

    Logger.of(this.getClass()).debug("searchDebug:accountId: " + account.getId().toString());
    Logger.of(this.getClass()).debug("searchDebug:searchString: " + searchString);
    Logger.of(this.getClass()).debug("searchDebug:modelType: " + collectionType);
    SearchResults searchResults =
        applySearchAlgo(account.getId().toString(), searchString, collectionType, true);
    return searchResults.getAllSearchResults();
  }

  /**
   * Perform query in indexed keyword field
   *
   * @param accountId
   * @param searchString
   * @param collectionType
   * @param limit
   * @param weight
   * @param match
   * @return
   */
  private List<? extends TableCollection> dbIndexedSearch(
      String accountId,
      String searchString,
      Class<? extends TableCollection> collectionType,
      int limit,
      String weight,
      String match) {

    Logger.of(this.getClass()).debug("dbIndexedSearch:accountId: " + accountId);
    Logger.of(this.getClass()).debug("dbIndexedSearch:searchString: " + searchString);
    Logger.of(this.getClass()).debug("dbIndexedSearch:collectionType: " + collectionType);
    Logger.of(this.getClass()).debug("dbIndexedSearch:limit: " + limit);
    Logger.of(this.getClass()).debug("dbIndexedSearch:weight: " + weight);
    Logger.of(this.getClass()).debug("dbIndexedSearch:match: " + match);

    // define query record limitations - find max 100
    FindOptions findOptions = new FindOptions();
    findOptions.skip(0);
    findOptions.limit(limit);

    // build query object
    Query<? extends TableCollection> query =
        databaseService.getConnection(accountId).createQuery(collectionType);

    // add query criteria - we go equal for full match and startsWith for
    // partial match
    if (match.equals(ApplicationConstants.SEARCH_MATCH_FULL)) {
      query
          .field("keywords")
          .equal(weight.concat(ApplicationConstants.SEARCH_MATCH_FULL).concat(searchString));
    } else if (match.equals(ApplicationConstants.SEARCH_MATCH_PARTIAL)) {
      query
          .field("keywords")
          .startsWith(
              weight.concat(ApplicationConstants.SEARCH_MATCH_PARTIAL).concat(searchString));
    }
    Logger.of(this.getClass()).debug("query: " + query);

    // execute and return the actual search
    return query.asList(findOptions);
  }

  /**
   * This is the search algorithm
   *
   * @param accountId
   * @param searchString
   * @param collectionType
   * @param runInDevMode
   * @return
   */
  private SearchResults applySearchAlgo(
      String accountId,
      String searchString,
      Class<? extends TableCollection> collectionType,
      boolean runInDevMode) {

    SearchResults searchResults = new SearchResults();

    // check search string
    if ((null != searchString) && (!searchString.equals(""))) {
      // split received string by white space regular expression
      String[] searchKeywordsArray =
          searchString.split(ApplicationConstants.REGEX_SEARCH_SEPARATOR);

      // for each keyword in provided search string - TODO split searches
      // in several threads per keyword
      for (String keyword : searchKeywordsArray) {
        // clean up keyword - remove all non digit or non alphabetical
        // and upper case it
        keyword = cleanUpKeyword(keyword).toUpperCase();

        int currentGatheredResults = 0;
        int maxAllowedResult = ConfigFactory.load().getInt("application.maxSearchSize");

        // execute search operation
        List<? extends TableCollection> searchResultHighFull =
            dbIndexedSearch(
                accountId,
                keyword,
                collectionType,
                maxAllowedResult,
                ApplicationConstants.SEARCH_WEIGHT_HIGH,
                ApplicationConstants.SEARCH_MATCH_FULL);
        currentGatheredResults += searchResultHighFull.size();

        // log results
        Logger.of(this.getClass()).debug("searchResultHighFull: " + searchResultHighFull.size());
        Logger.of(this.getClass()).debug("currentGatheredResults: " + currentGatheredResults);
        if (runInDevMode) printList(searchResultHighFull);

        // add results to the big list with score of 4
        searchResults.addRecordList(searchResultHighFull, 4);

        // check if we have more results to search for this keyword - we
        // do not have yet 100 results
        if (currentGatheredResults < maxAllowedResult) {
          // build search query for keyword LOW + FULL = 3 points
          List<? extends TableCollection> searchResultLowFull =
              dbIndexedSearch(
                  accountId,
                  keyword,
                  collectionType,
                  (maxAllowedResult - currentGatheredResults),
                  ApplicationConstants.SEARCH_WEIGHT_LOW,
                  ApplicationConstants.SEARCH_MATCH_FULL);
          currentGatheredResults += searchResultLowFull.size();

          // log results
          Logger.of(this.getClass()).debug("searchResultLowFull: " + searchResultLowFull.size());
          Logger.of(this.getClass()).debug("currentGatheredResults: " + currentGatheredResults);
          if (runInDevMode) printList(searchResultLowFull);

          // add results to the big list with score of 3
          searchResults.addRecordList(searchResultLowFull, 3);

          // check if we have more results to search for this keyword
          // we do not have yet 100 results
          if (currentGatheredResults < maxAllowedResult) {
            // build search query for keyword LOW + FULL = 2 points
            // of search score
            // execute search operation
            List<? extends TableCollection> searchResultHighPartial =
                dbIndexedSearch(
                    accountId,
                    keyword,
                    collectionType,
                    (maxAllowedResult - currentGatheredResults),
                    ApplicationConstants.SEARCH_WEIGHT_HIGH,
                    ApplicationConstants.SEARCH_MATCH_PARTIAL);
            currentGatheredResults += searchResultHighPartial.size();

            // log results
            Logger.of(this.getClass())
                .debug("searchResultHighPartial: " + searchResultHighPartial.size());
            Logger.of(this.getClass()).debug("currentGatheredResults: " + currentGatheredResults);
            if (runInDevMode) printList(searchResultHighPartial);

            // add results to the big list with score of 2
            searchResults.addRecordList(searchResultHighPartial, 2);

            // check if we have more results to search for this
            // keyword - we do not have yet 100 results
            if (currentGatheredResults < maxAllowedResult) {
              // build search query for keyword LOW + PARTIAL = 1
              // point of search score
              List<? extends TableCollection> searchResultLowPartial =
                  dbIndexedSearch(
                      accountId,
                      keyword,
                      collectionType,
                      (maxAllowedResult - currentGatheredResults),
                      ApplicationConstants.SEARCH_WEIGHT_LOW,
                      ApplicationConstants.SEARCH_MATCH_PARTIAL);
              currentGatheredResults += searchResultLowPartial.size();

              Logger.of(this.getClass())
                  .debug("searchResultHighPartial: " + searchResultLowPartial.size());
              Logger.of(this.getClass()).debug("currentGatheredResults: " + currentGatheredResults);
              if (runInDevMode) printList(searchResultLowPartial);

              // add results to the big list with score of 1
              searchResults.addRecordList(searchResultLowPartial, 1);
            }
          }
        }
      }
    }

    return searchResults;
  }

  /**
   * Method for debugging , mostly analysing the search algorithm
   *
   * @param resultsToPrint
   */
  private void printList(List<? extends TableCollection> resultsToPrint) {

    for (TableCollection result : resultsToPrint) {
      if (result instanceof Application) {
        Logger.of(this.getClass())
            .debug(
                ":printList: "
                    + ((Application) result).getApplicationName()
                    + " "
                    + (((Application) result).getApplicationEmail()));
      }
    }
  }
}
