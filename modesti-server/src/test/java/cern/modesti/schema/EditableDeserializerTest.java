package cern.modesti.schema;

import cern.modesti.schema.category.EditableDeserializer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test that the Editable field deserialiser works.
 *
 * @author James Hamilton
 */
public class EditableDeserializerTest {

    private ObjectMapper mapper;
    private EditableDeserializer deserializer;
    private ResourcePatternResolver resolver;

    @Before
    public void setup() throws IOException {
        mapper = new ObjectMapper();
        deserializer = new EditableDeserializer();
        resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void editableWithBooleanOnly() throws IOException {
        Map<String, Object> result = this.deserialise(
            new FileInputStream(resolver.getResource("schemas/editable-test-boolean.json").getFile())
        );
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertTrue(result.containsKey("_"));
        assertEquals(result.get("_"), false);
    }

    @Test
    public void editableWithoutTypeTest() throws IOException {
        Map<String, Object> result = this.deserialise(
            new FileInputStream(resolver.getResource("schemas/editable-test-without-type.json").getFile())
        );
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertEquals(getExpectedResultMap(), result);
    }

    @Test
    public void editableWithTypeTest() throws IOException {
        Map<String, Object> result = this.deserialise(
            new FileInputStream(resolver.getResource("schemas/editable-test-with-type.json").getFile())
        );
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertEquals(getExpectedResultMap(), result);
    }

    @Test
    public void editableWithStatusArrayTest() throws IOException {
        Map<String, Object> result = this.deserialise(
            new FileInputStream(resolver.getResource("schemas/editable-test-status-array.json").getFile())
        );
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertTrue(result.containsKey("_"));
        Map<String, List<String>> resultInnerMap = (Map<String, List<String>>)result.get("_");
        assertTrue(resultInnerMap.size() == 1);
        assertTrue(resultInnerMap.containsKey("status"));
        List<String> expectedStatuses = new ArrayList();
        expectedStatuses.add("IN_PROGRESS");
        expectedStatuses.add("IN_ADDRESSING");
        assertTrue(resultInnerMap.get("status").equals(expectedStatuses));
    }

    private static Map<String, Object> getExpectedResultMap() {
        Map<String, Object> expectedResult = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        expectedResult.put("CREATE", innerMap);
        innerMap.put("status", "FOR_ADDRESSING");
        Map<String, Object> conditionMap = new HashMap<>();
        innerMap.put("condition", conditionMap);
        conditionMap.put("field", "pointType");
        conditionMap.put("operation", "equals");
        conditionMap.put("value", "APIMMD");
        return expectedResult;
    }

    /**
     * @param stream the input stream containing JSON
     * @return map of the deserialised JSON
     * @see <a href="https://stackoverflow.com/a/37001410/379565">this Stack Overflow post</a>
     */
    @SneakyThrows({JsonParseException.class, IOException.class})
    private Map<String, Object> deserialise(final InputStream stream) {
        JsonParser parser = mapper.getFactory().createParser(stream);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        return deserializer.deserialize(parser, ctxt);
    }
}
