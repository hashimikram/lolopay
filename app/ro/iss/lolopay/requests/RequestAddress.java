package ro.iss.lolopay.requests;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCountryISO;

@Validate
public class RequestAddress extends RestRequest implements Validatable<String> {
  private static final List<String> notRequiredPostaCodeCountries =
      Arrays.asList(
          "AO", "AG", "AW", "BS", "BZ", "BJ", "BW", "BF", "BI", "CM", "CF", "KM", "CG", "CD", "CK",
          "CI", "DJ", "DM", "GQ", "ER", "FJ", "TF", "GM", "GH", "GD", "GN", "GY", "HK", "IE", "JM",
          "KE", "KI", "MO", "MW", "ML", "MR", "MU", "MS", "NR", "AN", "NU", "KP", "PA", "QA", "RW",
          "KN", "LC", "ST", "SA", "SC", "SL", "SB", "SO", "ZA", "SR", "SY", "TZ", "TL", "TK", "TO",
          "TT", "TV", "UG", "AE", "VU", "YE", "ZW");

  /** Address details line 1 */
  @Required(message = ErrorMessage.ERROR_ADDRESS_LINE1_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_ADDRESS_LINE1_MAXLENGTH)
  private String addressLine1;

  /** Address details line 1 */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_ADDRESS_LINE2_MAXLENGTH)
  private String addressLine2;

  /** City name */
  @Required(message = ErrorMessage.ERROR_ADDRESS_CITY_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_ADDRESS_CITY_MAXLENGTH)
  private String city;

  /**
   * County/Region name The region of the address - this is optional except if the Country is US, CA
   * or MX
   */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_ADDRESS_COUNTY_MAXLENGTH)
  private String county;

  /** Country code */
  @Required(message = ErrorMessage.ERROR_ADDRESS_COUNTRY_REQUIRED)
  @ValidateWith(value = IsCountryISO.class, message = ErrorMessage.ERROR_ADDRESS_COUNTRY_INVALID)
  private String country;

  /** Postal code */
  @MaxLength(value = 255, message = ErrorMessage.ERROR_ADDRESS_POSTALCODE_MAXLENGTH)
  private String postalCode;

  @Override
  public String validate() {

    // if country is CA, US or MX
    if (this.country != null
        && (this.country.equals("US") || this.country.equals("CA") || this.country.equals("MX"))) {
      // county becomes mandatory, it should be not null and not blank
      if (StringUtils.isBlank(this.county)) {
        return ErrorMessage.ERROR_ADDRESS_COUNTY_REQUIRED;
      }
    }

    if (!notRequiredPostaCodeCountries.contains(this.country)) {
      // postalCode becomes mandatory, it should be not null and not blank
      if (StringUtils.isBlank(this.postalCode)) {
        return ErrorMessage.ERROR_ADDRESS_POSTALCODE_REQUIRED;
      }
    }

    return null;
  }

  /** @return the addressLine1 */
  public String getAddressLine1() {

    return addressLine1;
  }

  /** @param addressLine1 the addressLine1 to set */
  public void setAddressLine1(String addressLine1) {

    this.addressLine1 = addressLine1;
  }

  /** @return the addressLine2 */
  public String getAddressLine2() {

    return addressLine2;
  }

  /** @param addressLine2 the addressLine2 to set */
  public void setAddressLine2(String addressLine2) {

    this.addressLine2 = addressLine2;
  }

  /** @return the city */
  public String getCity() {

    return city;
  }

  /** @param city the city to set */
  public void setCity(String city) {

    this.city = city;
  }

  /** @return the county */
  public String getCounty() {

    return county;
  }

  /** @param county the county to set */
  public void setCounty(String county) {

    this.county = county;
  }

  /** @return the country */
  public String getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(String country) {

    this.country = country;
  }

  /** @return the postalCode */
  public String getPostalCode() {

    return postalCode;
  }

  /** @param postalCode the postalCode to set */
  public void setPostalCode(String postalCode) {

    this.postalCode = postalCode;
  }
}
