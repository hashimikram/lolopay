package ro.iss.lolopay.programs.classes;

import com.opencsv.bean.CsvBindByName;

public class MangoWallet {
  @CsvBindByName private String Id;

  @CsvBindByName private String Tag;

  @CsvBindByName private Long CreationDate;

  @CsvBindByName(column = "CreationDate:ISO")
  private String CreationDateISO;

  @CsvBindByName private String Owners;

  @CsvBindByName private int BalanceAmount;

  @CsvBindByName private String BalanceCurrency;

  @CsvBindByName private String Description;

  @CsvBindByName private String FundsType;

  public String getId() {

    return Id;
  }

  public void setId(String id) {

    Id = id;
  }

  public String getTag() {

    return Tag;
  }

  public void setTag(String tag) {

    Tag = tag;
  }

  public Long getCreationDate() {

    return CreationDate;
  }

  public void setCreationDate(Long creationDate) {

    CreationDate = creationDate;
  }

  public String getCreationDateISO() {

    return CreationDateISO;
  }

  public void setCreationDateISO(String creationDateISO) {

    CreationDateISO = creationDateISO;
  }

  public String getOwners() {

    return Owners;
  }

  public void setOwners(String owners) {

    Owners = owners;
  }

  public int getBalanceAmount() {

    return BalanceAmount;
  }

  public void setBalanceAmount(int balanceAmount) {

    BalanceAmount = balanceAmount;
  }

  public String getBalanceCurrency() {

    return BalanceCurrency;
  }

  public void setBalanceCurrency(String balanceCurrency) {

    BalanceCurrency = balanceCurrency;
  }

  public String getDescription() {

    return Description;
  }

  public void setDescription(String description) {

    Description = description;
  }

  public String getFundsType() {

    return FundsType;
  }

  public void setFundsType(String fundsType) {

    FundsType = fundsType;
  }
}
