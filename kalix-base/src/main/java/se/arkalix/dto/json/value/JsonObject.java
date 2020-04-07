package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonTokenBuffer;
import se.arkalix.dto.json.JsonTokenizer;
import se.arkalix.dto.json.JsonType;
import se.arkalix.dto.json.JsonWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class JsonObject implements JsonCollection, Iterable<JsonPair> {
    private final List<JsonPair> pairs;

    public JsonObject(final List<JsonPair> pairs) {
        this.pairs = pairs;
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
            JsonWriter.write(pair.name(), writer);
            writer.write((byte) ':');
            pair.value().writeJson(writer);
        }
        writer.write((byte) '}');
    }
}
