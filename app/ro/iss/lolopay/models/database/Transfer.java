package ro.iss.lolopay.models.database;

import org.mongodb.morphia.annotations.Entity;

@Entity(value = "transactions", noClassnameStored = true)
public class Transfer extends Transaction {}
