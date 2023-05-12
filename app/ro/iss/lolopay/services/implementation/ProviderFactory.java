package ro.iss.lolopay.services.implementation;

import javax.inject.Inject;
import javax.inject.Singleton;
import play.Logger;
import ro.iss.lolopay.models.classes.FinancialProvider;
import ro.iss.lolopay.models.classes.ProviderDetail;
import ro.iss.lolopay.models.classes.ProviderOperation;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.definition.InternalProviderService;
import ro.iss.lolopay.services.definition.MangoPayService;
import ro.iss.lolopay.services.definition.PFSService;
import ro.iss.lolopay.services.definition.ProviderService;

@Singleton
public class ProviderFactory {
  @Inject MangoPayService mangoPayService;

  @Inject InternalProviderService internalProviderService;

  @Inject PFSService pfsService;

  public ProviderFactory() {

    Logger.of(this.getClass()).debug("Singleton created");
  }

  /**
   * Determine provider instance to be used to specified operation of an account
   *
   * @param account
   * @param providerOperation
   * @return
   */
  public ProviderService getProvider(Account account, ProviderOperation providerOperation) {

    // this approach requires adding new lines for each new providers => new compilation
    for (ProviderDetail providerDetail : account.getProviderDetails()) {
      // test if operation is identified
      if (providerDetail.getProviderOperation().equals(providerOperation)) {
        // test first provider for iterated operation
        if (providerDetail.getFinancialProvider().equals(FinancialProvider.PFS)) {
          return pfsService;
        } else
        // test second provider for iterated operation
        if (providerDetail.getFinancialProvider().equals(FinancialProvider.MANGO)) {
          return mangoPayService;
        } else {
          return internalProviderService;
        }
      }
    }

    // if operation is not defined - default internal provider implementation is used
    return internalProviderService;
  }
}
