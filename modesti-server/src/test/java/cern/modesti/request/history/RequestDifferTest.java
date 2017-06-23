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
  private static final int DEFAULT_NUM_POINTS = 500;
  private static final int DEFAULT_NUM_PROPERTIES = 150;
  private Request original;
  private Request modified;

  @Before
  public void initialize() {
    original = createRequest(DEFAULT_NUM_POINTS, DEFAULT_NUM_PROPERTIES);
    modified = createRequest(DEFAULT_NUM_POINTS, DEFAULT_NUM_PROPERTIES);
  }

  @Test
  public void singleModificationDiffTest() {
    modified.getPoints().get(0).getProperties().put("Property_0", "Test_0");

    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(1, changes.size());

    Change change = changes.get(0);
    assertEquals("/points[1]/properties{Property_0}", change.getPath());
    assertEquals(Long.valueOf("1"), change.getLineNo());
    assertEquals("Property_0", change.getProperty());
    assertEquals("CHANGED", change.getState());
    assertEquals("Test_0", change.getModified());
    assertEquals("Value_0", change.getOriginal());
  }

  @Test
  public void multipleModificationsDiffTest() {
    modified.getPoints().get(0).getProperties().put("Property_0", "Test_0");
    modified.getPoints().get(1).getProperties().put("Property_1", "Test_1");

    ChangeEvent changeEvent = RequestDiffer.diff(modified, original, ID_PROPERTY);
    List<Change> changes = changeEvent.getChanges();

    assertEquals(2, changes.size());
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
