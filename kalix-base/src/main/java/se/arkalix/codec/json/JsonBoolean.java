package se.arkalix.codec.json;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderException;
import se.arkalix.codec.json._internal.JsonPrimitives;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.io.buffer.BufferReader;
import se.arkalix.io.buffer.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

/**
 * JSON true or false.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
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
     * @throws DecoderException If the reader does not contain a valid JSON
     *                          boolean at the current read offset.
     */
    public static JsonBoolean decodeJson(final BufferReader reader) {
        return decodeJson_(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     *
     * @param buffer Buffer of JSON tokens.
     * @return Decoded boolean.
     */
    @Internal
    public static JsonBoolean decodeJson_(final JsonTokenBuffer buffer) {
        var token = buffer.next();
        switch (token.type()) {
        case TRUE: return TRUE;
        case FALSE: return FALSE;
        default:
            final var reader = buffer.reader();
            throw new DecoderException(
                CodecType.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected 'true' or 'false'");
        }
    }

    @Override
    public CodecType encodeJson(final BufferWriter writer) {
        writer.write(value ? BYTES_TRUE : BYTES_FALSE);
        return CodecType.JSON;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }
}
