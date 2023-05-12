package ro.iss.lolopay.classes.provider;

import java.util.List;

public class ProviderRequestCardIssue extends ProviderRequest {
  /** V/P from Virtual/Physical */
  private String cardType;

  private String firstName;

  private String lastName;

  private String embossName;

  private String address1;

  private String address2;

  private String address3;

  private String address4;

  private String city;

  private String countyName;

  private String zipCode;

  private String countryCode;

  private String phone;

  private String phone2;

  private String email;

  private String dob;

  private List<ProviderUDF> udfData;

  /** @return the phone2 */
  public String getPhone2() {

    return phone2;
  }

  /** @param phone2 the phone2 to set */
  public void setPhone2(String phone2) {

    this.phone2 = phone2;
  }

  /** @return the cardType */
  public String getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(String cardType) {

    this.cardType = cardType;
  }

  /** @return the firstName */
  public String getFirstName() {

    return firstName;
  }

  /** @param firstName the firstName to set */
  public void setFirstName(String firstName) {

    this.firstName = firstName;
  }

  /** @return the lastName */
  public String getLastName() {

    return lastName;
  }

  /** @param lastName the lastName to set */
  public void setLastName(String lastName) {

    if (firstName.length() >= 25) {
      firstName = firstName.substring(0, 25);
      this.lastName = "";
      return;
    }

    if ((firstName.length() + lastName.length()) > 25) {
      this.lastName =
          lastName.substring(
              0, (lastName.length() - (firstName.length() + lastName.length() - 25)));
      return;
    }
    this.lastName = lastName;
  }

  /** @return the embossName */
  public String getEmbossName() {

    return embossName;
  }

  /** @param embossName the embossName to set */
  public void setEmbossName(String embossName) {

    this.embossName = embossName;
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

  /** @return the address3 */
  public String getAddress3() {

    return address3;
  }

  /** @param address3 the address3 to set */
  public void setAddress3(String address3) {

    this.address3 = address3;
  }

  /** @return the address4 */
  public String getAddress4() {

    return address4;
  }

  /** @param address4 the address4 to set */
  public void setAddress4(String address4) {

    this.address4 = address4;
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

  /** @return the phone */
  public String getPhone() {

    return phone;
  }

  /** @param phone the phone to set */
  public void setPhone(String phone) {

    this.phone = phone;
  }

  /** @return the email */
  public String getEmail() {

    return email;
  }

  /** @param email the email to set */
  public void setEmail(String email) {

    this.email = email;
  }

  /** @return the dob */
  public String getDob() {

    return dob;
  }

  /** @param dob the dob to set */
  public void setDob(String dob) {

    this.dob = dob;
  }

  /** @return the udfData */
  public List<ProviderUDF> getUdfData() {

    return udfData;
  }

  /** @param udfData the udfData to set */
  public void setUdfData(List<ProviderUDF> udfData) {

    this.udfData = udfData;
  }
}
