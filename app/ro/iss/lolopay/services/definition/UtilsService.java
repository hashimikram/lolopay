package ro.iss.lolopay.services.definition;

import java.time.ZoneId;
import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.UtilsImplementation;

@ImplementedBy(UtilsImplementation.class)
public interface UtilsService {
  public String generateRandomString(int size);

  public long getCurrentTimeMiliseconds();

  public long getTimeStamp();

  public String generateDbName(String accountName);

  public String timeStampToDate(Long timeStamp, String dateFormat);

  public boolean isValidTransactionTimeStamp(long unixTimeStamp);

  public String generateHashId(String string);

  public Long stringDateToTimeStamp(String dateAsString, String dateFormat);

  public Long stringDateToTimeStamp(String dateAsString, String dateFormat, ZoneId originZoneId);

  public String stringDateToUTC(String dateAsString, String dateFormat, ZoneId originZoneId);

  public String nullToString(Object object);

  public String prettyPrintObject(Object object);

  public void setNowTimestamp(long timestamp);
}
