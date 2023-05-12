package ro.iss.lolopay.models.classes;

public class Birthplace {
  private String city;

  private CountryISO country;

  /** @return the city */
  public String getCity() {

    return city;
  }

  /** @param city the city to set */
  public void setCity(String city) {

    this.city = city;
  }

  /** @return the country */
  public CountryISO getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(CountryISO country) {

    this.country = country;
  }
}
