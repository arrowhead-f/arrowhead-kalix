package se.arkalix.encoding.json;

import se.arkalix.dto.DtoExclusive;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;
import se.arkalix.encoding.json._internal.JsonTokenBuffer;
import se.arkalix.encoding.json._internal.JsonTokenizer;
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
    public static final JsonNull instance = new JsonNull();

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
            throw new DtoReadException(JsonNull.class, DtoEncoding.JSON,
                "expected 'null'", token.readStringRaw(buffer.source()), token.begin());
        }
        return instance;
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