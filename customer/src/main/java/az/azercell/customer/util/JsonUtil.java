package az.azercell.customer.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;

public interface JsonUtil
{
    /**
     * Parses a JSON array string into a JsonNode object.
     * If the input string is null, returns a NullNode instance.
     *
     * @param jsonArrayString The JSON array string to parse.
     * @return The JsonNode object representing the parsed JSON data.
     * @throws JsonProcessingException if the input string is not valid JSON or does not represent an array.
     */
    static JsonNode parseJsonArray(String jsonArrayString) throws JsonProcessingException
    {
        return (jsonArrayString == null) ? NullNode.getInstance() : new ObjectMapper().readTree(jsonArrayString);
    }
}
