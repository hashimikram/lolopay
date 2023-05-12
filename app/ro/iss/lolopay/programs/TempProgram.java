package ro.iss.lolopay.programs;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import ro.iss.lolopay.services.definition.UtilsService;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

public class TempProgram {

  public static void main(String[] args) throws Exception {

    System.out.println(ContentType.APPLICATION_JSON.getMimeType());
    String string = "/callbacks/mango/%s/%s";

    String result = String.format(string, "asa", "cumvreau");

    System.out.println(result);
    System.out.println();
    LocalDate today =
        LocalDate.now(Clock.fixed(Instant.ofEpochSecond(1551274695L), ZoneOffset.UTC));

    System.out.println(today.format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

    long sevedDaysAgo = today.minusDays(7).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    System.out.println("sevedDaysAgo: " + sevedDaysAgo);

    System.out.println("*******************END *******************");

    UtilsService utilsService = new UtilsImplementation();

    Long transactionDatePfs =
        utilsService.stringDateToTimeStamp("03/06/2019 11:23:42 AM", "MM/dd/yyyy hh:mm:ss a");
    System.out.println("*********" + (transactionDatePfs + (60 * 60 * 7)));

    // LocalDate todayDate =
    // LocalDate.now(Clock.fixed(Instant.ofEpochSecond(utilsService.getTimeStamp()),
    // ZoneId.of("GMT")));
    LocalDate todayDate =
        LocalDate.now(Clock.fixed(Instant.ofEpochSecond(1519909891L), ZoneId.of("GMT")));
    System.out.println(todayDate.format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

    LocalDate tomorow = todayDate.plusDays(1);
    System.out.println(tomorow.format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

    LocalDate requestedLocalEndDate =
        LocalDate.now(Clock.fixed(Instant.ofEpochSecond(1519909891L), ZoneId.of("GMT")));
    System.out.println(requestedLocalEndDate.format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

    if (requestedLocalEndDate.equals(todayDate)) {
      System.out.println("sunt aceleasi zile");
    }
    long startOfDaySeconds = requestedLocalEndDate.atStartOfDay(ZoneId.of("GMT")).toEpochSecond();
    System.out.println(startOfDaySeconds);
    System.out.println("**********");
    LocalDate localDate = LocalDate.now().minusDays(7);
    System.out.println(localDate.format(DateTimeFormatter.ofPattern("dd LLLL yyyy")));

    // get now date
    ZonedDateTime now = Instant.ofEpochSecond(utilsService.getTimeStamp()).atZone(ZoneId.of("GMT"));
    System.out.println(now.toString() + " " + now.toEpochSecond());

    // get date of 7 days ago
    ZonedDateTime sevenDaysAgo = now.minusDays(7);
    System.out.println(sevenDaysAgo.toString() + " " + sevenDaysAgo.toEpochSecond());

    // get date time of 7 days ago at midnight
    // ZonedDateTime zonedSevenDaysAgoDateTime = LocalDateTime.of(sevenDaysAgo,
    // LocalTime.MAX).atZone(ZoneId.of("GMT"));
    ZonedDateTime zonedSevenDaysAgoDateTime =
        ZonedDateTime.of(sevenDaysAgo.toLocalDate(), LocalTime.MAX, ZoneId.of("GMT"));
    System.out.println(zonedSevenDaysAgoDateTime.toEpochSecond());

    String requestId = UUID.randomUUID().toString();
    System.out.println(requestId);
    for (int i = 0; i < 29; i++) {
      // System.out.println(UUID.randomUUID().toString());
    }

    /*
     * Map<String, String[]> formMap = new HashMap<>(); String[] firstString = new String[5]; firstString[0] = "f"; firstString[1] = "i"; firstString[2] = "r"; firstString[3] = "s"; firstString[4] = "t"; String[] secondString = new String[5]; secondString[0] = "s"; secondString[1] = "e"; secondString[2] = "c"; secondString[3] = "o"; secondString[4] = "n"; StringBuffer output = new StringBuffer(); formMap.put("firstKey", firstString); formMap.put("secondKey", secondString); for (Map.Entry<String, String[]> entry : formMap.entrySet()) { StringBuilder value = new StringBuilder(); for (int i = 0; i < entry.getValue().length; i++) { value = value.append(entry.getValue()[i]).append("|_|"); } output.append(entry.getKey().toString()).append(": ").append(value.toString()).append(System.lineSeparator()); } System.out.println(output);
     */

    // System.out.println(Instant.now().getEpochSecond());
    //
    //
    //
    // LocalDate now = LocalDate.now(ZoneId.of("GMT"));
    // ZonedDateTime zonedTodayDateTime = LocalDateTime.of(now,
    // LocalTime.MIN).atZone(ZoneId.of("GMT"));
    //

    // System.out.println(Instant.now().getEpochSecond());

    //
    // LocalDate now = LocalDate.now(ZoneId.of("GMT"));
    // ZonedDateTime zonedTodayDateTime = LocalDateTime.of(now,
    // LocalTime.MIN).atZone(ZoneId.of("GMT"));
    //
    // System.out.println(zonedTodayDateTime.toInstant().getEpochSecond());
    // 1540468800
    // System.out.println("START");
    // Instant tomorowInstant = Instant.now().plus(1, ChronoUnit.DAYS);
    //
    // System.out.println(tomorowInstant.toEpochMilli() / 1000L);
    //
    // LocalDate startDate =
    // Instant.ofEpochSecond(1483228801).atZone(ZoneId.of("GMT")).toLocalDate();
    // LocalDate endDate = Instant.ofEpochSecond(1518612585).atZone(ZoneId.of("GMT")).toLocalDate();
    // ;
    // System.out.println(startDate);
    // System.out.println(endDate);
    // System.out.println();
    // LocalTime midnight = LocalTime.NOON;
    // LocalDateTime todayMidnight = LocalDateTime.of(LocalDate.now(ZoneId.of("GMT")),
    // LocalTime.MIDNIGHT);
    // LocalDate yesterday = LocalDate.now(ZoneId.of("GMT")).minusDays(1);
    // ZonedDateTime zonedDateTime = LocalDateTime.of(yesterday,
    // LocalTime.MAX).atZone(ZoneId.of("GMT"));
    // long longEndDate = zonedDateTime.toInstant().toEpochMilli() / 1000;
    // System.out.println(longEndDate);

  }
}
