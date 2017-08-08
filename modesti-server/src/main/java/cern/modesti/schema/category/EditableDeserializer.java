package cern.modesti.schema.category;

import cern.modesti.request.RequestType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserialise editable fields.
 *
 * e.g.
 *
 * editable => {
 *     "UPDATE" => { .. },
 *     "CREATE" => { .. }
 * }
 *
 * or
 *
 * editable => {
 *     "status" => "IN_PROGRESS",
 *     "type" => "CREATE"
 * }
 *
 * or
 *
 * editable => false
 *
 */
public class EditableDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(com.fasterxml.jackson.core.JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        Map<String, Object> map = new HashMap<>();

        if (node.isBoolean()) {
            map.put("_", node.booleanValue());
        } else if (node.isObject()) {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.treeToValue(node, HashMap.class);
            if (containsTypeKeys(map)) {
                checkRequestType(map);
            } else {
                map = reformatWithoutKeys(map);
            }
        }

        return map;
    }

    /**
     * Check if a field "type" exists and if its value matches the map key.
     *
     * @param map the map from the JSON
     *
     * @return JSON as HashMap
     *
     * @throws JsonProcessingException
     */
    private static void checkRequestType(final Map<String, Object> map) throws JsonProcessingException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map innerMap = (Map) entry.getValue();
                if (innerMap.containsKey("type") && !innerMap.get("type").equals(entry.getKey())) {
                    throw new JsonMappingException("type field (" + innerMap.get("type") + ") does not match type key (" + entry.getKey() + ")");
                }
            }
        }
    }

    /**
     * Convert the following format:
     *
     * editable => {
     *     "status" => [],
     *     "type => "CREATE"
     * }
     *
     * to
     *
     * editable => {
     *     "CREATE" => {
     *          "status" => []
     *     }
     * }
     *
     * @param map
     * @return
     * @throws JsonProcessingException
     */
    private static Map<String, Object> reformatWithoutKeys(final Map<String, Object> map) throws JsonProcessingException {
        Map<String, Object> copy = new HashMap<>(map);
        Map<String, Object> newMap = new HashMap();
        if (copy.containsKey("type")) {
            String type = map.get("type").toString();
            if (!isValidRequestType(type)) {
                throw new JsonMappingException("Invalid type: " + type);
            }
            copy.remove("type");
            newMap.put(type, copy);
        } else {
            newMap.put("_", copy);
        }
        return newMap;
    }

    private static boolean isValidRequestType(final String type) {
        return Arrays.stream(RequestType.values()).anyMatch(t -> t.name().equals(type));
    }

    private static boolean containsTypeKeys(final Map<String, Object> map) {
        return map.keySet().stream().anyMatch(e -> e.equals("_") || isValidRequestType(e));
    }
}


