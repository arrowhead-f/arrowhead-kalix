package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.Format;
import eu.arrowhead.kalix.dto.ReadException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class JSONReader {
    private final ByteBuffer source;
    private final ArrayList<JSONToken> tokens;

    private int p0;
    private ReadException error = null;

    private JSONReader(final ByteBuffer source) {
        this.source = source;
        this.tokens = new ArrayList<>(source.remaining() / 16);
        this.p0 = source.position();
    }

    public static List<JSONToken> tokenize(final ByteBuffer source) throws ReadException {
        final var tokenizer = new JSONReader(source);
        if (tokenizer.tokenizeRoot()) {
            return tokenizer.tokens;
        }
        throw tokenizer.error;
    }

    private JSONToken collectCandidate(final JSONType type) {
        final var token = new JSONToken(type, p0, source.position(), 0);
        tokens.add(token);
        discardCandidate();
        return token;
    }

    private void discardCandidate() {
        p0 = source.position();
    }

    private void saveCandidateAsError(final String message) {
        final var buffer = new byte[source.position() - p0];
        source.position(p0).get(buffer);
        error = new ReadException(Format.JSON, message, new String(buffer, StandardCharsets.UTF_8), p0);
    }

    private void discardWhitespace() {
        for (byte b; ; ) {
            b = peek();
            if (b != '\t' && b != '\r' && b != '\n' && b != ' ') {
                discardCandidate();
                break;
            }
            skip1();
        }
    }

    private byte next() {
        return source.position() < source.limit()
            ? source.get()
            : 0;
    }

    private byte peek() {
        return source.get(source.position());
    }

    private void skip1() {
        if (source.position() < source.limit()) {
            source.position(source.position() + 1);
        }
    }

    private void skip4() {
        if (source.position() + 3 < source.limit()) {
            source.position(source.position() + 4);
        }
        else {
            source.position(source.limit());
        }
    }

    private boolean tokenizeRoot() {
        discardWhitespace();

        switch (next()) {
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

        switch (next()) {
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
        final var object = collectCandidate(JSONType.OBJECT);

        discardWhitespace();

        var b = peek();
        if (b == '}') {
            skip1();
            return true;
        }

        while (true) {
            discardWhitespace();

            b = next();
            if (b != '\"') {
                saveCandidateAsError("Object key must be string");
                return false;
            }
            if (!tokenizeString()) {
                return false;
            }

            discardWhitespace();

            b = next();
            if (b != ':') {
                saveCandidateAsError("Object key not followed by colon");
                return false;
            }

            if (!tokenizeValue()) {
                return false;
            }
            object.nChildren += 1;

            discardWhitespace();

            b = peek();
            if (b == ',') {
                skip1();
                continue;
            }
            if (b != '}') {
                saveCandidateAsError("Expected `,` or `}`");
                return false;
            }
            skip1();
            return true;
        }
    }

    private boolean tokenizeArray() {
        final var array = collectCandidate(JSONType.ARRAY);

        discardWhitespace();

        byte b = peek();
        if (b == ']') {
            skip1();
            return true;
        }

        while (true) {
            if (!tokenizeValue()) {
                return false;
            }
            array.nChildren += 1;

            discardWhitespace();

            b = next();
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
        while (true) {
            byte b = next();
            if (b == '\"') {
                final var token = collectCandidate(JSONType.STRING);

                // Remove leading and trailing double quotes `"` from token.
                token.begin += 1;
                token.end -= 1;

                return true;
            }
            if (b == '\\') {
                if (next() == 'u') {
                    skip4();
                }
            }
        }
    }

    private boolean tokenizeNumber() {
        while (true) {
            switch (peek()) {
            case '\0':
            case ',':
            case '}':
            case ']':
            case '\t':
            case '\r':
            case '\n':
            case ' ':
                collectCandidate(JSONType.NUMBER);
                return true;

            default:
                skip1();
                break;
            }
        }
    }

    private boolean tokenizeTrue() {
        error:
        {
            if (next() != 'r') {
                break error;
            }
            if (next() != 'u') {
                break error;
            }
            if (next() != 'e') {
                break error;
            }
            collectCandidate(JSONType.TRUE);
            return true;
        }
        expandAndSaveCandidateAsError("Bad true token");
        return false;
    }

    private boolean tokenizeFalse() {
        error:
        {
            if (next() != 'a') {
                break error;
            }
            if (next() != 'l') {
                break error;
            }
            if (next() != 's') {
                break error;
            }
            if (next() != 'e') {
                break error;
            }
            collectCandidate(JSONType.FALSE);
            return true;
        }
        expandAndSaveCandidateAsError("Bad false token");
        return false;
    }

    private boolean tokenizeNull() {
        error:
        {
            if (next() != 'u') {
                break error;
            }
            if (next() != 'l') {
                break error;
            }
            if (next() != 'l') {
                break error;
            }
            collectCandidate(JSONType.NULL);
            return true;
        }
        expandAndSaveCandidateAsError("Bad null token");
        return false;
    }

    private void expandAndSaveCandidateAsError(final String message) {
        while (true) {
            switch (peek()) {
            case '\0':
            case ',':
            case '}':
            case ']':
            case '\t':
            case '\r':
            case '\n':
            case ' ':
                saveCandidateAsError(message);
                return;

            default:
                skip1();
                break;
            }
        }
    }
}
