package cern.modesti.request.history;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.modesti.point.Point;
import cern.modesti.point.PointImpl;
import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestType;


/**
 * @author Ivan Prieto Barreiro
 */
public class RequestDifferTest {

  private static final String ID_PROPERTY = "id";
  private static final int DEFAULT_NUM_POINTS = 50;
  private static final int DEFAULT_NUM_PROPERTIES = 15;
  private Request original;
  private Request modified;

  @Before
  public void initialize() {
    original = createRequest(DEFAULT_NUM_POINTS, DEFAULT_NUM_PROPERTIES);
    modified = createRequest(DEFAULT_NUM_POINTS, DEFAULT_NUM_PROPERTIES);
  }

  @Test
  public void singleModificationDiffTest() {
    modified.getPoints().get(10).getProperties().put("Property_11", "Test_11");

    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(1, changes.size());

    Change change = changes.get(0);
    checkLineChanges(change, 11, 11);
  }

  /**
   * Verifies the changes in one line
   * 
   * @param change
   *          The change registered by the RequestDiffer
   * @param line
   *          The line number where the property was modified
   * @param property
   *          The number of the property that was modified
   */
  private void checkLineChanges(Change change, int line, int property) {
    assertEquals(String.format("/points[%d]/properties{Property_%d}", line, property), change.getPath());
    assertEquals(Long.valueOf(line), change.getLineNo());
    assertEquals(String.format("Property_%d", property), change.getProperty());
    assertEquals("CHANGED", change.getState());
    assertEquals(String.format("Test_%d", property), change.getModified());
    assertEquals(String.format("Value_%d", property), change.getOriginal());
  }

  @Test
  public void multipleModificationsDiffTest() {
    modified.getPoints().get(10).getProperties().put("Property_11", "Test_11");
    modified.getPoints().get(11).getProperties().put("Property_12", "Test_12");

    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(2, changes.size());
    checkLineChanges(changes.get(0), 11, 11);
    checkLineChanges(changes.get(1), 12, 12);
  }

  @Test
  public void multipleModificationsInOneRowDiffTest() {
    modified.getPoints().get(10).getProperties().put("Property_11", "Test_11");
    modified.getPoints().get(10).getProperties().put("Property_12", "Test_12");

    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(2, changes.size());
    checkLineChanges(changes.get(0), 11, 11);
    checkLineChanges(changes.get(1), 11, 12);
  }

  @Test
  public void deletedPointDiffTest() {
    modified.getPoints().remove(0);
    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(0, changes.size());
  }

  @Test
  public void addedPointDiffTest() {
    modified.getPoints().add(createPoint(DEFAULT_NUM_POINTS + 1, DEFAULT_NUM_PROPERTIES));
    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(0, changes.size());
  }

  private Request createRequest(int numPoints, int numProperties) {
    Request request = new RequestImpl();
    request.setType(RequestType.UPDATE);
    request.setDescription("Test description");
    request.setDomain("Unit Test");

    ArrayList<Point> points = new ArrayList<>();

    for (int i = 1; i <= numPoints; i++) {
      Point p = createPoint(i, numProperties);
      points.add(p);
    }

    request.setPoints(points);

    return request;
  }

  private Point createPoint(int id, int numProperties) {
    Point p = new PointImpl();
    p.setLineNo(Integer.valueOf(id).longValue());
    Map<String, Object> props = new HashMap<>();
    props.put("id", String.valueOf(id));
    for (int j = 0; j < numProperties; j++) {
      props.put("Property_" + j, "Value_" + j);
    }
    p.setProperties(props);
    return p;
  }

}
