package cern.modesti.notification;

import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public enum NotificationType {

  APPROVAL_STARTED          ("MODESTI request approval notification",   "approval-started",           "creator"),
  APPROVAL_COMPLETED        ("MODESTI request approval notification",   "approval-completed",         "creator"),

  ADDRESSING_STARTED        ("MODESTI request addressing notification", "addressing-started",         "creator"),
  ADDRESSING_COMPLETED      ("MODESTI request addressing notification", "addressing-completed",       "creator"),

  CABLING_STARTED           ("MODESTI request cabling notification",    "cabling-started",            "creator"),
  CABLING_COMPLETED         ("MODESTI request cabling notification",    "cabling-completed",          "creator"),

  TESTING_STARTED           ("MODESTI request testing notification",    "testing-started",            "creator"),
  TESTING_COMPLETED         ("MODESTI request testing notification",    "testing-completed",          "creator"),

  NEW_REQUEST_FOR_APPROVAL  ("New MODESTI request awaiting approval",   "new-request-for-approval",   "modesti.role.approvers"),
  NEW_REQUEST_FOR_ADDRESSING("New MODESTI request awaiting addressing", "new-request-for-addressing", "modesti.role.cablers"),
  NEW_REQUEST_FOR_CABLING   ("New MODESTI request awaiting cabling",    "new-request-for-cabling",    "modesti.role.cablers"),
  NEW_REQUEST_FOR_TESTING   ("New MODESTI request awaiting testing",    "new-request-for-testing",    "modesti.role.approvers"),

  CONFIGURATION_FAILURE     ("MODESTI request configuration failure",   "configuration-failed",       "modesti.role.administrators");

  private String subject;

  private String template;

  private List<String> recipients;

  /**
   * @param subject
   * @param template
   * @param recipients list of recipients
   */
  NotificationType(String subject, String template, String... recipients) {
    this.recipients = Arrays.asList(recipients);
    this.subject = subject;
    this.template = template;
  }

  public List<String> getRecipients() {
    return recipients;
  }

  public String getSubject() {
    return subject;
  }

  public String getTemplate() {
    return template;
  }
}
