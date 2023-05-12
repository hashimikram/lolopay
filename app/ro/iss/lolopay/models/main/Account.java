package ro.iss.lolopay.models.main;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.ProviderDetail;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "accounts", noClassnameStored = true)
public class Account extends TableCollection {
  /** A given readable account id */
  private String accountId;

  /** The actual legal business name */
  private String accountName;

  /** The company logo */
  private String accountLogo;

  /** The company number provided by company registration office in the country it was created */
  private String accountRegistrationNumber;

  /** The company VAT ID / Tax ID */
  private String accountTaxId;

  /** The company account address line 1 */
  private String accountAddressLine1;

  /** The company account address line 2 */
  private String accountAddressLine2;

  /** The company account address city */
  private String accountAddressCity;

  /** The company account address county */
  private String accountAddressCounty;

  /** The company account address country */
  private String accountAddressCountry;

  /** The company registration email */
  private String accountEmail;

  /** Account database name */
  @NotNull private String databaseName;

  /** Account database user */
  @NotNull private String databaseUsername;

  /** Account database password */
  @NotNull private String databasePassword;

  /** Account financial provider details (list of operations per provider) */
  private List<ProviderDetail> providerDetails;

  /** @return the accountId */
  public String getAccountId() {

    return accountId;
  }

  /** @param accountId the accountId to set */
  public void setAccountId(String accountId) {

    this.accountId = accountId;
  }

  /** @return the accountName */
  public String getAccountName() {

    return accountName;
  }

  /** @param accountName the accountName to set */
  public void setAccountName(String accountName) {

    this.accountName = accountName;
  }

  /** @return the accountLogo */
  public String getAccountLogo() {

    return accountLogo;
  }

  /** @param accountLogo the accountLogo to set */
  public void setAccountLogo(String accountLogo) {

    this.accountLogo = accountLogo;
  }

  /** @return the accountRegistrationNumber */
  public String getAccountRegistrationNumber() {

    return accountRegistrationNumber;
  }

  /** @param accountRegistrationNumber the accountRegistrationNumber to set */
  public void setAccountRegistrationNumber(String accountRegistrationNumber) {

    this.accountRegistrationNumber = accountRegistrationNumber;
  }

  /** @return the accountTaxId */
  public String getAccountTaxId() {

    return accountTaxId;
  }

  /** @param accountTaxId the accountTaxId to set */
  public void setAccountTaxId(String accountTaxId) {

    this.accountTaxId = accountTaxId;
  }

  /** @return the accountAddressLine1 */
  public String getAccountAddressLine1() {

    return accountAddressLine1;
  }

  /** @param accountAddressLine1 the accountAddressLine1 to set */
  public void setAccountAddressLine1(String accountAddressLine1) {

    this.accountAddressLine1 = accountAddressLine1;
  }

  /** @return the accountAddressLine2 */
  public String getAccountAddressLine2() {

    return accountAddressLine2;
  }

  /** @param accountAddressLine2 the accountAddressLine2 to set */
  public void setAccountAddressLine2(String accountAddressLine2) {

    this.accountAddressLine2 = accountAddressLine2;
  }

  /** @return the accountAddressCity */
  public String getAccountAddressCity() {

    return accountAddressCity;
  }

  /** @param accountAddressCity the accountAddressCity to set */
  public void setAccountAddressCity(String accountAddressCity) {

    this.accountAddressCity = accountAddressCity;
  }

  /** @return the accountAddressCounty */
  public String getAccountAddressCounty() {

    return accountAddressCounty;
  }

  /** @param accountAddressCounty the accountAddressCounty to set */
  public void setAccountAddressCounty(String accountAddressCounty) {

    this.accountAddressCounty = accountAddressCounty;
  }

  /** @return the accountAddressCountry */
  public String getAccountAddressCountry() {

    return accountAddressCountry;
  }

  /** @param accountAddressCountry the accountAddressCountry to set */
  public void setAccountAddressCountry(String accountAddressCountry) {

    this.accountAddressCountry = accountAddressCountry;
  }

  /** @return the accountEmail */
  public String getAccountEmail() {

    return accountEmail;
  }

  /** @param accountEmail the accountEmail to set */
  public void setAccountEmail(String accountEmail) {

    this.accountEmail = accountEmail;
  }

  /** @return the databaseName */
  public String getDatabaseName() {

    return databaseName;
  }

  /** @param databaseName the databaseName to set */
  public void setDatabaseName(String databaseName) {

    this.databaseName = databaseName;
  }

  /** @return the databaseUsername */
  public String getDatabaseUsername() {

    return databaseUsername;
  }

  /** @param databaseUsername the databaseUsername to set */
  public void setDatabaseUsername(String databaseUsername) {

    this.databaseUsername = databaseUsername;
  }

  /** @return the databasePassword */
  public String getDatabasePassword() {

    return databasePassword;
  }

  /** @param databasePassword the databasePassword to set */
  public void setDatabasePassword(String databasePassword) {

    this.databasePassword = databasePassword;
  }

  /** @return the providerDetails */
  public List<ProviderDetail> getProviderDetails() {

    return providerDetails;
  }

  /** @param providerDetails the providerDetails to set */
  public void setProviderDetails(List<ProviderDetail> providerDetails) {

    this.providerDetails = providerDetails;
  }
}
