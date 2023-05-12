package ro.iss.lolopay.programs;

import java.util.List;
import com.mangopay.MangoPayApi;
import com.mangopay.core.FilterEvents;
import com.mangopay.core.Pagination;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.entities.Event;

public class FindLostHooks {
  public static void main(String[] args) throws Exception {
    // mangopay.baseUrl = "https://api.mangopay.com"
    // mangopay.clientId = m3applicationclient
    // mangopay.clientPassword = "kLFsqg8hCDbfsi2j9Dkb4dn0w9uOdJvTbgY23DdGy4AdNN1cxb"
    // mangopay.connectionTimeout = 600000
    // mangopay.debugMode=true
    // mangopay.readTimeout = 600000
    // mangopay.returnUrl =
    // "https://payment-platform.moneymailme.com/callbacks/mango/moneymailme/m3Service/return"
    // mangopay.lastHookDate = 1513472461

    // PAYIN_NORMAL_SUCCEEDED
    // PAYIN_NORMAL_FAILED
    // PAYOUT_NORMAL_SUCCEEDED
    // PAYOUT_NORMAL_FAILED
    // TRANSFER_NORMAL_SUCCEEDED
    // TRANSFER_NORMAL_FAILED
    // PAYIN_REFUND_SUCCEEDED
    // PAYIN_REFUND_FAILED
    // PAYOUT_REFUND_SUCCEEDED
    // PAYOUT_REFUND_FAILED
    // TRANSFER_REFUND_SUCCEEDED
    // TRANSFER_REFUND_FAILED
    // TRANSFER_SETTLEMENT_SUCCEEDED
    // TRANSFER_SETTLEMENT_FAILED

    MangoPayApi api = new MangoPayApi();

    api.getConfig().setBaseUrl("https://api.mangopay.com");
    api.getConfig().setClientId("m3applicationclient");
    api.getConfig().setClientPassword("kLFsqg8hCDbfsi2j9Dkb4dn0w9uOdJvTbgY23DdGy4AdNN1cxb");
    api.getConfig().setConnectTimeout(600000);
    api.getConfig().setDebugMode(true);
    api.getConfig().setReadTimeout(600000);

    FilterEvents filterEvents = new FilterEvents();
    filterEvents.setAfterDate(1510704000L);
    filterEvents.setType(EventType.PAYIN_NORMAL_SUCCEEDED);

    Pagination pagination = new Pagination();
    pagination.setItemsPerPage(100);
    pagination.setPage(1);

    Sorting sorting = new Sorting();
    sorting.addField("Date", SortDirection.desc);

    List<Event> events = api.getEventApi().get(filterEvents, pagination, sorting);

    int counter = 0;

    for (Event event : events) {
      counter++;

      System.out.println(
          counter
              + " - "
              + event.getEventType()
              + " - "
              + event.getResourceId()
              + " - "
              + event.getDate());
    }
  }
}
