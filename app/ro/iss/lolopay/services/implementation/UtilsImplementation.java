package ro.iss.lolopay.services.implementation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Random;
import javax.inject.Singleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.Json;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class UtilsImplementation implements UtilsService {

  private static long nowTimestamp = 0L;

  private static String environment = ConfigFactory.load().getString("application.environment");

  /** Log singleton creation moment */
  public UtilsImplementation() {

    Logger.of(this.getClass()).debug("Singleton created");
  }

  /**
   * Generate random string between from and to specified parameters
   *
   * @param size
   * @return
   */
  @Override
  public String generateRandomString(int size) {

    char[] chars = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
    Random rnd = new Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= size; i++) {
      sb.append(chars[rnd.nextInt(chars.length)]);
    }
    return sb.toString();
  }

  /**
   * Return time stamp in m seconds
   *
   * @return
   */
  @Override
  public long getCurrentTimeMiliseconds() {

    if (environment.equals("local")) {
      if (nowTimestamp == 0L) {
        setNowTimestamp(System.currentTimeMillis());
      }
      return nowTimestamp;
    }
    return System.currentTimeMillis();
  }

  /**
   * Return time stamp in m seconds
   *
   * @return
   */
  @Override
  public long getTimeStamp() {

    return (getCurrentTimeMiliseconds() / 1000L);
  }

  /**
   * Generate database name based on entity name, by removing , . - and empty space and adding an
   * unique time stamp at the end
   *
   * @param accountName
   * @return
   */
  @Override
  public String generateDbName(String accountName) {

    accountName = accountName.replaceAll("\\.", "");
    accountName = accountName.replaceAll("\\_", "");
    accountName = accountName.replaceAll("\\-", "");
    accountName = accountName.replaceAll("\\s", "");

    if (accountName.length() > 10) {
      accountName = accountName.substring(0, 10);
    }

    accountName = accountName.concat("_").concat(String.valueOf(getCurrentTimeMiliseconds()));

    return accountName.toLowerCase();
  }

  @Override
  public String timeStampToDate(Long timeStamp, String dateFormat) {

    /// time stamp converted to milliseconds
    long stampMs = timeStamp * 1000;

    // create a date
    Date d = new Date(stampMs);

    // define a formatter
    DateFormat df = new SimpleDateFormat(dateFormat);

    // return formated date as string
    return df.format(d);
  }

  @Override
  public boolean isValidTransactionTimeStamp(long unixTimeStamp) {

    // the date when we start this project 01/04/2017
    long minTimeStamp = 1459468801L;
    // check if provided time stamp is in between accepted limits
    return unixTimeStamp >= minTimeStamp && unixTimeStamp <= this.getTimeStamp();
  }

  @Override
  public String generateHashId(String string) {

    long h = 929511134347L; // prime

    int len = string.length();

    for (int i = 0; i < len; i++) {
      h = 31 * h + string.charAt(i);
    }
    return String.valueOf(h);
  }

  @Override
  public Long stringDateToTimeStamp(String dateAsString, String dateFormat) {

    return stringDateToTimeStamp(dateAsString, dateFormat, ZoneId.systemDefault());
  }

  @Override
  public Long stringDateToTimeStamp(String dateAsString, String dateFormat, ZoneId originZoneId) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
    try {
      return ZonedDateTime.of(LocalDateTime.parse(dateAsString, formatter), originZoneId)
          .toEpochSecond();
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  @Override
  public String stringDateToUTC(String dateAsString, String dateFormat, ZoneId originZoneId) {

    ZoneId ZONE_ID_UTC = ZoneId.of("UTC");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
    try {
      ZonedDateTime zonedDateTimeOrigin =
          ZonedDateTime.of(LocalDateTime.parse(dateAsString, formatter), originZoneId);
      ZonedDateTime zonedDateTimeUTC = zonedDateTimeOrigin.withZoneSameInstant(ZONE_ID_UTC);
      return zonedDateTimeUTC.format(formatter);

    } catch (DateTimeParseException e) {
      return dateAsString;
    }
  }

  @Override
  public String nullToString(Object object) {

    if (object == null) return "";

    return object.toString();
  }

  @Override
  public String prettyPrintObject(Object object) {

    try {
      if (!object.getClass().getName().equals("JsonNode")) {
        object = Json.toJson(object);
      }
      ObjectMapper mapper = new ObjectMapper();
      Object json = mapper.readValue(object.toString(), Object.class);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (Exception e) {
      return "Sorry, pretty print didn't work";
    }
  }

  @Override
  public void setNowTimestamp(long timestamp) {

    nowTimestamp = timestamp;
  }
}
