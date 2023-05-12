package ro.iss.lolopay.programs;

import java.util.List;
import com.typesafe.config.ConfigFactory;
import ro.iss.lolopay.classes.PaginatedList;
import ro.iss.lolopay.classes.provider.ProviderResponse;
import ro.iss.lolopay.models.classes.TableCollection;
import ro.iss.lolopay.models.database.Refund;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.definition.RefundService;
import ro.iss.lolopay.models.services.definition.TransferService;
import ro.iss.lolopay.models.services.implementation.RefundImplementation;
import ro.iss.lolopay.models.services.implementation.TransferImplementation;
import ro.iss.lolopay.services.definition.DatabaseService;
import ro.iss.lolopay.services.definition.LogService;
import ro.iss.lolopay.services.definition.MangoPayService;
import ro.iss.lolopay.services.implementation.DatabaseImplementation;
import ro.iss.lolopay.services.implementation.LogImplementation;
import ro.iss.lolopay.services.implementation.MangoPayImplementation;

public class RepairTrans {
  public static void main(String[] args) {

    LogService logService = new LogImplementation();
    DatabaseService databaseService = new DatabaseImplementation(logService);

    String requestId = "1234";
    Account account =
        databaseService.getMainConnection().createQuery(Account.class).asList().get(0);

    // get wallet implementation for database query
    RefundService refundService = new RefundImplementation(databaseService);
    MangoPayService mangoPayService = new MangoPayImplementation(logService);
    TransferService transferService = new TransferImplementation(databaseService);
    int page = 0;
    int pageSize = ConfigFactory.load().getInt("application.maxRecordsToBeRetrievedFromDB");

    while (true) {
      PaginatedList paginatedList = refundService.getRefunds(requestId, account, page, pageSize);

      List<? extends TableCollection> tableCollections = paginatedList.getList();

      for (TableCollection tableCollection : tableCollections) {

        Refund refund = (Refund) tableCollection;
        if (refund.getInitialTransactionId() == null
            || refund.getInitialTransactionId().equals("")) {
          //
          // if (refund.getInitialTransactionId() == null) {
          // System.out.println("found refund without initialtransactionid");
          // System.out.println(Json.toJson(refund));
          //
          // }
          // else {
          // if (refund.getInitialTransactionId().equals("")) {
          // System.out.println("found refund with initialtransactionid == \"\"");
          // System.out.println(Json.toJson(refund));
          //
          // }
          // }
          //
          ProviderResponse providerResponse =
              mangoPayService.getProviderDepositRefund(requestId, refund.getProviderId());

          Transfer transfer =
              transferService.getTransferByProviderId(
                  requestId,
                  account,
                  providerResponse.getProviderData("InitialTransactionId").toString());

          refund.setInitialTransactionType(transfer.getType());
          refund.setInitialTransactionId(transfer.getId());

          refundService.saveRefund(requestId, account, refund);
        }
      }

      if (paginatedList.getList().size() == pageSize) {
        page = 1 + page;
      } else {
        break;
      }
    }
  }
}
