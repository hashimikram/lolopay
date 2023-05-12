package ro.iss.lolopay.responses;

import ro.iss.lolopay.classes.FxQuote;

public class ResponseFxQuote extends RestResponseBody {
  FxQuote fxQuote;

  /** @return the fxQuote */
  public FxQuote getFxQuote() {

    return fxQuote;
  }

  /** @param fxQuote the fxQuote to set */
  public void setFxQuote(FxQuote fxQuote) {

    this.fxQuote = fxQuote;
  }
}
