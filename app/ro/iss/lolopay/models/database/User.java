package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.CompanyType;
import ro.iss.lolopay.models.classes.CountryISO;
import ro.iss.lolopay.models.classes.IncomeRange;
import ro.iss.lolopay.models.classes.KYCLevel;
import ro.iss.lolopay.models.classes.SearchableCollection;
import ro.iss.lolopay.models.classes.UserType;

@Entity(value = "users", noClassnameStored = true)
public class User extends SearchableCollection {
  // TODO split in 2 natural and legal

  /** Developer custom tag */
  private String customTag;

  /** User type which is either person or legal entity */
  private UserType type;

  /**
   * User KYC level, STANDARD for non verified users, VERIFIED for validated users, HIGHRISK user
   */
  private KYCLevel kycLevel;

  /** User email address or company legal representative email address */
  private String email;

  /** User mobile phone number */
  private String mobilePhone;

  /** User first name or company legal representative first name */
  private String firstName;

  /** User last name or company legal representative last name */
  private String lastName;

  /** User birth date or company legal representative birth date */
  private Long birthDate;

  /** User nationality or company legal representative nationality */
  private CountryISO nationality;

  /** User country of residence or company legal representative country of residence */
  private CountryISO countryOfResidence;

  /** User address or company legal representative address */
  private Address address;

  /** User occupation - available for natural users only */
  private String occupation;

  /** User income range - available for natural users only */
  private IncomeRange incomeRange;

  /** The actual legal person type (Business, Organization or Soletrader) */
  private CompanyType companyType;

  /** The actual legal person name (Company registered name etc) */
  private String companyName;

  /** The actual legal person email (Company email etc) */
  private String companyEmail;

  /**
   * The actual legal person number provided by registration office in issuing country where it was
   * created
   */
  private String companyRegistrationNumber;

  /** The actual legal person VAT ID / Tax ID */
  private String companyVatId;

  /** The actual legal person registered address */
  private Address companyAddress;

  /** This is a flag to store if the user was refreshed with MangoPay data after migration */
  @JsonIgnore private boolean tempDiacriticsSolved;

  /** Id of the related record in financial provider system */
  @Indexed private String providerId;

  /** @return the customTag */
  public String getCustomTag() {

    return customTag;
  }

  /** @param customTag the customTag to set */
  public void setCustomTag(String customTag) {

    this.customTag = customTag;
  }

  /** @return the type */
  public UserType getType() {

    return type;
  }

  /** @param type the type to set */
  public void setType(UserType type) {

    this.type = type;
  }

  /** @return the kycLevel */
  public KYCLevel getKycLevel() {

    return kycLevel;
  }

  /** @param kycLevel the kycLevel to set */
  public void setKycLevel(KYCLevel kycLevel) {

    this.kycLevel = kycLevel;
  }

  /** @return the email */
  public String getEmail() {

    return email;
  }

  /** @param email the email to set */
  public void setEmail(String email) {

    this.email = email;
  }

  /** @return the mobilePhone */
  public String getMobilePhone() {

    return mobilePhone;
  }

  /** @param mobilePhone the mobilePhone to set */
  public void setMobilePhone(String mobilePhone) {

    this.mobilePhone = mobilePhone;
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

  /** @return the birthDate */
  public Long getBirthDate() {

    return birthDate;
  }

  /** @param birthDate the birthDate to set */
  public void setBirthDate(Long birthDate) {

    this.birthDate = birthDate;
  }

  /** @return the nationality */
  public CountryISO getNationality() {

    return nationality;
  }

  /** @param nationality the nationality to set */
  public void setNationality(CountryISO nationality) {

    this.nationality = nationality;
  }

  /** @return the countryOfResidence */
  public CountryISO getCountryOfResidence() {

    return countryOfResidence;
  }

  /** @param countryOfResidence the countryOfResidence to set */
  public void setCountryOfResidence(CountryISO countryOfResidence) {

    this.countryOfResidence = countryOfResidence;
  }

  /** @return the address */
  public Address getAddress() {

    return address;
  }

  /** @param address the address to set */
  public void setAddress(Address address) {

    this.address = address;
  }

  /** @return the occupation */
  public String getOccupation() {

    return occupation;
  }

  /** @param occupation the occupation to set */
  public void setOccupation(String occupation) {

    this.occupation = occupation;
  }

  /** @return the incomeRange */
  public IncomeRange getIncomeRange() {

    return incomeRange;
  }

  /** @param incomeRange the incomeRange to set */
  public void setIncomeRange(IncomeRange incomeRange) {

    this.incomeRange = incomeRange;
  }

  /** @return the companyType */
  public CompanyType getCompanyType() {

    return companyType;
  }

  /** @param companyType the companyType to set */
  public void setCompanyType(CompanyType companyType) {

    this.companyType = companyType;
  }

  /** @return the companyName */
  public String getCompanyName() {

    return companyName;
  }

  /** @param companyName the companyName to set */
  public void setCompanyName(String companyName) {

    this.companyName = companyName;
  }

  /** @return the companyEmail */
  public String getCompanyEmail() {

    return companyEmail;
  }

  /** @param companyEmail the companyEmail to set */
  public void setCompanyEmail(String companyEmail) {

    this.companyEmail = companyEmail;
  }

  /** @return the companyRegistrationNumber */
  public String getCompanyRegistrationNumber() {

    return companyRegistrationNumber;
  }

  /** @param companyRegistrationNumber the companyRegistrationNumber to set */
  public void setCompanyRegistrationNumber(String companyRegistrationNumber) {

    this.companyRegistrationNumber = companyRegistrationNumber;
  }

  /** @return the companyVatId */
  public String getCompanyVatId() {

    return companyVatId;
  }

  /** @param companyVatId the companyVatId to set */
  public void setCompanyVatId(String companyVatId) {

    this.companyVatId = companyVatId;
  }

  /** @return the companyAddress */
  public Address getCompanyAddress() {

    return companyAddress;
  }

  /** @param companyAddress the companyAddress to set */
  public void setCompanyAddress(Address companyAddress) {

    this.companyAddress = companyAddress;
  }

  /** @return the providerId */
  public String getProviderId() {

    return providerId;
  }

  /** @param providerId the providerId to set */
  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return the isTempDiacriticsSolved */
  public boolean isTempDiacriticsSolved() {

    return tempDiacriticsSolved;
  }

  /** @param isTempDiacriticsSolved the isTempDiacriticsSolved to set */
  public void setTempDiacriticsSolved(boolean isTempDiacriticsSolved) {

    this.tempDiacriticsSolved = isTempDiacriticsSolved;
  }

  @Override
  protected void generateKeyWords() {

    // Nothing to do in this applicationOF
  }
}
