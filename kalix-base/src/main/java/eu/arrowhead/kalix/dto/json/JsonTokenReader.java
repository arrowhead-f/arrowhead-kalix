package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.ReadException;

import java.nio.ByteBuffer;
import java.util.List;

public class JsonTokenReader {
    private final List<JsonToken> tokens;
    private final ByteBuffer source;
    private int offset;

    JsonTokenReader(final List<JsonToken> tokens, final ByteBuffer source) {
        this.tokens = tokens;
        this.source = source;

        offset = 0;
    }

    public JsonToken next() {
        return tokens.get(offset++);
    }

    public ByteBuffer source() {
        return source;
    }
}
