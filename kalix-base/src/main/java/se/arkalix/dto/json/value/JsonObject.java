package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonType;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
import se.arkalix.internal.dto.json.JsonWriter;
import se.arkalix.util.annotation.Internal;

import java.util.*;

@SuppressWarnings("unused")
public class JsonObject implements JsonCollection, Iterable<JsonPair> {
    private final List<JsonPair> pairs;

    public JsonObject(final List<JsonPair> pairs) {
        this.pairs = Collections.unmodifiableList(pairs);
    }

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

    public List<JsonPair> pairs() {
        return pairs;
    }

    @Override
    public Iterator<JsonPair> iterator() {
        return pairs.iterator();
    }

    public static JsonObject readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    @Internal
    public static JsonObject readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.OBJECT) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected object", token.readStringRaw(source), token.begin());
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
            JsonWriter.write(pair.name(), writer);
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
