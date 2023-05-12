package ro.iss.lolopay.models.database;

import java.util.HashMap;
import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "settings", noClassnameStored = true)
public class AccountSettings extends TableCollection {
  private HashMap<String, Object> customSettings;

  /**
   * List with Id's of the account wallets, this wallets are used for registering user transactions
   * fee and account operations
   */
  private List<String> accountWalletIds;

  /** @return the accountWalletIds */
  public List<String> getAccountWalletIds() {

    return accountWalletIds;
  }

  /** @param accountWalletIds the accountWalletIds to set */
  public void setAccountWalletIds(List<String> accountWalletIds) {

    this.accountWalletIds = accountWalletIds;
  }

  /** @return the customSettings */
  public HashMap<String, Object> getCustomSettings() {

    return customSettings;
  }

  /** @param customSettings the customSettings to set */
  public void setCustomSettings(HashMap<String, Object> customSettings) {

    this.customSettings = customSettings;
  }
}
