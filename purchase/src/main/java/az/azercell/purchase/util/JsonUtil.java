package az.azercell.purchase.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public interface JsonUtil {
    static JsonNode parseJsonArray(String jsonArrayString) throws JsonProcessingException
    {
        return (jsonArrayString == null) ? NullNode.getInstance() : new ObjectMapper().readTree(jsonArrayString);
    }
}
