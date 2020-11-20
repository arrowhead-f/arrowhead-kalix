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
 * JSON object.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
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
     * Reads JSON object from given {@code reader}.
     *
     * @param reader Source containing JSON object at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON object.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON object at the current read
     *                                    offset.
     */
    public static JsonObject decodeJson(final BinaryReader reader) {
        return decodeJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonObject decodeJson(final JsonTokenBuffer buffer) {
        final var reader = buffer.reader();
        var token = buffer.next();
        if (token.type() != JsonType.OBJECT) {
            throw new DecoderReadUnexpectedToken(
                CodecType.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected object");
        }
        final var pairs = new ArrayList<JsonPair>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            final var name = buffer.next();
            final var value = JsonValue.decodeJson(buffer);
            pairs.add(new JsonPair(JsonPrimitives.readString(name, reader), value));
        }
        return new JsonObject(pairs);
    }

    @Override
    public CodecType encodeJson(final BinaryWriter writer) {
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
            JsonPrimitives.write(pair.name(), writer);
            writer.write(new byte[]{'"', ':'});
            pair.value().encodeJson(writer);
        }
        writer.write((byte) '}');
        return CodecType.JSON;
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
