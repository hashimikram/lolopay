package ro.iss.lolopay.requests;

import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.ValidateWith;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.enums.ErrorMessage;
import ro.iss.lolopay.models.classes.BankCardType;
import ro.iss.lolopay.validators.IsBankCardType;
import ro.iss.lolopay.validators.IsCountryISO;

@Validate
public class RequestCreateBankCard extends RestRequest implements Validatable<String> {
  @Required(message = ErrorMessage.ERROR_CREATEBANKCARD_USERID_REQUIRED)
  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_MONGO_AND_INT_ID,
      message = ErrorMessage.ERROR_CREATEBANKCARD_USERID_INVALID)
  private String userId;

  @Required(message = ErrorMessage.ERROR_CREATEBANKCARD_TYPE_REQUIRED)
  @ValidateWith(
      value = IsBankCardType.class,
      message = ErrorMessage.ERROR_CREATEBANKCARD_TYPE_INVALID)
  private String type;

  @MaxLength(value = 255, message = ErrorMessage.ERROR_CREATEBANKCARD_TAG_MAXLENGTH)
  private String customTag;

  @Pattern(
      value = ApplicationConstants.REGEX_VALIDATE_EMBOSSNAME,
      message = ErrorMessage.ERROR_CREATEBANKCARD_EMBOSSNAME_INVALID)
  private String embossName = new String();

  @Required(message = ErrorMessage.ERROR_CREATEBANKCARD_FIRSTNAME_REQUIRED)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_CREATEBANKCARD_FIRSTNAME_MAXLENGTH)
  private String firstName;

  @Required(message = ErrorMessage.ERROR_CREATEBANKCARD_LASTNAME_REQUIRED)
  @MaxLength(value = 20, message = ErrorMessage.ERROR_CREATEBANKCARD_LASTNAME_MAXLENGTH)
  private String lastName;

  @MaxLength(value = 130, message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_LINE1_MAXLENGTH)
  private String address1;

  @MaxLength(value = 130, message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_LINE2_MAXLENGTH)
  private String address2;

  @MaxLength(value = 25, message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_CITY_MAXLENGTH)
  private String city;

  @MaxLength(value = 20, message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_COUNTY_MAXLENGTH)
  private String countyName;

  @MaxLength(value = 15, message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_POSTALCODE_MAXLENGTH)
  private String zipCode;

  @ValidateWith(
      value = IsCountryISO.class,
      message = ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_COUNTRY_INVALID)
  private String countryCode;

  @Valid
  // Uncomment the line below if you want to make this info mandatory
  // @NotNull(message = ErrorMessage.ERROR_CARDUSERINFO_REQUIRED)
  private RequestCardUserInfo cardUserInfo;

  @JsonIgnore
  public boolean isPhysicalCard() {

    return BankCardType.PHYSICAL.toString().equalsIgnoreCase(type);
  }

  @Override
  public String validate() {

    if (isPhysicalCard()) {
      if (StringUtils.isBlank(address1)) {
        return ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_LINE1_REQUIRED;
      }

      if (StringUtils.isNotBlank(address2)) {
        if (address1.length() + address2.length() >= 130) {
          return ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_LINE2_MAXLENGTH;
        }
      }

      if (StringUtils.isBlank(city)) {
        return ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_CITY_REQUIRED;
      }

      if (StringUtils.isBlank(countryCode)) {
        return ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_COUNTRY_REQUIRED;
      }

      if (StringUtils.isBlank(zipCode)) {
        return ErrorMessage.ERROR_CREATEBANKCARD_ADDRESS_POSTALCODE_REQUIRED;
      }
    }
    return null;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
  }

  /** @return the type */
  public String getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(String type) {

    this.type = type;
  }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the embossName */
  public String getEmbossName() {

    return embossName;
  }

  /** @param embossName the embossName to set */
  public void setEmbossName(String embossName) {

    this.embossName = embossName;
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

    this.lastName = lastName;
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

  public RequestCardUserInfo getCardUserInfo() {

    return cardUserInfo;
  }

  public void setCardUserInfo(RequestCardUserInfo cardUserInfo) {

    this.cardUserInfo = cardUserInfo;
  }
}
