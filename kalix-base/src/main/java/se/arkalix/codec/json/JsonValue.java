package se.arkalix.codec.json;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

/**
 * Any kind of JSON value.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
public interface JsonValue {
    JsonType type();

    /**
     * Attempts to use this JSON value into a Java boolean.
     * <p>
     * The attempt is successful only if the underlying JSON type is {@link
     * JsonBoolean}.
     *
     * @return This value as a Java boolean, if possible.
     */
    default Optional<Boolean> tryToBoolean() {
        return Optional.empty();
    }

    /**
     * Attempts to use this JSON value into a Java double.
     * <p>
     * The attempt is successful only if the underlying JSON type is {@link
     * JsonNumber}.
     *
     * @return This value as a Java double, if possible.
     */
    default Optional<Double> tryToDouble() {
        return Optional.empty();
    }

    /**
     * Attempts to use this JSON value into a Java long.
     * <p>
     * The attempt is successful only if the underlying JSON type is {@link
     * JsonNumber}.
     *
     * @return This value as a Java long, if possible.
     */
    default Optional<Long> tryToLong() {
        return Optional.empty();
    }

    /**
     * Attempts to use this JSON value into a Java String.
     * <p>
     * The attempt is successful only if the underlying JSON type is {@link
     * JsonString}.
     *
     * @return This value as a Java String, if possible.
     */
    default Optional<String> tryToString() {
        return Optional.empty();
    }

    /**
     * Writes JSON value to given {@code reader}.
     *
     * @param writer Writer to which this JSON value will be written.
     * @return {@link CodecType#JSON}.
     */
    CodecType writeJson(final BinaryWriter writer);

    /**
     * Reads JSON value from given {@code reader}.
     *
     * @param reader Reader containing JSON value at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON value.
     */
    static JsonValue readJson(final BinaryReader reader) {
         return readJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    static JsonValue readJson(final JsonTokenBuffer buffer) {
        var token = buffer.peek();
        switch (token.type()) {
        case OBJECT:
            return JsonObject.readJson(buffer);
        case ARRAY:
            return JsonArray.readJson(buffer);
        case STRING:
            return JsonString.readJson(buffer);
        case NUMBER:
            return JsonNumber.readJson(buffer);
        case TRUE:
        case FALSE:
            return JsonBoolean.readJson(buffer);
        case NULL:
            return JsonNull.readJson(buffer);
        default:
            throw new IllegalStateException("Illegal token type: " + token.type());
        }
    }
}
