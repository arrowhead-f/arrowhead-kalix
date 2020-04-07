package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonTokenBuffer;
import se.arkalix.dto.json.JsonTokenizer;
import se.arkalix.dto.json.JsonType;

@SuppressWarnings("unused")
public class JsonNull implements JsonValue {
    private static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    public static JsonNull instance() {
        return INSTANCE;
    }

    @Override
    public JsonType type() {
        return JsonType.NULL;
    }

    public static JsonNull readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    public static JsonNull readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        var token = buffer.next();
        if (token.type() != JsonType.NULL) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected null",
                token.readStringRaw(buffer.source()), token.begin());
        }
        return INSTANCE;
    }

    @Override
    public void writeJson(final BinaryWriter writer) {
        writer.write(new byte[]{'n', 'u', 'l', 'l'});
    }
}