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

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
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

    @Override
    public String toString() {
        return "null";
    }
}