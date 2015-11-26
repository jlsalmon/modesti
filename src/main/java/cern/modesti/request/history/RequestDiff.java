package cern.modesti.request.history;

import cern.modesti.user.User;
import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 */
@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDiff {

  @Id
  private String id;

  private DateTime modificationDate;

  private User user;

  private DiffNode diff;
}
