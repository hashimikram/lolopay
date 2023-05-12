package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.InternalProviderImplementation;

@ImplementedBy(InternalProviderImplementation.class)
public interface InternalProviderService extends ProviderService {}
