package classes;

import static play.mvc.Results.status;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;
import static shared.ResourcePaths.RESOURCE_PATH_CONTROLLERS;
import static shared.ResourcePaths.RESOURCE_PATH_SHARED;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MimeTypes;
import play.mvc.Result;
import play.test.Helpers;
import ro.iss.lolopay.enums.ApplicationConstants;
import shared.Providers;

public class HttpHelper {

  public String controllerName;
  public String testClassName;
  public String controllersResourcesDirectory;

  /** Constructor */
  public HttpHelper(String controllerName, String testClassName) {
    super();
    this.controllerName = controllerName;
    this.testClassName = testClassName;
    this.controllersResourcesDirectory =
        String.join(
            File.separator, RESOURCE_PATH_CONTROLLERS.getPath(), controllerName, testClassName);
  }

  public JsonNode executeRequest(
      String type,
      Application playApplication,
      String autheticationToken,
      String uri,
      Map<String, String> parameters,
      String requestId,
      String contentType)
      throws Exception {

    // build request
    Http.RequestBuilder request = new Http.RequestBuilder();
    request.method(type);
    request.uri(uri);
    if (autheticationToken != null)
      request.header(Http.HeaderNames.AUTHORIZATION, autheticationToken);
    request.header(ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID, requestId);
    request.header(Http.HeaderNames.USER_AGENT, "Testing Platform");

    if (contentType.equals(MimeTypes.JSON)) {
      if (type.equals(Helpers.POST) || type.equals(Helpers.PUT)) {

        // get input data
        JsonNode jsonBody = getTestInputDate(parameters);

        if (jsonBody == null) {
          throw new Exception("You have to create json file with input data");
        }
        request.bodyJson(jsonBody);
        System.out.println("TEST REQUEST:body: " + request.body().asJson().toString());
      }
    } else {
      request.bodyForm(getTestInputDateAsMap());
    }

    System.out.println("TEST REQUEST:uri: " + request.uri());
    System.out.println("TEST REQUEST:method: " + request.method());
    System.out.println(
        "TEST REQUEST:<" + Http.HeaderNames.AUTHORIZATION + ">: " + autheticationToken);
    System.out.println(
        "TEST REQUEST:<" + ApplicationConstants.HTTP_HEADER_CUSTOM_REQUESTID + ">: " + requestId);
    System.out.println("TEST REQUEST:<" + Http.HeaderNames.USER_AGENT + ">: " + "Testing Platform");

    // make request
    Result result = route(playApplication, request);

    String contentAsString = contentAsString(result);

    System.out.println("TEST " + type + " <" + uri + "> response: " + contentAsString);

    // extract response
    if (!contentAsString.equals("")) {
      try {
        return Json.parse(contentAsString);
      } catch (Exception e) {
        return Json.toJson(contentAsString);
      }
    }
    return Json.parse("{}");
  }

  /**
   * Retrieve the file with JSON data and create a jsonNode for testing
   *
   * @return
   */
  private JsonNode getTestInputDate(Map<String, String> parameters) {

    // read file from disk
    File file = new File(getTestInputFileName());
    String fileContent = "";

    try {
      fileContent = FileUtils.readFileToString(file, Charset.defaultCharset());
    } catch (IOException e1) {
      e1.printStackTrace();
      return null;
    }

    // process parameters if we have them
    if (parameters != null) {

      // get map iterator
      Iterator<Entry<String, String>> it = parameters.entrySet().iterator();

      // iterate map values
      while (it.hasNext()) {
        // get map pair
        Map.Entry<String, String> pair = it.next();

        fileContent = fileContent.replaceAll("@" + pair.getKey() + "@", pair.getValue());

        it.remove(); // avoids a ConcurrentModificationException
      }
    }

    // fileContent.repl
    return Json.parse(fileContent);
  }

  /**
   * Retrieve the file with JSON data and create a map for testing
   *
   * @return
   */
  private Map<String, String> getTestInputDateAsMap() {

    // read file from disk
    File file = new File(getTestInputFileName());
    Map<String, String> mapInFile = new HashMap<String, String>();

    try {
      FileInputStream fis = new FileInputStream(file);

      Scanner sc = new Scanner(fis);

      // read data from file line by line:
      String currentLine;

      while (sc.hasNextLine()) {
        currentLine = sc.nextLine();

        // now tokenize the currentLine:
        StringTokenizer st = new StringTokenizer(currentLine, "=", false);

        // put tokens of currentLine in map
        mapInFile.put(st.nextToken(), st.nextToken());
      }
      fis.close();
      sc.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return mapInFile;
  }

  /**
   * Gets input file needed for body of request
   *
   * @return
   */
  private String getTestInputFileName() {

    StackTraceElement[] traces = Thread.currentThread().getStackTrace();
    String methodName = traces[4].getMethodName();
    return String.join(File.separator, this.controllersResourcesDirectory, methodName)
        .concat(".json");
  }

  /**
   * Gets mock provider response shared to all requests from shared resource folder
   *
   * @param testName
   * @param providerName
   * @param statusCode
   * @return
   */
  public static Result getProviderSharedResponse(
      String testName, Providers provider, int statusCode) {

    // construct file name
    String fileName = String.join("_", testName, provider.getName(), String.valueOf(statusCode));

    // construct path to file
    String filePath = String.join(File.separator, RESOURCE_PATH_SHARED.getPath(), fileName);

    // add extension
    filePath = filePath.concat(".json");

    try {
      FileInputStream jsonFileStream = new FileInputStream(filePath);

      return status(statusCode, Json.parse(jsonFileStream));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets mock provider response from file
   *
   * @param testNumber
   * @param testName
   * @param providerName
   * @param statusCode
   * @return
   */
  public Result getProviderResponse(String testName, Providers provider, int statusCode) {

    // construct file name
    String fileName = String.join("_", testName, provider.getName(), String.valueOf(statusCode));

    // construct path to file
    String filePath =
        this.controllersResourcesDirectory.concat(File.separator).concat(fileName).concat(".json");

    try {
      return status(statusCode, Json.parse(new FileInputStream(filePath)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets method name
   *
   * @return
   */
  public String getMethodName() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    return stackTrace[2].getMethodName();
  }
}
