package ro.iss.lolopay.programs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class LoLoImportChecksum {

  public static void main(String[] args) throws Exception {

    LogService logService = new LogImplementation();
    DatabaseService dbService = new DatabaseImplementation(logService);

    // get main account
    Account account =
        (Account) dbService.getMainRecord("", "5989e241f5b0af2f948ba09f", Account.class);
    String checkSumFile = "";

    if (ConfigFactory.load().getString("application.environment").equals("local")) {
      checkSumFile =
          "/home/alin/JavaWorkspace/LoloPayMigrationFolder/BkUp/mangoWallets28112017.csv";
    } else if (ConfigFactory.load().getString("application.environment").equals("test")) {
      checkSumFile = "";
    } else if (ConfigFactory.load().getString("application.environment").equals("live")) {
      checkSumFile = "/home/cornel/migrationFolder/mangoWallets28112017.csv";
    }

    // Open the file
    FileInputStream fstream = new FileInputStream(checkSumFile);
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

    String strLine;

    // Read File Line By Line
    while ((strLine = br.readLine()) != null) {
      // Print the content on the console
      String[] walletLine = strLine.split(",");
      // System.out.println(walletLine[0] + ": " + walletLine[5] + " " + walletLine[6]);

      // map wallet
      ro.iss.lolopay.models.database.Wallet wallet =
          dbService
              .getConnection(account.getId())
              .get(ro.iss.lolopay.models.database.Wallet.class, walletLine[0]);

      if (wallet == null) {
        System.out.println(
            walletLine[4]
                + "	"
                + "Missing wallet in database: "
                + walletLine[0]
                + " user("
                + walletLine[4]
                + ")");
      } else {
        if (!walletLine[6].equals(wallet.getBalance().getCurrency().toString())) {
          System.out.println(
              wallet.getUserId()
                  + "	"
                  + "Wrong currency in database for : "
                  + walletLine[0]
                  + " user("
                  + wallet.getUserId()
                  + ")");
        } else {
          if (!walletLine[5].equals(wallet.getBalance().getValue().toString())) {
            System.out.println(
                wallet.getUserId()
                    + "	"
                    + "Wrong balance in database for : "
                    + walletLine[0]
                    + " - File is "
                    + walletLine[5]
                    + " database is "
                    + wallet.getBalance().getValue().toString()
                    + " user("
                    + wallet.getUserId()
                    + ")");
          }
        }
      }
    }

    // Close the input stream
    br.close();

    System.out.println("Done");
  }
}
