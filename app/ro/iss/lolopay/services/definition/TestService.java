package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.TestImplementation;

@ImplementedBy(TestImplementation.class)
public interface TestService {
  public String doSomething();
}
