package se.arkalix.codec.json._internal;

import se.arkalix.codec.json.JsonType;
import se.arkalix.io.buffer.BufferReader;
import se.arkalix.util.annotation.Internal;

import java.util.List;

@Internal
public class JsonTokenBuffer {
    private final List<JsonToken> tokens;
    private final BufferReader reader;
    private int offset;

    JsonTokenBuffer(final List<JsonToken> tokens, final BufferReader reader) {
        this.tokens = tokens;
        this.reader = reader;

        offset = 0;
    }

    public boolean atEnd() {
        return offset == tokens.size();
    }

    public JsonToken next() {
        return tokens.get(offset++);
    }

    public JsonToken peek() {
        return tokens.get(offset);
    }

    public void skipElement() {
        offset += 1;
    }

    public void skipValue() {
        var token = next();
        if (token.nChildren == 0) {
            return;
        }
        if (token.type == JsonType.ARRAY) {
            for (var n = token.nChildren; n-- != 0; ) {
                skipValue();
            }
            return;
        }
        if (token.type == JsonType.OBJECT) {
            for (var n = token.nChildren; n-- != 0; ) {
                offset += 1; // Skip key.
                skipValue();
            }
        }
    }

    public BufferReader reader() {
        return reader;
    }
}
