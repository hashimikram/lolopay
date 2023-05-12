package shared;

public enum Providers {
  MANGO("mango"),
  PFS("pfs");

  private String name;

  Providers(String name) {
    this.name = name;
  }

  /** @return the uri */
  public String getName() {
    return this.name;
  }
}
