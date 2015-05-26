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

    String to = request.getCreator().getEmail();
    String from = env.getRequiredProperty("spring.mail.from");
    String subject = type.getSubject();
    String template = type.getTemplate();

    // Need to know: involved people

    // TODO: create notification factory

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

      message.setTo(to);
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
