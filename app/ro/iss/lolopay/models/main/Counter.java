package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.TableCollection;

@Entity(value = "counters", noClassnameStored = true)
public class Counter extends TableCollection {
  private String tableName;

  private int counter;

  public String getTableName() {

    return tableName;
  }

  public void setTableName(String tableName) {

    this.tableName = tableName;
  }

  public int getCounter() {

    return counter;
  }

  public void setCounter(int counter) {

    this.counter = counter;
  }
}
