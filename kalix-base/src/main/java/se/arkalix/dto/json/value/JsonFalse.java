package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonType;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

@SuppressWarnings("unused")
public class JsonFalse implements JsonValue {
    private static final JsonFalse INSTANCE = new JsonFalse();

    private JsonFalse() {}

    public static JsonFalse instance() {
        return INSTANCE;
    }

    @Override
    public JsonType type() {
        return JsonType.FALSE;
    }

    public static JsonFalse readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    @Internal
    public static JsonFalse readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        var token = buffer.next();
        if (token.type() != JsonType.FALSE) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected false",
                token.readStringRaw(buffer.source()), token.begin());
        }
        return INSTANCE;
    }

    @Override
    public void writeJson(final BinaryWriter writer) {
        writer.write(new byte[]{'f', 'a', 'l', 's', 'e'});
    }
}
