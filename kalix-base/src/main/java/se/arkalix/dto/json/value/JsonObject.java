package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoExclusive;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json._internal.JsonTokenBuffer;
import se.arkalix.dto.json._internal.JsonTokenizer;
import se.arkalix.dto.json._internal.JsonWrite;
import se.arkalix.util.annotation.Internal;

import java.util.*;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * JSON object.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@DtoExclusive(JSON)
@SuppressWarnings("unused")
public class JsonObject implements JsonCollection<String>, Iterable<JsonPair> {
    private final List<JsonPair> pairs;

    /**
     * Creates new JSON array from given list of {@link JsonPair pairs}.
     *
     * @param pairs Pairs to make up the contents of the created JSON object.
     */
    public JsonObject(final List<JsonPair> pairs) {
        this.pairs = Collections.unmodifiableList(pairs);
    }

    /**
     * Creates new JSON array from given list of {@link JsonPair pairs}.
     *
     * @param pairs Pairs to make up the contents of the created JSON object.
     */
    public JsonObject(final JsonPair... pairs) {
        this.pairs = List.of(pairs);
    }

    @Override
    public JsonType type() {
        return JsonType.OBJECT;
    }

    @Override
    public boolean isEmpty() {
        return pairs.isEmpty();
    }

    @Override
    public int size() {
        return pairs.size();
    }

    @Override
    public Optional<JsonValue> get(final String key) {
        if (key != null) {
            for (final var pair : pairs) {
                if (key.equals(pair.name())) {
                    return Optional.of(pair.value());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return Object pairs.
     */
    public List<JsonPair> pairs() {
        return pairs;
    }

    @Override
    public Iterator<JsonPair> iterator() {
        return pairs.iterator();
    }

    /**
     * Reads JSON object from given {@code source}.
     *
     * @param source Source containing JSON object at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON object.
     * @throws DtoReadException If the source does not contain a valid JSON
     *                          object at the current read offset, or if the
     *                          source could not be read.
     */
    public static JsonObject readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonObject readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.OBJECT) {
            throw new DtoReadException(JsonObject.class, DtoEncoding.JSON,
                "expected object", token.readStringRaw(source), token.begin());
        }
        final var pairs = new ArrayList<JsonPair>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            final var name = buffer.next();
            final var value = JsonValue.readJson(buffer);
            pairs.add(new JsonPair(name.readString(source), value));
        }
        return new JsonObject(pairs);
    }

    @Override
    public void writeJson(final BinaryWriter writer) throws DtoWriteException {
        writer.write((byte) '{');
        var isFirst = true;
        for (final var pair : pairs) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                writer.write((byte) ',');
            }
            writer.write((byte) '"');
            JsonWrite.write(pair.name(), writer);
            writer.write(new byte[]{'"', ':'});
            pair.value().writeJson(writer);
        }
        writer.write((byte) '}');
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final JsonObject jsonPairs = (JsonObject) other;
        return pairs.equals(jsonPairs.pairs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairs);
    }

    @Override
    public String toString() {
        return "{" + pairs + '}';
    }
}
