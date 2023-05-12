package ro.iss.lolopay.programs;

public class Test {

  public static void main(String[] args) {

    StringBuilder resourcesFolder = new StringBuilder("test/resources");

    String className = "app.ro.iss.lolopay.controllers.BankAccount.CreateCaTests";
    String methodName = "test001_CreateNaturalUser";

    String[] packages = className.replaceAll("app\\.ro\\.iss\\.lolopay\\.", "").split("\\.");
    for (String string : packages) {
      resourcesFolder.append("/").append(string);
    }
    resourcesFolder.append("/").append(methodName).append(".json");
    System.out.println(resourcesFolder.toString());
  }
}
