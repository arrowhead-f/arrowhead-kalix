package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoExclusive;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * JSON null.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@DtoExclusive(JSON)
@SuppressWarnings("unused")
public class JsonNull implements JsonValue {
    private static final byte[] BYTES_NULL = new byte[]{'n', 'u', 'l', 'l'};

    private JsonNull() {}

    /**
     * JSON null.
     */
    public static final JsonNull INSTANCE = new JsonNull();

    @Override
    public JsonType type() {
        return JsonType.NULL;
    }

    /**
     * Reads "null" from given {@code source}.
     *
     * @param source Source containing "null" at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON null.
     * @throws DtoReadException If the source does not contain the word "null"
     *                          at the current read offset, or if the source
     *                          could not be read.
     */
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
        writer.write(BYTES_NULL);
    }

    @Override
    public String toString() {
        return "null";
    }
}