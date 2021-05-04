package se.arkalix.codec.json._internal;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderReadUnexpectedToken;
import se.arkalix.codec.json.JsonType;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.util.annotation.Internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

@Internal
@SuppressWarnings("unused")
public final class JsonTokenizer {
    private final BufferReader reader;
    private final ArrayList<JsonToken> tokens;

    private int p0;
    private DecoderReadUnexpectedToken error = null;

    private JsonTokenizer(final BufferReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.tokens = new ArrayList<>(reader.readableBytes() / 16);
        this.p0 = reader.readOffset();
    }

    public static JsonTokenBuffer tokenize(final BufferReader reader) {
        final var tokenizer = new JsonTokenizer(reader);
        if (tokenizer.tokenizeValue()) {
            return new JsonTokenBuffer(tokenizer.tokens, reader);
        }
        throw tokenizer.error;
    }

    private JsonToken collectCandidate(final JsonType type) {
        final var token = new JsonToken(type, p0, reader.readOffset(), 0);
        tokens.add(token);
        discardCandidate();
        return token;
    }

    private void discardCandidate() {
        p0 = reader.readOffset();
    }

    private void saveCandidateAsError(final String message) {
        final var buffer = new byte[reader.readOffset() - p0];
        reader.getAt(p0, buffer);
        error = new DecoderReadUnexpectedToken(
            CodecType.JSON,
            reader,
            new String(buffer, StandardCharsets.UTF_8),
            p0,
            message
        );
    }

    private void discardWhitespace() {
        for (byte b; reader.readableBytes() > 0; ) {
            b = reader.peekS8();
            if (b != '\t' && b != '\r' && b != '\n' && b != ' ') {
                break;
            }
            reader.skip(1);
        }
        discardCandidate();
    }

    private boolean tokenizeValue() {
        discardWhitespace();

        switch (readByteOrZero()) {
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
            tokenizeNumber();
            return true;

        case 't': return tokenizeTrue();
        case 'f': return tokenizeFalse();
        case 'n': return tokenizeNull();

        default:
            saveCandidateAsError("unexpected character");
            return false;
        }
    }

    private boolean tokenizeObject() {
        final var object = collectCandidate(JsonType.OBJECT);

        discardWhitespace();

        if (reader.readableBytes() == 0) {
            saveCandidateAsError("unexpected end of object");
            return false;
        }
        var b = reader.peekS8();
        if (b == '}') {
            reader.skip(1);
            return true;
        }

        while (true) {
            discardWhitespace();

            b = readByteOrZero();
            if (b != '\"') {
                saveCandidateAsError("object key must be string");
                return false;
            }
            if (!tokenizeString()) {
                return false;
            }

            discardWhitespace();

            b = readByteOrZero();
            if (b != ':') {
                saveCandidateAsError("object key not followed by colon");
                return false;
            }

            if (!tokenizeValue()) {
                return false;
            }
            object.nChildren += 1;

            discardWhitespace();

            if (reader.readableBytes() == 0) {
                saveCandidateAsError("unexpected end of object");
                return false;
            }
            b = reader.peekS8();
            if (b == ',') {
                reader.skip(1);
                continue;
            }
            if (b != '}') {
                saveCandidateAsError("expected `,` or `}`");
                return false;
            }
            reader.skip(1);
            return true;
        }
    }

    private boolean tokenizeArray() {
        final var array = collectCandidate(JsonType.ARRAY);

        discardWhitespace();

        if (reader.readableBytes() == 0) {
            saveCandidateAsError("unexpected end of array");
            return false;
        }
        byte b = reader.peekS8();
        if (b == ']') {
            reader.skip(1);
            return true;
        }

        while (true) {
            if (!tokenizeValue()) {
                return false;
            }
            array.nChildren += 1;

            discardWhitespace();

            b = readByteOrZero();
            if (b == ',') {
                continue;
            }
            if (b != ']') {
                saveCandidateAsError("expected `,` or `]`");
                return false;
            }
            return true;
        }
    }

    private boolean tokenizeString() {
        while (reader.readableBytes() > 0) {
            byte b = reader.readS8();
            if (b == '\"') {
                final var token = collectCandidate(JsonType.STRING);

                // Remove leading and trailing double quotes `"` from token.
                token.begin += 1;
                token.end -= 1;

                return true;
            }
            if (b == '\\') {
                if (reader.readableBytes() == 0) {
                    break;
                }
                if (reader.readS8() == 'u') {
                    if (reader.readableBytes() < 4) {
                        break;
                    }
                    reader.skip(4);
                }
            }
        }
        saveCandidateAsError("unexpected end of string");
        return false;
    }

    private void tokenizeNumber() {
        number:
        while (reader.readableBytes() > 0) {
            switch (reader.peekS8()) {
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
                reader.skip(1);
                break;
            }
        }
        collectCandidate(JsonType.NUMBER);
    }

    private boolean tokenizeTrue() {
        error:
        {
            if (readByteOrZero() != 'r') {
                break error;
            }
            if (readByteOrZero() != 'u') {
                break error;
            }
            if (readByteOrZero() != 'e') {
                break error;
            }
            collectCandidate(JsonType.TRUE);
            return true;
        }
        expandAndSaveCandidateAsError("bad true token");
        return false;
    }

    private boolean tokenizeFalse() {
        error:
        {
            if (readByteOrZero() != 'a') {
                break error;
            }
            if (readByteOrZero() != 'l') {
                break error;
            }
            if (readByteOrZero() != 's') {
                break error;
            }
            if (readByteOrZero() != 'e') {
                break error;
            }
            collectCandidate(JsonType.FALSE);
            return true;
        }
        expandAndSaveCandidateAsError("bad false token");
        return false;
    }

    private boolean tokenizeNull() {
        error:
        {
            if (readByteOrZero() != 'u') {
                break error;
            }
            if (readByteOrZero() != 'l') {
                break error;
            }
            if (readByteOrZero() != 'l') {
                break error;
            }
            collectCandidate(JsonType.NULL);
            return true;
        }
        expandAndSaveCandidateAsError("bad null token");
        return false;
    }

    private void expandAndSaveCandidateAsError(final String message) {
        expand:
        while (reader.readableBytes() > 0) {
            switch (reader.peekS8()) {
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
                reader.skip(1);
                break;
            }
        }
        saveCandidateAsError(message);
    }

    private byte readByteOrZero() {
        if (reader.readableBytes() > 0) {
            return reader.readS8();
        }
        else {
            return 0;
        }
    }
}
