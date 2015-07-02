package cern.modesti.schema.category;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Constraint {
  String type;
  List<String> members;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }
}