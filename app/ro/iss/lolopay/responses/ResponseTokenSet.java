package ro.iss.lolopay.responses;

import ro.iss.lolopay.classes.TokenSet;

public class ResponseTokenSet extends RestResponseBody {
  private TokenSet tokenSet;

  /** @return the tokenSet */
  public TokenSet getTokenSet() {

    return tokenSet;
  }

  /** @param tokenSet the tokenSet to set */
  public void setTokenSet(TokenSet tokenSet) {

    this.tokenSet = tokenSet;
  }
}
