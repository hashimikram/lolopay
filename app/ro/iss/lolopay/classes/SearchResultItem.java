package ro.iss.lolopay.classes;

import ro.iss.lolopay.models.classes.TableCollection;

public class SearchResultItem {
  private TableCollection record;

  private int searchScore;

  /** @return the record */
  public TableCollection getRecord() {

    return record;
  }

  /** @param record the record to set */
  public void setRecord(TableCollection record) {

    this.record = record;
  }

  /** @return the searchScore */
  public int getSearchScore() {

    return searchScore;
  }

  /** @param searchScore the searchScore to set */
  public void setSearchScore(int searchScore) {

    this.searchScore = searchScore;
  }

  public void incSearchScore(int pointsToAdd) {

    this.searchScore += pointsToAdd;
  }
}
