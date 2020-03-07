package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.ReadException;
import eu.arrowhead.kalix.dto.binary.BinaryReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@SuppressWarnings("unused")
public final class JsonReader {
    private final BinaryReader source;
    private final ArrayList<JsonToken> tokens;

    private int p0;
    private ReadException error = null;

    private JsonReader(final BinaryReader source) {
        this.source = source;
        this.tokens = new ArrayList<>(source.readableBytes() / 16);
        this.p0 = source.readOffset();
    }

    public static JsonTokenReader tokenize(final BinaryReader source) throws ReadException {
        final var tokenizer = new JsonReader(source);
        if (tokenizer.tokenizeRoot()) {
            return new JsonTokenReader(tokenizer.tokens, source);
        }
        throw tokenizer.error;
    }

    private JsonToken collectCandidate(final JsonType type) {
        final var token = new JsonToken(type, p0, source.readOffset(), 0);
        tokens.add(token);
        discardCandidate();
        return token;
    }

    private void discardCandidate() {
        p0 = source.readOffset();
    }

    private void saveCandidateAsError(final String message) {
        final var buffer = new byte[source.readOffset() - p0];
        source.getBytes(p0, buffer);
        error = new ReadException(DataEncoding.JSON, message, new String(buffer, StandardCharsets.UTF_8), p0);
    }

    private void discardWhitespace() {
        for (byte b; source.readableBytes() > 0; ) {
            b = source.peekByte();
            if (b != '\t' && b != '\r' && b != '\n' && b != ' ') {
                break;
            }
            source.skipByte();
        }
        discardCandidate();
    }

    private boolean tokenizeRoot() {
        discardWhitespace();

        switch (source.readByteOrZero()) {
        case '{': return tokenizeObject();
        case '[': return tokenizeArray();
        default:
            saveCandidateAsError("JSON root not object or array");
            return false;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tokenizeValue() {
        discardWhitespace();

        switch (source.readByteOrZero()) {
        case '\0': return true;
        case '{': return tokenizeObject();
        case '[': return tokenizeArray();
        case '"': return tokenizeString();

        case '-':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return tokenizeNumber();

        case 't': return tokenizeTrue();
        case 'f': return tokenizeFalse();
        case 'n': return tokenizeNull();

        default:
            saveCandidateAsError("Unexpected character");
            return false;
        }
    }

    private boolean tokenizeObject() {
        final var object = collectCandidate(JsonType.OBJECT);

        discardWhitespace();

        if (source.readableBytes() == 0) {
            return false;
        }
        var b = source.peekByte();
        if (b == '}') {
            source.skipByte();
            return true;
        }

        while (true) {
            discardWhitespace();

            b = source.readByteOrZero();
            if (b != '\"') {
                saveCandidateAsError("Object key must be string");
                return false;
            }
            if (!tokenizeString()) {
                return false;
            }

            discardWhitespace();

            b = source.readByteOrZero();
            if (b != ':') {
                saveCandidateAsError("Object key not followed by colon");
                return false;
            }

            if (!tokenizeValue()) {
                return false;
            }
            object.nChildren += 1;

            discardWhitespace();

            if (source.readableBytes() == 0) {
                return false;
            }
            b = source.peekByte();
            if (b == ',') {
                source.skipByte();
                continue;
            }
            if (b != '}') {
                saveCandidateAsError("Expected `,` or `}`");
                return false;
            }
            source.skipByte();
            return true;
        }
    }

    private boolean tokenizeArray() {
        final var array = collectCandidate(JsonType.ARRAY);

        discardWhitespace();

        if (source.readableBytes() == 0) {
            return false;
        }
        byte b = source.peekByte();
        if (b == ']') {
            source.skipByte();
            return true;
        }

        while (true) {
            if (!tokenizeValue()) {
                return false;
            }
            array.nChildren += 1;

            discardWhitespace();

            b = source.readByteOrZero();
            if (b == ',') {
                continue;
            }
            if (b != ']') {
                saveCandidateAsError("Expected `,` or `]`");
                return false;
            }
            return true;
        }
    }

    private boolean tokenizeString() {
        while (source.readableBytes() > 0) {
            byte b = source.readByte();
            if (b == '\"') {
                final var token = collectCandidate(JsonType.STRING);

                // Remove leading and trailing double quotes `"` from token.
                token.begin += 1;
                token.end -= 1;

                return true;
            }
            if (b == '\\') {
                if (source.readableBytes() == 0) {
                    return false;
                }
                if (source.readByte() == 'u') {
                    if (source.readableBytes() < 4) {
                        return false;
                    }
                    source.skipBytes(4);
                }
            }
        }
        return false;
    }

    private boolean tokenizeNumber() {
        number:
        while (source.readableBytes() > 0) {
            switch (source.peekByte()) {
            case '\0':
            case ',':
            case '}':
            case ']':
            case '\t':
            case '\r':
            case '\n':
            case ' ':
                break number;

            default:
                source.skipByte();
                break;
            }
        }
        collectCandidate(JsonType.NUMBER);
        return true;
    }

    private boolean tokenizeTrue() {
        error:
        {
            if (source.readByteOrZero() != 'r') {
                break error;
            }
            if (source.readByteOrZero() != 'u') {
                break error;
            }
            if (source.readByteOrZero() != 'e') {
                break error;
            }
            collectCandidate(JsonType.TRUE);
            return true;
        }
        expandAndSaveCandidateAsError("Bad true token");
        return false;
    }

    private boolean tokenizeFalse() {
        error:
        {
            if (source.readByteOrZero() != 'a') {
                break error;
            }
            if (source.readByteOrZero() != 'l') {
                break error;
            }
            if (source.readByteOrZero() != 's') {
                break error;
            }
            if (source.readByteOrZero() != 'e') {
                break error;
            }
            collectCandidate(JsonType.FALSE);
            return true;
        }
        expandAndSaveCandidateAsError("Bad false token");
        return false;
    }

    private boolean tokenizeNull() {
        error:
        {
            if (source.readByteOrZero() != 'u') {
                break error;
            }
            if (source.readByteOrZero() != 'l') {
                break error;
            }
            if (source.readByteOrZero() != 'l') {
                break error;
            }
            collectCandidate(JsonType.NULL);
            return true;
        }
        expandAndSaveCandidateAsError("Bad null token");
        return false;
    }

    private void expandAndSaveCandidateAsError(final String message) {
        expand:
        while (source.readableBytes() > 0) {
            switch (source.peekByte()) {
            case '\0':
            case ',':
            case '}':
            case ']':
            case '\t':
            case '\r':
            case '\n':
            case ' ':
               break expand;

            default:
                source.skipByte();
                break;
            }
        }
        saveCandidateAsError(message);
    }
}
