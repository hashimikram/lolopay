package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.services.implementation.JWTImplementation;

@ImplementedBy(JWTImplementation.class)
public interface TokenService {
  public boolean isTokenValid(String token);

  public String getSessionIdFromToken(String token);

  public String getRoleFromToken(String token);

  public TokenSet generateTokenSet(
      String accountId, String applicationId, String sessionId, String ipAddress);

  public boolean isTokenSecure(
      String token,
      String validAccountId,
      String validApplicationId,
      String validSessionId,
      String actualIpAddress,
      String validForRole);
}
