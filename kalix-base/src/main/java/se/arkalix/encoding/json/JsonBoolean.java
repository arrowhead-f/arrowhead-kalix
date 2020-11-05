package se.arkalix.encoding.json;

import se.arkalix.dto.DtoExclusive;
import se.arkalix.encoding.DecoderReadUnexpectedToken;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;
import se.arkalix.encoding.json._internal.JsonPrimitives;
import se.arkalix.encoding.json._internal.JsonTokenBuffer;
import se.arkalix.encoding.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * JSON true or false.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@DtoExclusive(JSON)
@SuppressWarnings("unused")
public class JsonBoolean implements JsonValue {
    private static final byte[] BYTES_TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static final byte[] BYTES_FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};

    private final boolean value;

    private JsonBoolean(final boolean value) {
        this.value = value;
    }

    /**
     * JSON true.
     */
    public static final JsonBoolean TRUE = new JsonBoolean(true);

    /**
     * JSON false.
     */
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    @Override
    public JsonType type() {
        return JsonType.FALSE;
    }

    @Override
    public Optional<Boolean> tryToBoolean() {
        return Optional.of(value);
    }

    /**
     * @return {@code true} or {@code false}.
     */
    public boolean value() {
        return value;
    }

    /**
     * Reads "false" from given {@code reader}.
     *
     * @param reader Source containing "false" at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON false.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON boolean at the current
     *                                    read offset.
     */
    public static JsonBoolean readJson(final BinaryReader reader) {
        return readJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonBoolean readJson(final JsonTokenBuffer buffer) {
        var token = buffer.next();
        switch (token.type()) {
        case TRUE: return TRUE;
        case FALSE: return FALSE;
        default:
            final var reader = buffer.reader();
            throw new DecoderReadUnexpectedToken(
                Encoding.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected 'true' or 'false'");
        }
    }

    @Override
    public Encoding writeJson(final BinaryWriter writer) {
        writer.write(value ? BYTES_TRUE : BYTES_FALSE);
        return Encoding.JSON;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }
}
