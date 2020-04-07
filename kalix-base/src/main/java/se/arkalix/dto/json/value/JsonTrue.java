package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonTokenBuffer;
import se.arkalix.dto.json.JsonTokenizer;
import se.arkalix.dto.json.JsonType;

@SuppressWarnings("unused")
public class JsonTrue implements JsonValue {
    private static final JsonTrue INSTANCE = new JsonTrue();

    private JsonTrue() {}

    public static JsonTrue instance() {
        return INSTANCE;
    }

    @Override
    public JsonType type() {
        return JsonType.TRUE;
    }

    public static JsonTrue readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    public static JsonTrue readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        var token = buffer.next();
        if (token.type() != JsonType.TRUE) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected true",
                token.readStringRaw(buffer.source()), token.begin());
        }
        return INSTANCE;
    }

    @Override
    public void writeJson(final BinaryWriter writer) {
        writer.write(new byte[]{'t', 'r', 'u', 'e'});
    }
}
