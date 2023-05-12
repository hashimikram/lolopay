package ro.iss.lolopay.models.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.models.database.Transfer;
import ro.iss.lolopay.models.main.Account;
import ro.iss.lolopay.models.services.implementation.TransferImplementation;

@ImplementedBy(TransferImplementation.class)
public interface TransferService {
  public Transfer getTransfer(String requestId, Account account, String transferId);

  public Transfer getTransferByProviderId(String requestId, Account account, String providerId);

  public void saveTransfer(String requestId, Account account, Transfer transfer);
}
