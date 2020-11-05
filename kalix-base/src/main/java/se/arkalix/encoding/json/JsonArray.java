package se.arkalix.encoding.json;

import se.arkalix.dto.DtoExclusive;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.DecoderReadUnexpectedToken;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;
import se.arkalix.encoding.json._internal.JsonTokenBuffer;
import se.arkalix.encoding.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.util.*;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * JSON array.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@DtoExclusive(JSON)
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
    public static JsonArray readJson(final BinaryReader reader) {
        return readJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonArray readJson(final JsonTokenBuffer buffer) {
        final var reader = buffer.reader();
        var token = buffer.next();
        if (token.type() != JsonType.ARRAY) {
            throw new DecoderReadUnexpectedToken(
                Encoding.JSON,
                reader,
                token.readStringRaw(reader),
                token.begin(),
                "expected array");
        }
        final var elements = new ArrayList<JsonValue>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            elements.add(JsonValue.readJson(buffer));
        }
        return new JsonArray(elements);
    }

    @Override
    public Encoding writeJson(final BinaryWriter writer) {
        writer.write((byte) '[');
        var isFirst = true;
        for (final var element : elements) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                writer.write((byte) ',');
            }
            element.writeJson(writer);
        }
        writer.write((byte) ']');
        return Encoding.JSON;
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
