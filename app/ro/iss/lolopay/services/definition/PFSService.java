package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.PFSImplementation;

@ImplementedBy(PFSImplementation.class)
public interface PFSService extends ProviderService {}
