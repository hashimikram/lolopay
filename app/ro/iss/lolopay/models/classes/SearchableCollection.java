package ro.iss.lolopay.models.classes;

import java.util.LinkedHashSet;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.PrePersist;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ro.iss.lolopay.enums.ApplicationConstants;

public abstract class SearchableCollection extends TableCollection {
  @JsonIgnore @Indexed private LinkedHashSet<String> keywords;

  /** @return the keywords */
  public LinkedHashSet<String> getKeywords() {

    return keywords;
  }

  /** @param keywords the keywords to set */
  public void setKeywords(LinkedHashSet<String> keywords) {

    this.keywords = keywords;
  }

  /** Before collection save hook */
  @PrePersist
  @Override
  public void beforeSave() {

    // refresh keywords on each save, create a new keywords list
    this.keywords = new LinkedHashSet<String>();

    // generate keywords as well - programmer will choose what data will
    // go to keywords engine
    generateKeyWords();

    // call super before save
    super.beforeSave();
  }

  /**
   * This is to be implemented in each model. The programmer will specify how keywords per model
   * will be built
   */
  protected abstract void generateKeyWords();

  /**
   * Create search able keywords from a provided string and attach them to object to be saved in the
   * database
   *
   * @param valueOfStringField
   */
  protected void createKeywordsFromFieldValue(String valueOfStringField, String weight) {

    // add value to keywords
    if ((null != valueOfStringField) && (!valueOfStringField.equals(""))) {
      // split received string by white space regular expression
      String[] keywordArray = valueOfStringField.split(ApplicationConstants.REGEX_SEARCH_SEPARATOR);

      // for each keyword in provided string
      for (String keyword : keywordArray) {
        // clean up keyword - remove all non digit or non alphabetical

        // Create a string builder first
        StringBuilder builder = new StringBuilder();

        // iterate each letter of keyword
        for (char ch : keyword.toCharArray()) {
          // test if letter from any alphabet or digit
          if (Character.isAlphabetic(ch)
              || (Integer.valueOf(ch) >= 48 && Integer.valueOf(ch) <= 57)) {
            // promote as a valid character
            builder.append(ch);
          }
        }

        // retrieve cleaned up version of this keyword
        keyword = builder.toString();

        // add full match word
        this.keywords.add(
            weight.concat(ApplicationConstants.SEARCH_MATCH_FULL).concat(keyword.toUpperCase()));

        // split each keyword
        for (int i = 0; i < keyword.length(); i++) {
          // add partial key to list
          this.keywords.add(
              weight
                  .concat(ApplicationConstants.SEARCH_MATCH_PARTIAL)
                  .concat(keyword.substring(i, keyword.length()).toUpperCase()));
        }
      }
    }
  }
}
