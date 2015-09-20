package cern.modesti.request;

import cern.modesti.user.User;

import java.util.Date;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Comment {

  private String text;

  private User user;

  private Date timestamp;

  public Comment() {
  }

  public Comment(String text, User user, Date timestamp) {
    this.text = text;
    this.user = user;
    this.timestamp = timestamp;
  }

  public String getText() {
    return text;
  }

  public User getUser() {
    return user;
  }

  public Date getTimestamp() {
    return timestamp;
  }
}
