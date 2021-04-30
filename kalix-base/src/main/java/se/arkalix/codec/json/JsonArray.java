package se.arkalix.codec.json;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderReadUnexpectedToken;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.codec.json._internal.JsonPrimitives;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.util.*;

/**
 * JSON array.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@SuppressWarnings("unused")
public class JsonArray implements JsonCollection<Integer>, Iterable<JsonValue> {
    private final List<JsonValue> elements;

    /**
     * Creates new JSON array from given list of {@link JsonValue elements}.
     *
     * @param elements Elements to make up the contents of the created JSON
     *                 array.
     */
    public JsonArray(final List<JsonValue> elements) {
        this.elements = Collections.unmodifiableList(Objects.requireNonNull(elements, "elements"));
    }

    /**
     * Creates new JSON array from given list of {@link JsonValue elements}.
     *
     * @param elements Elements to make up the contents of the created JSON
     *                 array.
     */
    public JsonArray(final JsonValue... elements) {
        this.elements = List.of(elements);
    }

    @Override
    public JsonType type() {
        return JsonType.ARRAY;
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Optional<JsonValue> get(final Integer index) {
        return (index < 0 || index > elements.size() - 1)
            ? Optional.empty()
            : Optional.of(elements.get(index));
    }

    /**
     * @return Array elements.
     */
    public List<JsonValue> elements() {
        return elements;
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return elements.iterator();
    }

    /**
     * Reads JSON array from given {@code reader}.
     *
     * @param reader Source containing JSON array at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON array.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON array at the current read
     *                                    offset.
     */
    public static JsonArray decodeJson(final BinaryReader reader) {
        return decodeJson_(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     *
     * @param buffer Buffer of JSON tokens.
     * @return Decoded array.
     */
    @Internal
    public static JsonArray decodeJson_(final JsonTokenBuffer buffer) {
        final var reader = buffer.reader();
        var token = buffer.next();
        if (token.type() != JsonType.ARRAY) {
            throw new DecoderReadUnexpectedToken(
                CodecType.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected array");
        }
        final var elements = new ArrayList<JsonValue>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            elements.add(JsonValue.decodeJson_(buffer));
        }
        return new JsonArray(elements);
    }

    @Override
    public CodecType encodeJson(final BinaryWriter writer) {
        writer.write((byte) '[');
        var isFirst = true;
        for (final var element : elements) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                writer.write((byte) ',');
            }
            element.encodeJson(writer);
        }
        writer.write((byte) ']');
        return CodecType.JSON;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final JsonArray that = (JsonArray) other;
        return elements.equals(that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        return "[" + elements + ']';
    }
}
