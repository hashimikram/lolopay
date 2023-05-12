package ro.iss.lolopay.models.services.implementation;

import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.AccountSettingsService;
import ro.iss.lolopay.services.definition.CacheService;

@Singleton
public class AccountSettingsImplementation implements AccountSettingsService {
  @Inject private CacheService cacheService;

  @Override
  public AccountSettings getAccountSettings(String requestId, Account account) {

    return cacheService.getAccountSettings(requestId, account);
  }

  @Override
  public Wallet getAccountWallet(String requestId, Account account, CurrencyISO currencyISO) {

    return cacheService.getAccountWallet(requestId, account, currencyISO);
  }

  @Override
  public HashMap<String, Object> getAccountCustomSettings(String requestId, Account account) {

    AccountSettings accountSettings = cacheService.getAccountSettings(requestId, account);

    if (accountSettings != null) {
      return accountSettings.getCustomSettings();
    }

    return null;
  }
}
