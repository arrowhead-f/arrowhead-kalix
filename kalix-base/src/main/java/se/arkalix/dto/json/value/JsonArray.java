package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonTokenBuffer;
import se.arkalix.dto.json.JsonTokenizer;
import se.arkalix.dto.json.JsonType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class JsonArray implements JsonCollection, Iterable<JsonValue> {
    private final List<JsonValue> elements;

    public JsonArray(final List<JsonValue> elements) {
        this.elements = elements;
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

    public List<JsonValue> elements() {
        return elements;
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return elements.iterator();
    }

    public static JsonArray readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    public static JsonArray readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.ARRAY) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected array", token.readStringRaw(source), token.begin());
        }
        final var elements = new ArrayList<JsonValue>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            final var name = buffer.next();
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
}
