package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCountryISO;

public class RequestUpgradeBankCard extends RestRequest {
  @Required(message = ErrorMessage.ERROR_UPGRADEBANKCARD_CARDID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_UPGRADEBANKCARD_CARDID_INVALID)
  private String cardId;

  @Required(message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_LINE1_REQUIRED)
  @MaxLength(value = 130, message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_LINE1_MAXLENGTH)
  private String address1;

  @MaxLength(value = 130, message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_LINE2_MAXLENGTH)
  private String address2;

  @Required(message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_CITY_REQUIRED)
  @MaxLength(value = 25, message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_CITY_MAXLENGTH)
  private String city;

  @MaxLength(value = 20, message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_COUNTY_MAXLENGTH)
  private String countyName;

  @Required(message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_POSTALCODE_REQUIRED)
  @MaxLength(value = 15, message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_POSTALCODE_MAXLENGTH)
  private String zipCode;

  @Required(message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_COUNTRY_REQUIRED)
  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_UPGRADEBANKCARD_ADDRESS_COUNTRY_INVALID)
  private String countryCode;

  /** @return the cardId */
  public String getCardId() {

    return cardId;
  }

  /** @param cardId the cardId to set */
  public void setCardId(String cardId) {

    this.cardId = cardId;
  }

  /** @return the address1 */
  public String getAddress1() {

    return address1;
  }

  /** @param address1 the address1 to set */
  public void setAddress1(String address1) {

    this.address1 = address1;
  }

  /** @return the address2 */
  public String getAddress2() {

    return address2;
  }

  /** @param address2 the address2 to set */
  public void setAddress2(String address2) {

    this.address2 = address2;
  }

  /** @return the city */
  public String getCity() {

    return city;
  }

  /** @param city the city to set */
  public void setCity(String city) {

    this.city = city;
  }

  /** @return the countyName */
  public String getCountyName() {

    return countyName;
  }

  /** @param countyName the countyName to set */
  public void setCountyName(String countyName) {

    this.countyName = countyName;
  }

  /** @return the zipCode */
  public String getZipCode() {

    return zipCode;
  }

  /** @param zipCode the zipCode to set */
  public void setZipCode(String zipCode) {

    this.zipCode = zipCode;
  }

  /** @return the countryCode */
  public String getCountryCode() {

    return countryCode;
  }

  /** @param countryCode the countryCode to set */
  public void setCountryCode(String countryCode) {

    this.countryCode = countryCode;
  }
}
