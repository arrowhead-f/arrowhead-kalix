package se.arkalix.internal.dto.json;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.json.JsonType;
import se.arkalix.util.annotation.Internal;

import java.util.ArrayList;
import java.util.List;

@Internal
public class JsonReader {
    private JsonReader() {}

    public static <V> V readOne(final Reader<V> reader, final BinaryReader source)
        throws DtoReadException
    {
        return reader.read(JsonTokenizer.tokenize(source));
    }

    public static <V> List<V> readMany(final Reader<V> reader, final BinaryReader source)
        throws DtoReadException
    {
        final var buffer = JsonTokenizer.tokenize(source);
        var token = buffer.next();
        if (token.type() != JsonType.ARRAY) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected array", token.readStringRaw(source), token.begin());
        }
        final var elements = new ArrayList<V>(token.nChildren());
        for (var n = token.nChildren(); n-- != 0; ) {
            elements.add(reader.read(buffer));
        }
        return elements;
    }

    public interface Reader<V> {
        V read(final JsonTokenBuffer buffer) throws DtoReadException;
    }
}
