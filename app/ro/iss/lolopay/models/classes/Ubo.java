package ro.iss.lolopay.models.classes;

import javax.validation.Valid;

public class Ubo {

  /** Id of the related record in financial provider system */
  private String providerId;

  private long createdAt;

  private String firstName;

  private String lastName;

  private Address address;

  private CountryISO nationality;

  private Long birthday;

  @Valid private Birthplace birthplace;

  /** @return the firstName */
  public String getFirstName() {

    return firstName;
  }

  /** @return the lastName */
  public String getLastName() {

    return lastName;
  }

  /** @param firstName the firstName to set */
  public void setFirstName(String firstName) {

    this.firstName = firstName;
  }

  /** @param lastName the lastName to set */
  public void setLastName(String lastName) {

    this.lastName = lastName;
  }

  /** @return the address */
  public Address getAddress() {

    return address;
  }

  /** @return the birthday */
  public Long getBirthday() {

    return birthday;
  }

  /** @return the nationality */
  public CountryISO getNationality() {

    return nationality;
  }

  /** @return the birthplace */
  public Birthplace getBirthplace() {

    return birthplace;
  }

  /** @param address the address to set */
  public void setAddress(Address address) {

    this.address = address;
  }

  /** @param birthday the birthday to set */
  public void setBirthday(Long birthday) {

    this.birthday = birthday;
  }

  /** @param nationality the nationality to set */
  public void setNationality(CountryISO nationality) {

    this.nationality = nationality;
  }

  /** @param birthplace the birthplace to set */
  public void setBirthplace(Birthplace birthplace) {

    this.birthplace = birthplace;
  }

  public String getProviderId() {

    return providerId;
  }

  public void setProviderId(String providerId) {

    this.providerId = providerId;
  }

  /** @return createdAt */
  public long getCreatedAt() {

    return createdAt;
  }

  /** @param createdAt the createdAt to set */
  public void setCreatedAt(long createdAt) {

    this.createdAt = createdAt;
  }
}
