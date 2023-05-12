package ro.iss.lolopay.models.classes;

public class Address {

  /** Address details line 1 */
  private String addressLine1;

  /** Address details line 1 */
  private String addressLine2;

  /** City name */
  private String city;

  /**
   * County/Region name The region of the address - this is optional except if the Country is US, CA
   * or MX
   */
  private String county;

  /** Country code */
  private CountryISO country;

  /** Postal code */
  private String postalCode;

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
  public CountryISO getCountry() {

    return country;
  }

  /** @param country the country to set */
  public void setCountry(CountryISO country) {

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
