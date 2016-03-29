package cern.modesti.request.history;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class ChangeEvent {

  private final DateTime changeDate;

  private List<Change> changes = new ArrayList<>();
}
