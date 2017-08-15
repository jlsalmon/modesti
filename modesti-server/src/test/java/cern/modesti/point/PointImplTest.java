package cern.modesti.point;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PointImplTest {
    @Test
    public void addSingleErrorMessage() throws Exception {
        Point point = new PointImpl();
        String errorMessage = "errorMessage";
        point.addErrorMessage("category", "property", errorMessage);

        assertEquals("There should be only one property with errors",1, point.getErrors().size());
        assertEquals("There should be only one error logged",1, point.getErrors().get(0).getErrors().size());
        assertEquals(errorMessage, point.getErrors().get(0).getErrors().get(0));
    }

    @Test
    public void addMultipleIdenticalErrorMessages() throws Exception {
        Point point = new PointImpl();
        String errorMessage = "errorMessage";
        point.addErrorMessage("category", "property", errorMessage);
        point.addErrorMessage("category", "property", errorMessage);

        assertEquals("There should be only one property with errors",1, point.getErrors().size());
        assertEquals("There should be only one unique error logged",1, point.getErrors().get(0).getErrors().size());
        assertEquals(errorMessage, point.getErrors().get(0).getErrors().get(0));
    }

    @Test
    public void addMultipleDifferentErrorMessages() throws Exception {
        Point point = new PointImpl();
        String errorMessage1 = "errorMessage1";
        String errorMessage2 = "errorMessage2";
        point.addErrorMessage("category", "property", errorMessage1);
        point.addErrorMessage("category", "property", errorMessage2);

        assertEquals("There should be only one property with errors",1, point.getErrors().size());
        assertEquals("There should be two different errors logged",2, point.getErrors().get(0).getErrors().size());
        assertEquals(errorMessage1, point.getErrors().get(0).getErrors().get(0));
        assertEquals(errorMessage2, point.getErrors().get(0).getErrors().get(1));
    }

    @Test
    public void addMultipleDifferentErrorMessagesToDifferentProperties() throws Exception {
        Point point = new PointImpl();
        String errorMessage1 = "errorMessage1";
        String errorMessage2 = "errorMessage2";
        point.addErrorMessage("category", "property1", errorMessage1);
        point.addErrorMessage("category", "property2", errorMessage2);

        assertEquals("There should be two properties with errors", 2, point.getErrors().size());
        assertEquals("There should be one error in each property",1, point.getErrors().get(0).getErrors().size());
        assertEquals("There should be one error in each property", 1, point.getErrors().get(1).getErrors().size());
        assertEquals(errorMessage1, point.getErrors().get(0).getErrors().get(0));
        assertEquals(errorMessage2, point.getErrors().get(1).getErrors().get(0));
    }

    @Test
    public void addNullErrorMessage() throws Exception {
        Point point = new PointImpl();
        String errorMessage = null;
        point.addErrorMessage("category", "property", errorMessage);

        assertEquals("There should be only one property with errors",1, point.getErrors().size());
        assertEquals("There should be only one error logged",1, point.getErrors().get(0).getErrors().size());
        assertNotEquals("Error message should be rewritten and not be null", errorMessage, point.getErrors().get(0).getErrors().get(0));
    }

    @Test
    public void addMultipleNullErrorMessages() throws Exception {
        Point point = new PointImpl();
        String errorMessage = null;
        point.addErrorMessage("category", "property", errorMessage);
        point.addErrorMessage("category", "property", errorMessage);

        assertEquals("There should be only one property with errors",1, point.getErrors().size());
        assertEquals("There should be only one error logged",1, point.getErrors().get(0).getErrors().size());
        assertNotEquals("Error message should be rewritten and not be null", errorMessage, point.getErrors().get(0).getErrors().get(0));
    }

    @Test
    public void addMultipleNullErrorMessagesToDifferentProperties() throws Exception {
        Point point = new PointImpl();
        String errorMessage = null;
        point.addErrorMessage("category", "property1", errorMessage);
        point.addErrorMessage("category", "property2", errorMessage);

        assertEquals("There should be only two properties with errors",2, point.getErrors().size());
        assertEquals("There should be one error in each property",1, point.getErrors().get(0).getErrors().size());
        assertEquals("There should be one error in each property", 1, point.getErrors().get(1).getErrors().size());
        assertNotEquals(errorMessage, point.getErrors().get(0).getErrors().get(0));
        assertNotEquals(errorMessage, point.getErrors().get(1).getErrors().get(0));
    }

}