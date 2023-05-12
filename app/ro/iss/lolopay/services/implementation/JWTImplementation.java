package ro.iss.lolopay.services.implementation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import play.Logger;
import ro.iss.lolopay.classes.TokenSet;
import ro.iss.lolopay.enums.ApplicationConstants;
import ro.iss.lolopay.services.definition.TokenService;
import ro.iss.lolopay.services.definition.UtilsService;

@Singleton
public class JWTImplementation implements TokenService {
  private final String CLAIMS_KEY_IP_ADDRESS = "ip";

  private final String CLAIMS_KEY_ACCOUNT_ID = "acid";

  private final String CLAIMS_KEY_ROLE = "role";

  private final String AUDIENCE_API = "api";

  @Inject UtilsService utilsService;

  /** Log singleton creation moment */
  public JWTImplementation() {

    Logger.of(this.getClass()).debug("Singleton created");
  }

  /**
   * Validate token syntax and date intervals
   *
   * @param token
   * @return
   */
  @Override
  public boolean isTokenValid(String token) {

    // try to extract claims from token
    Claims tokenClaims = getClaimsFromToken(token);

    // if claims cam't be extracted it means token is invalid
    return (tokenClaims != null ? true : false);
  }

  /**
   * Get session id from the token
   *
   * @param token
   * @return
   */
  @Override
  public String getSessionIdFromToken(String token) {

    // try to extract claims from token
    Claims tokenClaims = getClaimsFromToken(token);

    // return session id
    return tokenClaims.getId();
  }

  /**
   * Get role from the token
   *
   * @param token
   * @return
   */
  @Override
  public String getRoleFromToken(String token) {

    // try to extract claims from token
    Claims tokenClaims = getClaimsFromToken(token);

    // return token role
    return tokenClaims.get(CLAIMS_KEY_ROLE).toString();
  }

  /**
   * Generate new authentication token based on user id, user generated session id and csrf token
   *
   * @param authenticatedUserId
   * @param serverSessionId
   * @param ipAddress
   * @param accountId
   * @return
   */
  @Override
  public TokenSet generateTokenSet(
      String accountId, String applicationId, String sessionId, String ipAddress) {

    long expireTimeMilisecAuth = generateExpirationTimeMiliSec();
    long expiryTimeForRefreshToken = generateExpirationDateRefresh(expireTimeMilisecAuth);
    long startTimeForRefreshToken = generateStartDateRefresh(expireTimeMilisecAuth);

    Date now = new Date();
    Date expiryDateAuth = new Date(expireTimeMilisecAuth);
    Date startDateRefresh = new Date(startTimeForRefreshToken);
    Date expiryDateRefresh = new Date(expiryTimeForRefreshToken);

    // token header
    Map<String, Object> header = new HashMap<>();
    header.put("cty", "JWT");
    header.put("typ", "JWT");

    // generate claims for authentication
    Claims claimsAuthetication = new DefaultClaims();
    claimsAuthetication.setId(sessionId);
    claimsAuthetication.setSubject(applicationId);
    claimsAuthetication.setIssuer(getIssuer());
    claimsAuthetication.setAudience(AUDIENCE_API);
    claimsAuthetication.setNotBefore(now);
    claimsAuthetication.setIssuedAt(now);
    claimsAuthetication.setExpiration(expiryDateAuth);

    claimsAuthetication.put(CLAIMS_KEY_ROLE, ApplicationConstants.CLAIMS_ROLE_AUTHENTICATION);
    claimsAuthetication.put(CLAIMS_KEY_IP_ADDRESS, ipAddress);
    claimsAuthetication.put(CLAIMS_KEY_ACCOUNT_ID, accountId);

    // generate authentication token
    String autToken =
        Jwts.builder()
            .setHeaderParams(header)
            .setClaims(claimsAuthetication)
            .signWith(SignatureAlgorithm.HS512, getSecret())
            .compact();

    // generate claims for refresh operation
    Claims claimsRefresh = new DefaultClaims();
    claimsRefresh.setId(sessionId);
    claimsRefresh.setSubject(applicationId);
    claimsRefresh.setIssuer(getIssuer());
    claimsRefresh.setAudience(AUDIENCE_API);
    claimsRefresh.setNotBefore(startDateRefresh);
    claimsRefresh.setIssuedAt(now);
    claimsRefresh.setExpiration(expiryDateRefresh);

    claimsRefresh.put(CLAIMS_KEY_ROLE, ApplicationConstants.CLAIMS_ROLE_REFRESH);
    claimsRefresh.put(CLAIMS_KEY_IP_ADDRESS, ipAddress);
    claimsRefresh.put(CLAIMS_KEY_ACCOUNT_ID, accountId);

    // generate refresh token
    String refreshToken =
        Jwts.builder()
            .setHeaderParams(header)
            .setClaims(claimsRefresh)
            .signWith(SignatureAlgorithm.HS512, getSecret())
            .compact();

    // build response token set
    TokenSet tokenSet = new TokenSet();
    tokenSet.setAutheticationToken(autToken);
    tokenSet.setAutheticationExpiresAt(expireTimeMilisecAuth / 1000L);
    tokenSet.setRefreshToken(refreshToken);
    tokenSet.setRefreshExpiresAt(expiryTimeForRefreshToken / 1000L);
    return tokenSet;
  }

