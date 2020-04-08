package se.arkalix.internal.dto.json;

import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.json.JsonType;
import se.arkalix.util.annotation.Internal;

import java.util.List;

@Internal
public class JsonTokenBuffer {
    private final List<JsonToken> tokens;
    private final BinaryReader source;
    private int offset;

    JsonTokenBuffer(final List<JsonToken> tokens, final BinaryReader source) {
        this.tokens = tokens;
        this.source = source;

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

    public void skip() {
        var token = next();
        if (token.nChildren == 0) {
            return;
        }
        if (token.type == JsonType.ARRAY) {
            for (var n = token.nChildren; n-- != 0;) {
                skip();
            }
            return;
        }
        if (token.type == JsonType.OBJECT) {
            for (var n = token.nChildren; n-- != 0;) {
                offset += 1; // Skip key.
                skip();
            }
        }
    }

    public BinaryReader source() {
        return source;
    }
}
