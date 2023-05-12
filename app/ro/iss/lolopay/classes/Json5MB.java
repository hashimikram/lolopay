package ro.iss.lolopay.classes;

import javax.inject.Inject;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;

public class Json5MB extends BodyParser.Json {
  @Inject
  public Json5MB(HttpErrorHandler errorHandler) {

    // 10 * 1024 = 10K
    super(5 * 1024 * 1024, errorHandler);
  }
}
