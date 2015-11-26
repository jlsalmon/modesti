package cern.modesti.request.history;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class RequestHistoryChange {

  private final DateTime changeDate;

  private List<RequestHistoryChangeItem> items = new ArrayList<>();
}
