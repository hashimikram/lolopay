package ro.iss.lolopay.models.classes;

public enum DisputeStatus {
  CREATED,
  PENDING_CLIENT_ACTION,
  SUBMITTED,
  PENDING_BANK_ACTION,
  REOPENED_PENDING_CLIENT_ACTION,
  CLOSED
}
