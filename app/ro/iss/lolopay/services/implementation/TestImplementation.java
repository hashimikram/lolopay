package ro.iss.lolopay.services.implementation;

import ro.iss.lolopay.services.definition.TestService;

public class TestImplementation implements TestService {

  @Override
  public String doSomething() {

    return "The implemented do something";
  }
}
