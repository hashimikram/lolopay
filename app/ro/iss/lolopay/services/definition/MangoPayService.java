package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.MangoPayImplementation;

@ImplementedBy(MangoPayImplementation.class)
public interface MangoPayService extends ProviderService {}
