package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.Address;
import ro.iss.lolopay.models.classes.CardType;
import ro.iss.lolopay.models.classes.CultureCode;
import ro.iss.lolopay.models.classes.ExecutionType;
import ro.iss.lolopay.models.classes.PaymentType;
import ro.iss.lolopay.models.classes.SecureMode;
import ro.iss.lolopay.models.classes.SecurityInfo;

@Entity(value = "transactions", noClassnameStored = true)
public class PayIn extends Transaction {

  /** Payment type can be Card or Bank, it applies to PayIn and PayOut only */
  private PaymentType paymentType;

  /** The type of card */
  private CardType cardType;

  /** The id of card at the provider */
  private String cardProviderId;

  /** The URL to redirect to after payment (whether successful or not) */
  private String returnURL;

  /** The type of execution for the pay in (only WEB supported for now) */
  private ExecutionType executionType;

  /**
   * The SecureMode corresponds to '3D secure' for CB Visa and MasterCard. This field lets you
   * activate it manually. The field lets you activate it automatically with "DEFAULT" (Secured Mode
   * will be activated from €50 or when MANGOPAY detects there is a higher risk ), "FORCE" (if you
   * wish to specifically force the secured mode).
   */
  private SecureMode secureMode;

  /** The language to use for the payment page - needs to be the ISO code of the language */
  private CultureCode culture;

  /**
   * A URL to an SSL page to allow you to customise the payment page. Must be in the format:
   * array("PAYLINE"=>"https://...") and meet all the specifications listed here:
   * https://docs.mangopay.com/guide/customising-the-design. Note that only a template for Payline
   * is currently available
   */
  private String templateURL;

  /**
   * A custom description to appear on the user's bank statement. It can be up to 10 characters
   * long, and can only include alphanumeric characters or spaces. See thsi link
   * https://docs.mangopay.com/guide/customising-bank-statement-references for important info and
   * note that this functionality is in private beta and not available for all clients.
   */
  private String statementDescriptor;

  /** An external reference/provider bank transfer ID */
  private String externalReference;

  /** The URL returned to client, which will be used by users to enter card details */
  private String redirectURL;

  /**
   * This is the URL where users are automatically redirected after 3D secure validation (if
   * activated) in DIRECT PAYINS
   */
  private String secureModeReturnUrl;

  /** This is the URL where users must be redirected to enter 3DSecure password in DIRECT PAYINS */
  private String secureModeRedirectUrl;

  /** Contains every useful informations related to the user billing */
  private Address billing;

  /** Contains useful informations related to security and fraud */
  private SecurityInfo securityInfo;

  /** @return the securityInfo */
  public SecurityInfo getSecurityInfo() {

    return securityInfo;
  }

  /** @param securityInfo the securityInfo to set */
  public void setSecurityInfo(SecurityInfo securityInfo) {

    this.securityInfo = securityInfo;
  }

  /** @return the billing */
  public Address getBilling() {

    return billing;
  }

  /** @param billing the billing to set */
  public void setBilling(Address billing) {

    this.billing = billing;
  }

  /** @return the paymentType */
  public PaymentType getPaymentType() {

    return paymentType;
  }

  /** @param paymentType the paymentType to set */
  public void setPaymentType(PaymentType paymentType) {

    this.paymentType = paymentType;
  }

  /** @return the cardType */
  public CardType getCardType() {

    return cardType;
  }

  /** @param cardType the cardType to set */
  public void setCardType(CardType cardType) {

    this.cardType = cardType;
  }

  /** @return the returnURL */
  public String getReturnURL() {

    return returnURL;
  }

  /** @param returnURL the returnURL to set */
  public void setReturnURL(String returnURL) {

    this.returnURL = returnURL;
  }

  /** @return the executionType */
  public ExecutionType getExecutionType() {

    return executionType;
  }

  /** @param executionType the executionType to set */
  public void setExecutionType(ExecutionType executionType) {

    this.executionType = executionType;
  }

  /** @return the secureMode */
  public SecureMode getSecureMode() {

    return secureMode;
  }

  /** @param secureMode the secureMode to set */
  public void setSecureMode(SecureMode secureMode) {

    this.secureMode = secureMode;
  }

  /** @return the culture */
  public CultureCode getCulture() {

    return culture;
  }

  /** @param culture the culture to set */
  public void setCulture(CultureCode culture) {

    this.culture = culture;
  }

  /** @return the templateURL */
  public String getTemplateURL() {

    return templateURL;
  }

  /** @param templateURL the templateURL to set */
  public void setTemplateURL(String templateURL) {

    this.templateURL = templateURL;
  }

  /** @return the statementDescriptor */
  public String getStatementDescriptor() {

    return statementDescriptor;
  }

  /** @param statementDescriptor the statementDescriptor to set */
  public void setStatementDescriptor(String statementDescriptor) {

    this.statementDescriptor = statementDescriptor;
  }

  /** @return the externalReference */
  public String getExternalReference() {

    return externalReference;
  }

  /** @param externalReference the externalReference to set */
  public void setExternalReference(String externalReference) {

    this.externalReference = externalReference;
  }

  /** @return the redirectURL */
  public String getRedirectURL() {

    return redirectURL;
  }

  /** @param redirectURL the redirectURL to set */
  public void setRedirectURL(String redirectURL) {

    this.redirectURL = redirectURL;
  }

  /** @return the secureModeReturnUrl */
  public String getSecureModeReturnUrl() {

    return secureModeReturnUrl;
  }

  /** @param secureModeReturnUrl the secureModeReturnUrl to set */
  public void setSecureModeReturnUrl(String secureModeReturnUrl) {

    this.secureModeReturnUrl = secureModeReturnUrl;
  }

  /** @return the cardProviderId */
  public String getCardProviderId() {

    return cardProviderId;
  }

  /** @param cardProviderId the cardProviderId to set */
  public void setCardProviderId(String cardProviderId) {

    this.cardProviderId = cardProviderId;
  }

  /** @return the secureModeRedirectUrl */
  public String getSecureModeRedirectUrl() {

    return secureModeRedirectUrl;
  }

  /** @param secureModeRedirectUrl the secureModeRedirectUrl to set */
  public void setSecureModeRedirectUrl(String secureModeRedirectUrl) {

    this.secureModeRedirectUrl = secureModeRedirectUrl;
  }
}
