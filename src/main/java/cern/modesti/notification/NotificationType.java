package cern.modesti.notification;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public enum NotificationType {
  APPROVAL_COMPLETED   ("MODESTI request approval notification",   "approval"),
  ADDRESSING_COMPLETED ("MODESTI request addressing notification", "addressing"),
  CABLING_COMPLETED    ("MODESTI request cabling notification",    "cabling"),
  TESTING_COMPLETED    ("MODESTI request testing notification",    "testing");

  private String subject;

  private String template;

  NotificationType(String subject, String template) {
    this.subject = subject;
    this.template = template;
  }

  public String getSubject() {
    return subject;
  }

  public String getTemplate() {
    return template;
  }
}
