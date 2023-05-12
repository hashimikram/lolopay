package ro.iss.lolopay.application;

import play.libs.typedmap.TypedKey;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.models.database.Application;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.main.Session;

/**
 * Defines attribute keys to store into request.attrs() Map
 *
 * @author cornel
 */
public class Attrs {
  public static final TypedKey<Session> SESSION_NAME =
      TypedKey.<Session>create(ApplicationConstants.CTX_SESSION_RECORD_NAME);

  public static final TypedKey<Account> ACCOUNT_NAME =
      TypedKey.<Account>create(ApplicationConstants.CTX_ACCOUNT_RECORD_NAME);

  public static final TypedKey<Application> APPLICATION_NAME =
      TypedKey.<Application>create(ApplicationConstants.CTX_APPLICATION_RECORD_NAME);

  public static final TypedKey<String> HEADER_CUSTOM_REQUESTID =
      TypedKey.<String>create(ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID);
}
