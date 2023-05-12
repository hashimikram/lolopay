package ro.iss.lolopay.models.main;

import org.mongodb.morphia.annotations.Entity;
import ro.iss.lolopay.models.classes.GeneralCallback;

@Entity(value = "callbacks", noClassnameStored = true)
public class Callback extends GeneralCallback {}
