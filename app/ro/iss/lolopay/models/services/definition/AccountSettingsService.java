package ro.iss.lolopay.models.services.definition;

import java.util.HashMap;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.classes.CurrencyISO;
import ro.iss.lolopay.models.database.AccountSettings;
import ro.iss.lolopay.models.database.Wallet;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.AccountSettingsImplementation;

@ImplementedBy(AccountSettingsImplementation.class)
public interface AccountSettingsService {
  public AccountSettings getAccountSettings(String requestId, Account account);

  public Wallet getAccountWallet(String requestId, Account account, CurrencyISO currencyISO);

  public HashMap<String, Object> getAccountCustomSettings(String requestId, Account account);
}
