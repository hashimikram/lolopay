package ro.iss.lolopay.classes;

public class TokenSet {
  private String autheticationToken;

  private long autheticationExpiresAt;

  private String refreshToken;

  private long refreshExpiresAt;

  /** @return the autheticationToken */
  public String getAutheticationToken() {

    return autheticationToken;
  }

  /** @param autheticationToken the autheticationToken to set */
  public void setAutheticationToken(String autheticationToken) {

    this.autheticationToken = autheticationToken;
  }

  /** @return the autheticationExpiresAt */
  public long getAutheticationExpiresAt() {

    return autheticationExpiresAt;
  }

  /** @param autheticationExpiresAt the autheticationExpiresAt to set */
  public void setAutheticationExpiresAt(long autheticationExpiresAt) {

    this.autheticationExpiresAt = autheticationExpiresAt;
  }

  /** @return the refreshToken */
  public String getRefreshToken() {

    return refreshToken;
  }

  /** @param refreshToken the refreshToken to set */
  public void setRefreshToken(String refreshToken) {

    this.refreshToken = refreshToken;
  }

  /** @return the refreshExpiresAt */
  public long getRefreshExpiresAt() {

    return refreshExpiresAt;
  }

  /** @param refreshExpiresAt the refreshExpiresAt to set */
  public void setRefreshExpiresAt(long refreshExpiresAt) {

    this.refreshExpiresAt = refreshExpiresAt;
  }
}
