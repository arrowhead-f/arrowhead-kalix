package se.arkalix.codec.json;

import se.arkalix.codec.DecoderReadUnexpectedToken;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.codec.json._internal.JsonPrimitives;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

/**
 * JSON null.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
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
     * Reads "null" from given {@code reader}.
     *
     * @param reader Source containing "null" at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON null.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON null at the current read
     *                                    offset.
     */
    public static JsonNull decodeJson(final BinaryReader reader) {
        return decodeJson_(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     *
     * @param buffer Buffer of JSON tokens.
     * @return Decoded null.
     */
    @Internal
    public static JsonNull decodeJson_(final JsonTokenBuffer buffer) {
        var token = buffer.next();
        if (token.type() != JsonType.NULL) {
            final var reader = buffer.reader();
            throw new DecoderReadUnexpectedToken(
                CodecType.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected 'null'");
        }
        return instance;
    }

    @Override
    public CodecType encodeJson(final BinaryWriter writer) {
        writer.write(BYTES_NULL);
        return CodecType.JSON;
    }

    @Override
    public String toString() {
        return "null";
    }
}