package se.arkalix.encoding.json._internal;

import se.arkalix.encoding.DecoderReadUnexpectedToken;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.json.JsonType;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;

@Internal
@SuppressWarnings("unused")
public final class JsonToken {
    final JsonType type;

    int begin, end;
    int nChildren;

    JsonToken(final JsonType type, final int begin, final int end, final int nChildren) {
        assert begin >= 0 && begin < end && nChildren >= 0;

        this.type = type;
        this.begin = begin;
        this.end = end;
        this.nChildren = nChildren;
    }

    public JsonType type() {
        return type;
    }

    public int begin() {
        return begin;
    }

    public int end() {
        return end;
    }

    public int length() {
        return end - begin;
    }

    public int nChildren() {
        return nChildren;
    }
}
