package cern.modesti.request.point;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Point {

  private Long lineNo;

  private Boolean dirty = true;

  private Boolean selected = false;

  private List<Error> errors = new ArrayList<>();

  private Map<String, Object> properties = new HashMap<>();

  public Point(Long lineNo) {
    this.lineNo = lineNo;
  }

  /**
   * TODO remove these domain-specific properties
   */
//  private Boolean valid;
//
//  private Approval approval = new Approval();
//
//  private Addressing addressing = new Addressing();
//
//  private Cabling cabling = new Cabling();
//
//  private Boolean configured;
//
//  private Testing testing = new Testing();
}