  /**
   * Validate token
   *
   * @param token
   * @param validUserId
   * @param validSessionId
   * @param actualIpAddress
   * @param validAccountId
   * @param validForRole
   * @return
   */
  @Override
  public boolean isTokenSecure(
      String token,
      String validAccountId,
      String validApplicationId,
      String validSessionId,
      String actualIpAddress,
      String validForRole) {

    final Claims claims = getClaimsFromToken(token);

    if (claims == null) return false;

    // test server session id
    if (!claims.getId().equals(validSessionId)) return false;

    // test user identification
    if (!claims.getSubject().equals(validApplicationId)) return false;

    // test issuer
    if (!claims.getIssuer().equals(getIssuer())) return false;

    // test audience
    if (!claims.getAudience().equals(AUDIENCE_API)) return false;

    // test role for token
    if (!claims.get(CLAIMS_KEY_ROLE).toString().equals(validForRole)) return false;

    // test ip address
    if (!claims.get(CLAIMS_KEY_IP_ADDRESS).toString().equals(actualIpAddress)) return false;

    // test account id
    if (!claims.get(CLAIMS_KEY_ACCOUNT_ID).toString().equals(validAccountId)) return false;

    return true;
  }

  /** Generate expiration date as UNIX time stamp */
  private long generateExpirationTimeMiliSec() {

    if (ConfigFactory.load().hasPath("jwt.auth.token.expiration")) {
      // curent time stamp + number of minutes configured * number of second in a minute * number of
      // m seconds in a second
      // return utilsService.getCurrentTimeMiliseconds()
      return utilsService.getCurrentTimeMiliseconds()
          + ConfigFactory.load().getInt("jwt.auth.token.expiration") * 60 * 1000;
    } else {
      // 720 minute default (12 ore) * 60 * 1000 = 12 hours in m seconds
      return utilsService.getCurrentTimeMiliseconds() + 720 * 60 * 1000;
    }
  }

  /**
   * Generate expire date for token refresh authentication
   *
   * @return Date
   */
  private long generateExpirationDateRefresh(long expirationTimeMsAuth) {

    long expiryTimeForToken;

    if (ConfigFactory.load().hasPath("jwt.refresh.token.life.span")) {
      expiryTimeForToken =
          expirationTimeMsAuth
              + ConfigFactory.load().getInt("jwt.refresh.token.life.span") * 60 * 1000;
    } else {
      // 5 minutes * 60 seconds * 1000 ms = 5 minutes
      expiryTimeForToken = expirationTimeMsAuth + 5 * 60 * 1000;
    }

    return expiryTimeForToken;
  }

  /**
   * Generate expire date for token refresh authentication
   *
   * @return Date
   */
  private long generateStartDateRefresh(long expirationTimeMsAuth) {

    long startTimeForToken;

    if (ConfigFactory.load().hasPath("jwt.refresh.token.life.span")) {
      startTimeForToken =
          expirationTimeMsAuth
              - ConfigFactory.load().getInt("jwt.refresh.token.life.span") * 60 * 1000;
    } else {
      // 5 minutes * 60 seconds * 1000 ms = 5 minutes
      startTimeForToken = expirationTimeMsAuth - 5 * 60 * 1000;
    }

    return startTimeForToken;
  }

  /**
   * Retrieve application id - issuer of JWT
   *
   * @return
   */
  private String getIssuer() {

    if (ConfigFactory.load().hasPath("application.id")) {

      return ConfigFactory.load().getString("application.id");
    } else {
      // return no name application id
      return "499ddaad9df107bf7107a3e2c0064800";
    }
  }

  /**
   * Retrieve secret password
   *
   * @return
   */
  private String getSecret() {

    if (ConfigFactory.load().hasPath("jwt.secret")) {

      return ConfigFactory.load().getString("jwt.secret");
    } else {
      // 720 minute default (12 ore) * 60 * 1000 = 12 ore in miliseunde
      return "secret";
    }
  }

  /**
   * Get claims from token
   *
   * @param token Token to be parsed back to a Claims object
   * @return
   */
  private Claims getClaimsFromToken(String token) {

    try {
      return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody();
    } catch (Exception e) {
      Logger.of(this.getClass()).error("getClaimsFromToken error: " + e.getMessage());
      return null;
    }
  }
}
