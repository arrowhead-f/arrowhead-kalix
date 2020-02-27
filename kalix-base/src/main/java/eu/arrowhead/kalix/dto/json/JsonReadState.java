package eu.arrowhead.kalix.dto.json;

import java.nio.ByteBuffer;
import java.util.List;

public class JsonReadState {
    private final List<JsonToken> tokens;
    private final ByteBuffer source;
    private int offset;

    public JsonReadState(final List<JsonToken> tokens, final ByteBuffer source) {
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
