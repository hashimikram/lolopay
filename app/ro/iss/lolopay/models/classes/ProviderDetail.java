package ro.iss.lolopay.models.classes;

public class ProviderDetail {
  /** Financial provider supporting the operation */
  private FinancialProvider financialProvider;

  /** Operation supported by the specified financial provider */
  private ProviderOperation providerOperation;

  /** @return the financialProvider */
  public FinancialProvider getFinancialProvider() {

    return financialProvider;
  }

  /** @param financialProvider the financialProvider to set */
  public void setFinancialProvider(FinancialProvider financialProvider) {

    this.financialProvider = financialProvider;
  }

  /** @return the providerOperation */
  public ProviderOperation getProviderOperation() {

    return providerOperation;
  }

  /** @param providerOperation the providerOperation to set */
  public void setProviderOperation(ProviderOperation providerOperation) {

    this.providerOperation = providerOperation;
  }
}
