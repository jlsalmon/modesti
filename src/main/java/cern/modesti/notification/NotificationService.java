package cern.modesti.notification;

import cern.modesti.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class NotificationService {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private SpringTemplateEngine templateEngine;

  @Autowired
  private Environment env;

  /**
   *
   * @param request
   * @param type
   */
  public void sendNotification(Request request, NotificationType type) {

    List<String> to = new ArrayList<>();

    // Build the list of recipient addresses based on the notification type
    for (String recipient : type.getRecipients()) {
      if (recipient.equals("creator")) {
        to.add(request.getCreator().getEmail());
      }

      else {
        String[] addresses = env.getRequiredProperty(recipient, String[].class);
        for (String address : addresses) {
          to.add(address + "@cern.ch");
        }
      }
    }

    String from = env.getRequiredProperty("spring.mail.from");
    String subject = type.getSubject();
    String template = type.getTemplate();

    // Prepare the evaluation context
    final Context ctx = new Context(Locale.UK);
    ctx.setVariable("request", request);
    ctx.setVariable("url", env.getRequiredProperty("modesti.base") + "/requests/" + request.getRequestId());

    // Prepare message using a Spring helper
    final MimeMessage mimeMessage = mailSender.createMimeMessage();
    final MimeMessageHelper message;

    try {
      message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      message.setSubject(subject);

      message.setTo(to.toArray(new String[to.size()]));
      message.setFrom(from);

      // Create the HTML body using Thymeleaf
      final String htmlContent = templateEngine.process(template, ctx);
      message.setText(htmlContent, true);

    } catch (MessagingException e) {
      LOG.error("Error sending email notification", e);
    }

    // Send mail
    mailSender.send(mimeMessage);
  }
}
