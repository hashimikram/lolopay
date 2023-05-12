package ro.iss.lolopay.programs;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import play.libs.Json;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.programs.classes.CSVTransCol;
import ro.iss.lolopay.programs.classes.LoLoClient;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;

public class LoloImport {

  public static void main(String[] args) throws Exception {

    String migrationFolder = "";

    if (ConfigFactory.load().getString("application.environment").equals("local")) {
      migrationFolder = "/home/alin/JavaWorkspace/LoloPayMigrationFolder/migrationFolder";
    } else if (ConfigFactory.load().getString("application.environment").equals("test")) {
      migrationFolder = "/home/sorin/mangoexport/migrationFolder";
    } else if (ConfigFactory.load().getString("application.environment").equals("live")) {
      migrationFolder = "/home/cornel/migrationFolder";
    }

    System.out.println("migrationFolder:" + migrationFolder);

    LogService logService = new LogImplementation();
    DatabaseService dbService = new DatabaseImplementation(logService);

    Account account =
        (Account) dbService.getMainRecord("", "5989e241f5b0af2f948ba09f", Account.class);

    // Load users
    int noUsers = 0;
    File dir = new File(migrationFolder + "/Users");
    File[] directoryListing = dir.listFiles();
    for (File child : directoryListing) {
      // import user file
      try (FileInputStream is =
          new FileInputStream(migrationFolder + "/Users/" + child.getName())) {
        // get user file json string
        final JsonNode json = Json.parse(is);
        String mangoUserType = json.findPath("personType").asText();

        if (mangoUserType.equals("NATURAL")) {
          // map natural user
          ro.iss.lolopay.models.database.User loloUserNatural =
              LoLoClient.mapJsonToLoloNaturalUser(json);
          dbService.getConnection(account.getId()).save(loloUserNatural);
        } else {
          // map legal suer
          ro.iss.lolopay.models.database.User loloUserLegal =
              LoLoClient.mapJsonToLoloLegalUser(json);
          dbService.getConnection(account.getId()).save(loloUserLegal);
        }
        noUsers++;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        break;
      }
    }
    System.out.println("Users saved in db:" + noUsers);

    // load wallets
    int walletsSavedInDb = 0;
    dir = new File(migrationFolder + "/Wallets");
    directoryListing = dir.listFiles();
    for (File child : directoryListing) {
      // import user file
      try (FileInputStream is =
          new FileInputStream(migrationFolder + "/Wallets/" + child.getName())) {
        // get user file json string
        final JsonNode json = Json.parse(is);

        // map wallet
        ro.iss.lolopay.models.database.Wallet loloWallet = LoLoClient.mapJsonToLoloWallet(json);
        dbService.getConnection(account.getId()).save(loloWallet);
        walletsSavedInDb++;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        break;
      }
    }
    System.out.println("Wallets saved in db: " + walletsSavedInDb);

    // load bank accounts
    int bankAccountsSavedInDb = 0;
    dir = new File(migrationFolder + "/BankAccounts");
    directoryListing = dir.listFiles();
    for (File child : directoryListing) {
      // import user file
      try (FileInputStream is =
          new FileInputStream(migrationFolder + "/BankAccounts/" + child.getName())) {
        // get user file json string
        final JsonNode json = Json.parse(is);
        String bankAccountType = json.findPath("type").asText();

        switch (bankAccountType) {
          case "IBAN":

            // map iban
            ro.iss.lolopay.models.database.BankAccountIBAN loloBankAccountIBAN =
                LoLoClient.mapJsonToBankAccountIBAN(json);
            dbService.getConnection(account.getId()).save(loloBankAccountIBAN);

            break;
          case "GB":

            // map GB
            ro.iss.lolopay.models.database.BankAccountGB loloBankAccountGB =
                LoLoClient.mapJsonToBankAccountGB(json);
            dbService.getConnection(account.getId()).save(loloBankAccountGB);

            break;
          case "US":

            // map US
            ro.iss.lolopay.models.database.BankAccountUS loloBankAccountUS =
                LoLoClient.mapJsonToBankAccountUS(json);
            dbService.getConnection(account.getId()).save(loloBankAccountUS);

            break;
          case "CA":

            // map CA
            ro.iss.lolopay.models.database.BankAccountCA loloBankAccountCA =
                LoLoClient.mapJsonToBankAccountCA(json);
            dbService.getConnection(account.getId()).save(loloBankAccountCA);

            break;
          case "OTHER":

            // map OTHER
            ro.iss.lolopay.models.database.BankAccountOTHER loloBankAccountOTHER =
                LoLoClient.mapJsonToBankAccountOTHER(json);
            dbService.getConnection(account.getId()).save(loloBankAccountOTHER);

            break;
          default:
            break;
        }

        bankAccountsSavedInDb++;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        break;
      }
    }
    System.out.println("Bank accounts saved in db: " + bankAccountsSavedInDb);

    // load documents
    int documentsSaved = 0;
    dir = new File(migrationFolder + "/Documents");
    directoryListing = dir.listFiles();
    for (File child : directoryListing) {
      // import user file
      try (FileInputStream is =
          new FileInputStream(migrationFolder + "/Documents/" + child.getName())) {
        // get user file json string
        final JsonNode json = Json.parse(is);

        // map document
        ro.iss.lolopay.models.database.Document loloDocument =
            LoLoClient.mapJsonToLoloDocument(json);
        dbService.getConnection(account.getId()).save(loloDocument);
        documentsSaved++;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        break;
      }
    }
    System.out.println("Documents saved in db: " + documentsSaved);

    System.out.println("Look up transaction files in " + migrationFolder + "/Transactions");

    // load documents
    int filesImported = 0;
    dir = new File(migrationFolder + "/Transactions");
    directoryListing = dir.listFiles();
    for (File child : directoryListing) {

      CSVParser parser = CSVParser.parse(child, Charset.defaultCharset(), CSVFormat.RFC4180);

      for (CSVRecord csvRecord : parser) {
        // skip first line
        if (csvRecord.get(0).equals("Id")) continue;

        // skip dummy lines with X X X from test 5 sandbox
        if (csvRecord.get(CSVTransCol.DebitedFundsCurrency).equals("XXX")) continue;

        switch (csvRecord.get(17)) {
          case "TRANSFER":
            if (csvRecord.get(18).equals("REGULAR")) {
              LoLoClient.mapCSVRecordToLoloTransferRegular(dbService, account, csvRecord);
            } else if (csvRecord.get(18).equals("SETTLEMENT")) {
              LoLoClient.mapCSVRecordToLoloTransferSettlement(dbService, account, csvRecord);
            } else if (csvRecord.get(18).equals("REFUND")) {
              LoLoClient.mapCSVRecordToLoloTransferRefund(dbService, account, csvRecord);
            } else {
              throw new Exception("TRANSFER " + csvRecord.get(18) + " not implemented for import");
            }
            break;
          case "PAYIN":
            if (csvRecord.get(18).equals("REGULAR")) {
              LoLoClient.mapCSVRecordToLoloPayInRegular(dbService, account, csvRecord);
            } else if (csvRecord.get(18).equals("REFUND")) {
              // when you refund a withdrawal
              LoLoClient.mapCSVRecordToLoloPayInRefund(dbService, account, csvRecord);
            } else {
              throw new Exception("PAYIN " + csvRecord.get(18) + " not implemented for import");
            }
            break;
          case "PAYOUT":
            if (csvRecord.get(18).equals("REGULAR")) {
              switch (csvRecord.get(CSVTransCol.DebitedWalletId)) {
                case "59137858":
                  System.out.println("Skipped pay out from 59137858 (FEES_USD)");
                  break;

                case "FEES_USD":
                  System.out.println("Skipped pay out from (FEES_USD)");
                  break;

                case "45445090":
                  System.out.println("Skipped pay out from 45445090 (FEES_EUR)");
                  break;

                case "FEES_EUR":
                  System.out.println("Skipped pay out from FEES_EUR");
                  break;

                case "58106402":
                  System.out.println("Skipped pay out from 58106402 (FEES_GBP)");
                  break;

                case "FEES_GBP":
                  System.out.println("Skipped pay out from FEES_GBP");
                  break;

                case "132766530":
                  System.out.println("Skipped pay out from 132766530 (FEES_ZAR)");
                  break;

                case "129635750":
                  System.out.println("Skipped pay out from 129635750 (FEES_ZAR)");
                  break;

                case "FEES_ZAR":
                  System.out.println("Skipped pay out from FEES_ZAR");
                  break;

                case "130082857":
                  System.out.println("Skipped pay out from 130082857 (FEES_PLN)");
                  break;

                case "FEES_PLN":
                  System.out.println("Skipped pay out from FEES_PLN");
                  break;

                case "130085049":
                  System.out.println("Skipped pay out from 130085049 (FEES_CHF)");
                  break;

                case "FEES_CHF":
                  System.out.println("Skipped pay out from FEES_CHF");
                  break;

                default:
                  try {
                    LoLoClient.mapCSVRecordToLoloPayOutRegular(dbService, account, csvRecord);
                  } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(
                        "Belea file: "
                            + child.getName()
                            + " Belea Id: "
                            + csvRecord.get(CSVTransCol.Id)
                            + " Belea Debit Wallet Id:"
                            + csvRecord.get(CSVTransCol.DebitedWalletId)
                            + " Belea Author Id:"
                            + csvRecord.get(CSVTransCol.AuthorId)
                            + " Belea Credit Wallet Id:"
                            + csvRecord.get(CSVTransCol.CreditedWalletId)
                            + " Belea Credited User Id:"
                            + csvRecord.get(CSVTransCol.CreditedUserId)
                            + " Belea Bank Account Id:"
                            + csvRecord.get(CSVTransCol.BankAccountId));
                  }
                  break;
              }
            } else if (csvRecord.get(18).equals("REFUND")) {
              // when you refund a card deposit
              LoLoClient.mapCSVRecordToLoloPayOutRefund(dbService, account, csvRecord);
            } else if (csvRecord.get(18).equals("REPUDIATION")) {
              System.out.println("Skipped repudiation");
            } else {
              throw new Exception("TRANSFER " + csvRecord.get(18) + " not implemented for import");
            }
            break;
          default:
            break;
        }
      }

      filesImported++;
    }
    System.out.println("Files imported: " + filesImported);
  }
}
