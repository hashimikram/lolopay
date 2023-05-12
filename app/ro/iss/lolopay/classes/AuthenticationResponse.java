package ro.iss.lolopay.classes;

import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;

public class AuthenticationResponse {
  private Account authenticatedAccount;

  private Application authenticatedApplication;

  /** @return the authenticatedAccount */
  public Account getAuthenticatedAccount() {

    return authenticatedAccount;
  }

  /** @param authenticatedAccount the authenticatedAccount to set */
  public void setAuthenticatedAccount(Account authenticatedAccount) {

    this.authenticatedAccount = authenticatedAccount;
  }

  /** @return the authenticatedApplication */
  public Application getAuthenticatedApplication() {

    return authenticatedApplication;
  }

  /** @param authenticatedApplication the authenticatedApplication to set */
  public void setAuthenticatedApplication(Application authenticatedApplication) {

    this.authenticatedApplication = authenticatedApplication;
  }
}
