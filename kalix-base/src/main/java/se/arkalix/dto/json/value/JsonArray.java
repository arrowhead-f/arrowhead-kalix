package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoExclusive;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
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
        this.elements = Collections.unmodifiableList(Objects.requireNonNull(elements, "Expected elements"));
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
     * Reads JSON array from given {@code source}.
     *
     * @param source Source containing JSON array at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON array.
     * @throws DtoReadException If the source does not contain a valid JSON
     *                          array at the current read offset, or if the
     *                          source could not be read.
     */
    public static JsonArray readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonArray readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.ARRAY) {
            throw new DtoReadException(JsonArray.class, JSON, "expected array",
                token.readStringRaw(source), token.begin());
        }
        final var elements = new ArrayList<JsonValue>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            elements.add(JsonValue.readJson(buffer));
        }
        return new JsonArray(elements);
    }

    @Override
    public void writeJson(final BinaryWriter writer) throws DtoWriteException {
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
