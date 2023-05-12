package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.AuthenticationResponse;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.models.classes.ApplicationStamp;
import ro.iss.lolopay.models.classes.ApplicationStatus;
import ro.iss.lolopay.models.main.Session;
import ro.iss.lolopay.services.implementation.SecurityImplementation;

@ImplementedBy(SecurityImplementation.class)
public interface SecurityService {
  public AuthenticationResponse createAccount(
      String requestId,
      String accountName,
      String accountEmail,
      String applicationName,
      String applicationUsername,
      String applicationEmail,
      String applicationPassword,
      ApplicationStatus applicationStatus,
      ApplicationStamp whoIsOperating);

  public AuthenticationResponse authenticateApplication(
      String requestId, String accountNumber, String applicationId, String password);

  public TokenSet registerSession(
      String requestId, AuthenticationResponse authenticationResponse, String remoteIP);

  public void unregisterSession(String requestId, Session session);
}
