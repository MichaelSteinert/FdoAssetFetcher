package fdoassetfetcher;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.Map;

public class MetadataExtractor {
    public static EdcMetadata extractMetadata(JsonObject dataset) {
        String id = null;
        String name = null;
        String description = null;
        String isFdo = null;
        String contenttype = null;
        for (Map.Entry<String, JsonValue> entry : dataset.entrySet()) {
            String key = entry.getKey();
            JsonValue value = entry.getValue();
            if (value.getValueType() == JsonValue.ValueType.ARRAY) {
                JsonArray valueArray = value.asJsonArray();
                if (!valueArray.isEmpty()) {
                    JsonValue element = valueArray.get(0);
                    if (element.getValueType() == JsonValue.ValueType.OBJECT) {
                        JsonObject valueObject = element.asJsonObject();
                        String extractedValue = valueObject.getString("@value", null);
                        switch (key) {
                            case "https://w3id.org/edc/v0.0.1/ns/name":
                                name = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/description":
                                description = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/isFdo":
                                isFdo = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/id":
                                id = extractedValue;
                                break;
                            case "https://w3id.org/edc/v0.0.1/ns/contenttype":
                                contenttype = extractedValue;
                                break;
                        }
                    }
                }
            }
        }
        return new EdcMetadata(id, name, description, isFdo, contenttype);
    }
}