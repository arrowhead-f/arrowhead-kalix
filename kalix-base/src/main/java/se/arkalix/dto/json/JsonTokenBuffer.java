package se.arkalix.dto.json;

import se.arkalix.dto.binary.BinaryReader;

import java.util.List;

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

    public BinaryReader source() {
        return source;
    }
}
