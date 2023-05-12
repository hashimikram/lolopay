package ro.iss.lolopay.enums;

public class ApplicationConstants {
  public static final String HTTP_HEADER_CUSTOM_REQUESTID = "RequestId";

  public static final String HTTP_REFRESH_REQUEST_PATH = "/refresh";

  public static final String CTX_SESSION_RECORD_NAME = "sess";

  public static final String CTX_ACCOUNT_RECORD_NAME = "acc";

  public static final String CTX_APPLICATION_RECORD_NAME = "app";

  public static final String SEARCH_WEIGHT_HIGH = "H";

  public static final String SEARCH_WEIGHT_LOW = "L";

  public static final String SEARCH_MATCH_FULL = "F";

  public static final String SEARCH_MATCH_PARTIAL = "P";

  public static final String CLAIMS_ROLE_AUTHENTICATION = "auth";

  public static final String CLAIMS_ROLE_REFRESH = "rerfesh";

  public static final String REGEX_SEARCH_SEPARATOR = "\\s+";

  public static final String REGEX_VALIDATE_EMBOSSNAME = "[a-zA-Z0-9 `'/@_+#=?\\Q()-.\\E]{1,30}";

  public static final String REGEX_VALIDATE_UUID =
      "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

  public static final String REGEX_VALIDATE_UNIXTIME =
      "\\d{4}[-]?\\d{1,2}[-]?\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}[.]?\\d{1,6}$";

  public static final String REGEX_VALIDATE_MONGOID = "[0-9a-fA-F]{24}$";

  public static final String REGEX_VALIDATE_MONGO_AND_INT_ID = "[0-9a-fA-F]{1,24}$";

  public static final String REGEX_VALIDATE_DIGITS = "\\d+";

  public static final String REGEX_VALIDATE_DIGITS_LETTERS_SPACE = "[0-9a-zA-Z\\s]+";

  public static final String REGEX_VALIDATE_ALPHANUMERIC = "[0-9a-zA-Z]+";

  public static final String REGEX_VALIDATE_PFSHOOK_TRANSACTION_TYPE = "[CD]{1}$";

  public static final String REGEX_VALIDATE_BENEFICIARY_NAME = "[a-zA-Z ]{0,20}";

  public static final String REGEX_VALIDATE_REPLACE_CARD_REASON = "[\\w\\s., \\Q()\\E]{1,250}";
}
