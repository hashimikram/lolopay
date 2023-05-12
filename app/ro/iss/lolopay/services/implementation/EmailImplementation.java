package ro.iss.lolopay.services.implementation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import ro.iss.emailer.rabbit.QueueEmails;
import ro.iss.emailer.rabbit.QueueParameters;
import ro.iss.lolopay.services.definition.EmailService;

@Singleton
public class EmailImplementation implements EmailService {

  private QueueEmails queueEmails;

  @Inject private play.Environment environment;

  /** Log singleton creation moment */
  public EmailImplementation() {

    Logger.of(this.getClass()).debug("Singleton created");

    QueueParameters queueParameters = new QueueParameters();
    queueParameters.setHost(ConfigFactory.load().getString("email.host"));
    queueParameters.setPort(ConfigFactory.load().getInt("email.port"));
    queueParameters.setUsername(ConfigFactory.load().getString("email.username"));
    queueParameters.setPassword(ConfigFactory.load().getString("email.password"));
    queueParameters.setQueueName(ConfigFactory.load().getString("email.queueName"));
    queueParameters.setConnectionTimeOut(ConfigFactory.load().getInt("email.conTimeOut"));
    queueParameters.setSetHandshakeTimeout(ConfigFactory.load().getInt("email.hskTimeOut"));
    queueEmails = new QueueEmails(queueParameters);
  }

  @Override
  public void email(
      String fromEmail,
      String fromName,
      String toEmail,
      String toName,
      String subject,
      String emailContent) {

    String templateName = "SUPPORT_EMAIL";
    Map<String, String> templateVars = new HashMap<String, String>();
    templateVars.put("var", emailContent);

    // don't try to send email when unit testing is ran
    if (!environment.isTest()) {
      CompletableFuture.runAsync(
          () -> {
            try {
              queueEmails.email(
                  fromEmail, fromName, toEmail, toName, subject, templateName, templateVars, "en");
            } catch (IOException | TimeoutException e) {
              e.printStackTrace();
              Logger.of(this.getClass()).error("Email system does not work " + e.getMessage());
            }
          });
    }
  }
}
