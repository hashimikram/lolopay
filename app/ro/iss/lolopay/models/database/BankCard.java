package ro.iss.lolopay.models.database;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ro.iss.lolopay.models.classes.BankCardStatus;
import ro.iss.lolopay.models.classes.BankCardType;
import ro.iss.lolopay.models.classes.CardUserInfo;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.classes.TransactionDate;

@Entity(value = "bankCards", noClassnameStored = true)
public class BankCard extends TableCollection {
  /** Developer custom tag */
  private String customTag;

  /** A user's ID */
  @Indexed private String userId;

  /** First name on card */
  private String firstName;

  /** Last name on card */
  private String lastName;

  /** Emboss Name on card */
  private String embossName;

  /** Address line 1 for card */
  private String address1;

  /** Address line 2 for card */
  private String address2;

  /** Address line 3 for card */
  private String address3;

  /** Address line 4 for card */
  private String address4;

  /** Card city */
  private String city;

  /** Card county */
  private String countyName;

  /** Card zip */
  private String zipCode;

  /** Card country */
  private String countryCode;

  /** Card phone */
  private String phone;

  /** Card email */
  private String email;

  /** Card owner dob */
  private String dob;

  /** Card type */
  private BankCardType type;

  /** Card number */
  private String sensitiveCardNumber;

  /** Card status */
  private BankCardStatus status;

  /** Card bank BIC */
  private String providerBic;

  /** Card bank IBAN */
  private String providerIban;

  /** Card available currencies */
  private List<String> currencies;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId;

  private Map<CurrencyISO, TransactionDate> transactionDates;

  private CardUserInfo cardUserInfo;

  private long expirationDate;

  /** @return Returns true if this is PHYSICAL card */
  @JsonIgnore
  public boolean isPhysicalCard() {

    return BankCardType.PHYSICAL == this.type;
  }

  /** @return Returns true if delivery address is valid */
  @JsonIgnore
  public boolean deliveryAddressIsValid() {

    return StringUtils.isNotBlank(getAddress1())
        && StringUtils.isNotBlank(getCity())
        && StringUtils.isNotBlank(getZipCode())
        && StringUtils.isNotBlank(getCountryCode());
  }

  /** @return the transactionDates */
  public Map<CurrencyISO, TransactionDate> getTransactionDates() {

    return transactionDates;
  }

  /** @param transactionDates the transactionDates to set */
  public void setTransactionDates(Map<CurrencyISO, TransactionDate> transactionDates) {

    this.transactionDates = transactionDates;
  }

  /** @param transactionDates the transactionDates to set */
  // public void updateTransactionsDates(BankCardService bankCardService, BankCardWallet
  // bankCardWallet, List<TransactionDate> providerTransactionDates, long startDate, long endDate) {
  //
  // TransactionDate transactionDate = new TransactionDate();
  // transactionDate.setCurrency(bankCardWallet.getCurrency());
  //
  // // if we don't have any transactions received from the provider it means that all transactions
  // were served from DB and update of transactionDates is unnecessary
  // if (providerTransactionDates.isEmpty()) {
  // return;
  // }
  //
  // // if transactionDates is null, or not existing
  // if (this.transactionDates == null ||
  // !this.transactionDates.containsKey(bankCardWallet.getCurrency())) {
  // // set the startDate as projectStartDate
  // transactionDate.setStartDate(ConfigFactory.load().getLong("application.projectStartDate"));
  //
  // // set the endDate
  // // if the requested endDate is today set endDate to sevenDaysAgo
  // // if endDate is less thenSevenDaysAgo setEndDate as requested endDate
  // }
  //
  // boolean currencyFound = false;
  // for (int i = 0; i < this.transactionDates.size(); i++) {
  //
  // if (this.transactionDates.get(i).getCurrency().equals(currency)) {
  // currencyFound = true;
  // if (this.transactionDates.get(i).getEndDate() < endDate) {
  // LocalDate localDateEndDate =
  // Instant.ofEpochSecond(endDate).atZone(ZoneId.of("GMT")).toLocalDate();
  // LocalDate localDateNow =
  // Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("GMT")).toLocalDate();
  //
  // // if endDate is today, store endDate as yesterday
  // if (localDateEndDate.equals(localDateNow)) {
  //
  // long yesterday = localDateNow.minusDays(1L).atStartOfDay(ZoneId.of("GMT")).toEpochSecond();
  // this.transactionDates.get(i).setEndDate(yesterday);
  // }
  // else {
  //
  // // else store endDate as endDate
  // this.transactionDates.get(i).setEndDate(endDate);
  // }
  // }
  // }
  // }
  //
  // if (!currencyFound) {
  // this.transactionDates.add(new TransactionDate(currency,
  // ConfigFactory.load().getLong("application.projectStartDate"), endDate));
  // }
  //
  // return;
  // }

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the userId */
  public String getUserId() {

    return userId;
  }

  /** @param userId the userId to set */
  public void setUserId(String userId) {

    this.userId = userId;
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

  /** @return the type */
  public BankCardType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(BankCardType type) {

    this.type = type;
  }

  /** @return the sensitiveCardNumber */
  public String getSensitiveCardNumber() {

    return sensitiveCardNumber;
  }

  /** @param sensitiveCardNumber the sensitiveCardNumber to set */
  public void setSensitiveCardNumber(String sensitiveCardNumber) {

    this.sensitiveCardNumber = sensitiveCardNumber;
  }

  /** @return the status */
  public BankCardStatus getStatus() {

    return status;
  }

  /** @param status the status to set */
  public void setStatus(BankCardStatus status) {

    this.status = status;
  }

  /** @return the providerBic */
  public String getProviderBic() {

    return providerBic;
  }

  /** @param providerBic the providerBic to set */
  public void setProviderBic(String providerBic) {

    this.providerBic = providerBic;
  }

  /** @return the providerIban */
  public String getProviderIban() {

    return providerIban;
  }

  /** @param providerIban the providerIban to set */
  public void setProviderIban(String providerIban) {

    this.providerIban = providerIban;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return the currencies */
  public List<String> getCurrencies() {

    return currencies;
  }

  /** @param currencies the currencies to set */
  public void setCurrencies(List<String> currencies) {

    this.currencies = currencies;
  }

  /** @return the cardUserInfo */
  public CardUserInfo getCardUserInfo() {

    return cardUserInfo;
  }

  /** @param cardUserInfo the cardUserInfo to set */
  public void setCardUserInfo(CardUserInfo cardUserInfo) {

    this.cardUserInfo = cardUserInfo;
  }

  /** @return the expirationDate */
  public long getExpirationDate() {
    return expirationDate;
  }

  /** @param expirationDate the expirationDate to set */
  public void setExpirationDate(long expirationDate) {
    this.expirationDate = expirationDate;
  }
}
