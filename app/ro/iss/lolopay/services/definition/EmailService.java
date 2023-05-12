package ro.iss.lolopay.services.definition;

import com.google.inject.ImplementedBy;
import ro.iss.lolopay.services.implementation.EmailImplementation;

@ImplementedBy(EmailImplementation.class)
public interface EmailService {
  public void email(
      String fromEmail,
      String fromName,
      String toEmail,
      String toName,
      String subject,
      String emailContent);
}
