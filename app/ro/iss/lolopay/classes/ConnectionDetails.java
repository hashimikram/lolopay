package ro.iss.lolopay.classes;

public class ConnectionDetails {
  private String server1Address = "";

  private int server1Port = 0;

  private String server2Address = "";

  private int server2Port = 0;

  private String server3Address = "";

  private int server3Port = 0;

  private String databaseUsername = "";

  private String databaseUsernamePassword = "";

  private String databaseName = "";

  /** @return the server1Address */
  public String getServer1Address() {

    return server1Address;
  }

  /** @param server1Address the server1Address to set */
  public void setServer1Address(String server1Address) {

    this.server1Address = server1Address;
  }

  /** @return the server1Port */
  public int getServer1Port() {

    return server1Port;
  }

  /** @param server1Port the server1Port to set */
  public void setServer1Port(int server1Port) {

    this.server1Port = server1Port;
  }

  /** @return the server2Address */
  public String getServer2Address() {

    return server2Address;
  }

  /** @param server2Address the server2Address to set */
  public void setServer2Address(String server2Address) {

    this.server2Address = server2Address;
  }

  /** @return the server2Port */
  public int getServer2Port() {

    return server2Port;
  }

  /** @param server2Port the server2Port to set */
  public void setServer2Port(int server2Port) {

    this.server2Port = server2Port;
  }

  /** @return the server3Address */
  public String getServer3Address() {

    return server3Address;
  }

  /** @param server3Address the server3Address to set */
  public void setServer3Address(String server3Address) {

    this.server3Address = server3Address;
  }

  /** @return the server3Port */
  public int getServer3Port() {

    return server3Port;
  }

  /** @param server3Port the server3Port to set */
  public void setServer3Port(int server3Port) {

    this.server3Port = server3Port;
  }

  /** @return the databaseUsername */
  public String getDatabaseUsername() {

    return databaseUsername;
  }

  /** @param databaseUsername the databaseUsername to set */
  public void setDatabaseUsername(String databaseUsername) {

    this.databaseUsername = databaseUsername;
  }

  /** @return the databaseUsernamePassword */
  public String getDatabaseUsernamePassword() {

    return databaseUsernamePassword;
  }

  /** @param databaseUsernamePassword the databaseUsernamePassword to set */
  public void setDatabaseUsernamePassword(String databaseUsernamePassword) {

    this.databaseUsernamePassword = databaseUsernamePassword;
  }

  /** @return the databaseName */
  public String getDatabaseName() {

    return databaseName;
  }

  /** @param databaseName the databaseName to set */
  public void setDatabaseName(String databaseName) {

    this.databaseName = databaseName;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer();
    sb.append("server1Address:");
    sb.append(server1Address);
    sb.append(";server1Port:");
    sb.append(server1Port);
    sb.append(";server2Address:");
    sb.append(server2Address);
    sb.append(";server2Port:");
    sb.append(server2Port);
    sb.append(";server3Address:");
    sb.append(server3Address);
    sb.append(";server3Port:");
    sb.append(server3Port);

    sb.append(";databaseUsername:");
    sb.append(databaseUsername);
    sb.append(";databaseUsernamePassword:");
    sb.append(databaseUsernamePassword);
    sb.append(";databaseName:");
    sb.append(databaseName);

    return sb.toString();
  }
}
