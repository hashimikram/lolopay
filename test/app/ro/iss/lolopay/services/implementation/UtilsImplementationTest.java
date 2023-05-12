package app.ro.iss.lolopay.services.implementation;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilsImplementationTest {
  private UtilsImplementation utils = new UtilsImplementation();

  @Test
  public void testStringDateToTimeStamp() {

    ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

    // pfs server is in Florida, zoneid America/New_York
    ZoneId ZONE_ID_PFS_SERVER = ZoneId.of("America/New_York");

    // Florida Time from PFS
    String dateFromPfs = "07/10/2019 08:58:31 AM";
    String dateFormat = "MM/dd/yyyy hh:mm:ss a";

    Long timestamp = utils.stringDateToTimeStamp(dateFromPfs, dateFormat, ZONE_ID_PFS_SERVER);

    // convert timestamp to UTC date
    ZonedDateTime date = Instant.ofEpochSecond(timestamp).atZone(ZONE_ID_UTC);

    // summer time, the Florida date si GMT-4
    ZonedDateTime expectedDateUtc = ZonedDateTime.of(2019, 7, 10, 12, 58, 31, 0, ZONE_ID_UTC);
    assertEquals(expectedDateUtc, date);
  }

  @Test
  public void testStringDateToTimeStampWithDifferentSystemTimezone() {

    // testing with different system timezone. It shouldn't matter.
    TimeZone origTz = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));

    testStringDateToTimeStamp();

    // restoring system timezone
    TimeZone.setDefault(origTz);
  }

  @Test
  public void testStringDateToUTC() {

    String dateFormat = "yyyyMMddHHmmss";
    String datePFS = "20200120081356";
    ZoneId ZONE_ID_PFS_SERVER = ZoneId.of("America/New_York");

    assertEquals("20200120131356", utils.stringDateToUTC(datePFS, dateFormat, ZONE_ID_PFS_SERVER));

    // if bad format return the original string
    String datePFSWrongFormat = "2020-01-20 08:13:56";
    assertEquals(
        datePFSWrongFormat,
        utils.stringDateToUTC(datePFSWrongFormat, dateFormat, ZONE_ID_PFS_SERVER));
  }
}
