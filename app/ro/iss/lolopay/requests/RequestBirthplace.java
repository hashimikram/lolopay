package ro.iss.lolopay.requests;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.validators.IsCountryISO;

public class RequestBirthplace extends RestRequest {
  /** City name */
  @Required(message = ErrorMessage.ERROR_BIRTHPLACE_CITY_REQUIRED)
  @MaxLength(value = 255, message = ErrorMessage.ERROR_BIRTHPLACE_CITY_MAXLENGTH)
  private String city;

  /** Country code */
  @Required(message = ErrorMessage.ERROR_BIRTHPLACE_COUNTRY_REQUIRED)
  @ValidateWith(value = IsCountryISO.class, message = ErrorMessage.ERROR_BIRTHPLACE_COUNTRY_INVALID)
  private String country;

  /** @return the city */
  public String getCity() {

    return city;
  }

  /** @param city the city to set */
  public void setCity(String city) {

    this.city = city;
  }

  /** @return the country */
  public String getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(String country) {

    this.country = country;
  }
}
