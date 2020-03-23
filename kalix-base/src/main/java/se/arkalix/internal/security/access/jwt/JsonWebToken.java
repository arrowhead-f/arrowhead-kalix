package se.arkalix.internal.security.access.jwt;

import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.json.JsonReader;
import se.arkalix.internal.dto.binary.ByteArrayReader;

import static se.arkalix.dto.DtoEncoding.JSON;
import static se.arkalix.dto.json.JsonType.OBJECT;
import static se.arkalix.dto.json.JsonType.STRING;

public final class JsonWebToken {
    // TODO: Implement.

    private static JoseHeader parseHeader(final ByteArrayReader byteArrayReader) throws DtoReadException {
        final var header = new JoseHeader();

        final var reader = JsonReader.tokenize(byteArrayReader);
        final var source = reader.source();

        var token = reader.next();
        if (token.type() != OBJECT) {
            throw new DtoReadException(JSON, "Expected object", token.readString(source), 0);
        }
        final var n1 = token.nChildren();
        for (var n0 = 0; n0 < n1; ++n0) {

            final var key = reader.next();
            final var key0 = key.readString(source);
            if (key.type() != STRING) {
                throw new DtoReadException(JSON, "Expected string", key0, key.begin());
            }

            final var value = reader.next();
            final var value0 = value.readString(source);
            if (value.type() != STRING) {
                throw new DtoReadException(JSON, "Expected string", value0, value.begin());
            }

            switch (key0.toLowerCase()) {
            case "alg":
                header.alg = value0;
                break;

            case "cty":
                header.cty = value0;
                break;

            case "kid":
                header.kid = value0;
                break;

            case "typ":
                header.typ = value0;
                break;
            }
        }
        return header;
    }

    private static final class JoseHeader {
        public String alg;
        public String cty;
        public String kid;
        public String typ;
    }
}
