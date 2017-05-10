package cern.modesti.request.history;

import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.TestUtil;
import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Changing a property multiple times stores only the original value and the latest value
 *
 * @author Justin Lewis Salmon
 */
public class ChangeVisitorTest {

  private Request original;
  private Request modified;

  @Before
  public void setUp() {
    original = TestUtil.getDummyRequest();
    original.getPoints().get(0).setLineNo(1L);
    original.getPoints().get(1).setLineNo(2L);
    modified = SerializationUtils.clone(original);
  }

  @Test
  public void addStringProperty() {
    modified.getPoints().get(0).addProperty("string", "test");
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.ADDED, "/points[1]/properties{string}", null, "test");
  }

  @Test
  public void updateStringProperty() {
    original.getPoints().get(0).addProperty("string", "test");
    modified.getPoints().get(0).addProperty("string", "test2");
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{string}", "test", "test2");
  }

  @Test
  public void addIntegerProperty() {
    modified.getPoints().get(0).addProperty("integer", 0);
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.ADDED, "/points[1]/properties{integer}", null, 0);
  }

  @Test
  public void updateIntegerProperty() {
    original.getPoints().get(0).addProperty("integer", 0);
    modified.getPoints().get(0).addProperty("integer", 1);
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{integer}", 0, 1);
  }

  @Test
  public void addObjectProperty() {
    Thing thing = new Thing(1, "thing 1");
    modified.getPoints().get(0).addProperty("object", thing);
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.ADDED, "/points[1]/properties{object}", null, thing);
  }

  @Test
  public void updateObjectProperty() {
    Thing thing1 = new Thing(1, "thing 1");
    Thing thing2 = new Thing(2, "thing 2");
    original.getPoints().get(0).addProperty("object", thing1);
    modified.getPoints().get(0).addProperty("object", thing2);
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{object}", thing1, thing2);
  }

  @Test
  public void convertStringToInteger() {
    original.getPoints().get(0).addProperty("integer", "0");
    modified.getPoints().get(0).addProperty("integer", 0);
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{integer}", "0", 0);
  }

  @Test
  public void nullToEmptyStringIsNotRecorded() {
    original.getPoints().get(0).addProperty("string", null);
    modified.getPoints().get(0).addProperty("string", "");
    ChangeEvent event = RequestDiffer.diff(modified, original, "id");
    assertTrue(event.getChanges().size() == 0);
  }

  @Test
  public void compareCorrespondingEntriesIfRowIsRemoved(){
    original.getPoints().get(0).addProperty("pointId", "1");
    original.getPoints().get(1).addProperty("pointId", "2");
    modified.getPoints().get(0).addProperty("pointId", "1");
    modified.getPoints().get(1).addProperty("pointId", "2");
    List<Point> points = modified.getPoints();
    points.remove(0);
    modified.setPoints(points);
    modified.getPoints().get(0).setLineNo(1L);

    ChangeEvent event = RequestDiffer.diff(modified, original, "pointId");

    assertEquals("We expect no changes because the only difference is missing row.",0, event.getChanges().size());
  }

  private void assertChangeEquals(Change change, DiffNode.State state, String path, Object original, Object modified) {
    assertEquals(change.getState(), state.name());
    assertEquals(change.getPath(), path);
    assertEquals(change.getOriginal(), original);
    assertEquals(change.getModified(), modified);
  }

  @Data
  @AllArgsConstructor
  private class Thing {
    private Integer id;
    private String name;
  }
}
