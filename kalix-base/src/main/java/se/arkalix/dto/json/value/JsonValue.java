package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.json.*;

public interface JsonValue extends JsonReadable, JsonWritable {
    JsonType type();

    static JsonValue readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    static JsonValue readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        var token = buffer.peek();
        switch (token.type()) {
        case OBJECT: return JsonObject.readJson(buffer);
        case ARRAY: return JsonArray.readJson(buffer);
        case STRING: return JsonString.readJson(buffer);
        case NUMBER: return JsonNumber.readJson(buffer);
        case TRUE: return JsonTrue.readJson(buffer);
        case FALSE: return JsonFalse.readJson(buffer);
        case NULL: return JsonNull.readJson(buffer);
        default:
            throw new IllegalStateException("Illegal token type: " + token.type());
        }
    }
}
