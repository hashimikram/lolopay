package ro.iss.lolopay.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import play.mvc.With;
import ro.iss.lolopay.actions.CustomValidJsonAction;

@With(CustomValidJsonAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomValidJson {}
