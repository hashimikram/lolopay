package ro.iss.lolopay.requests;

public class RequestPFSTransactionHook {
  private String username;

  private String password;

  private String data;

  /** @return the username */
  public String getUsername() {

    return username;
  }

  /** @param username the username to set */
  public void setUsername(String username) {

    this.username = username;
  }

  /** @return the password */
  public String getPassword() {

    return password;
  }

  /** @param password the password to set */
  public void setPassword(String password) {

    this.password = password;
  }

  /** @return the data */
  public String getData() {

    return data;
  }

  /** @param data the data to set */
  public void setData(String data) {

    this.data = data;
  }
}
