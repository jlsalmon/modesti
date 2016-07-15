package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.test.TestUtil;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    modified = SerializationUtils.clone(original);
  }

  @Test
  public void addStringProperty() {
    modified.getPoints().get(0).addProperty("string", "test");
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.ADDED, "/points[1]/properties{string}", null, "test");
  }

  @Test
  public void updateStringProperty() {
    original.getPoints().get(0).addProperty("string", "test");
    modified.getPoints().get(0).addProperty("string", "test2");
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{string}", "test", "test2");
  }

  @Test
  public void addIntegerProperty() {
    modified.getPoints().get(0).addProperty("integer", 0);
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.ADDED, "/points[1]/properties{integer}", null, 0);
  }

  @Test
  public void updateIntegerProperty() {
    original.getPoints().get(0).addProperty("integer", 0);
    modified.getPoints().get(0).addProperty("integer", 1);
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{integer}", 0, 1);
  }

  @Test
  public void addObjectProperty() {
    Thing thing = new Thing(1, "thing 1");
    modified.getPoints().get(0).addProperty("object", thing);
    ChangeEvent event = diff(modified, original);
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
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{object}", thing1, thing2);
  }

  @Test
  public void convertStringToInteger() {
    original.getPoints().get(0).addProperty("integer", "0");
    modified.getPoints().get(0).addProperty("integer", 0);
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 1);

    Change change = event.getChanges().get(0);
    assertChangeEquals(change, DiffNode.State.CHANGED, "/points[1]/properties{integer}", "0", 0);
  }

  @Test
  public void nullToEmptyStringIsNotRecorded() {
    original.getPoints().get(0).addProperty("string", null);
    modified.getPoints().get(0).addProperty("string", "");
    ChangeEvent event = diff(modified, original);
    assertTrue(event.getChanges().size() == 0);
  }

  private ChangeEvent diff(Request modified, Request original) {
    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    DiffNode root = ObjectDifferBuilder.buildDefault().compare(modified, original);
    root.visit(new ChangeVisitor(event, modified, original));
    return event;
  }

  private void assertChangeEquals(Change change, DiffNode.State state, String path, Object original, Object modified) {
    assertEquals(change.getState(), state);
    assertEquals(change.getPath(), path);
    assertEquals(change.getOriginal(), original);
    assertEquals(change.getModified(), modified);
  }

  @Data
  @AllArgsConstructor
  class Thing {
    private Integer id;
    private String name;
  }
}
