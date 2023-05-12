package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.GeneralCallback;

@Entity(value = "processedCallbacks", noClassnameStored = true)
public class ProcessedCallback extends GeneralCallback {}
