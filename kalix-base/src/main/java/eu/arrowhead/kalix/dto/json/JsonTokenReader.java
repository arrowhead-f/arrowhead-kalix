package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.binary.BinaryReader;

import java.util.List;

public class JsonTokenReader {
    private final List<JsonToken> tokens;
    private final BinaryReader source;
    private int offset;

    JsonTokenReader(final List<JsonToken> tokens, final BinaryReader source) {
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
