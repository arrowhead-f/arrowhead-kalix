package se.arkalix.net.http._internal;

import se.arkalix.net.Encoding;
import se.arkalix.util.annotation.Internal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Various utility methods related to HTTP and Media/MIME Types.
 */
@Internal
public class HttpMediaTypes {
    private HttpMediaTypes() {}

    /**
     * Attempts to convert given "content-type" into an encoding descriptor.
     * <p>
     * While RFC 6838, Section 4.2.8 outlines requirements for how media types
     * should be structured such as to make their encodings more apparent, the
     * RFC in question is only categorized as "BEST CURRENT PRACTICE", which
     * means that it becomes practically impossible to avoid using some
     * guesswork when writing a routine such as this. This particular
     * implementation treats the subtype suffix or the subtype itself as an
     * encoding name. No other parts of the "content-type" is regarded. The
     * method returns an empty optional <i>only</i> if the given "content-type"
     * is either empty or invalid.
     * <p>
     * While Section 4.2.8 of RFC 6838 designates "+" as suffix delimiter, the
     * media types using the EXI encoding, such as SenML, seem to use "-" as
     * delimiter, at least if looking at the IANA <i>Media Types</i> registry.
     * For this reason, both "+" and "-" are considered valid suffix
     * designators. For example, both the {@code "application/json"} and {@code
     * "application/senml+json"} content types will result in {@link
     * Encoding#JSON} being returned.
     * <p>
     * More about HTTP content types can be read in RFC 7231, Section 3.1.1.5.
     *
     * @param contentType A content type, assumed to follow the specification
     *                    for the "content-type" HTTP header field.
     * @return An encoding descriptor, if any such can be produced from the
     * given content type.
     * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2.8">RFC 6838, Section 4.2.8</a>
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">RFC 7231, Section 3.1.1.5</a>
     * @see <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types</a>
     */
    public static Optional<Encoding> encodingFromContentType(final String contentType) {
        if (contentType == null || contentType.length() == 0) {
            return Optional.empty();
        }

        int c0 = 0, c1;

        // Find end of media type.
        c1 = contentType.indexOf(';');
        if (c1 == -1) {
            c1 = contentType.length();
        }

        // Trim trailing and leading whitespace.
        while (c1 != 0) {
            if (contentType.charAt(--c1) > ' ') {
                c1 += 1;
                break;
            }
        }
        while (c0 < c1) {
            if (contentType.charAt(c0) > ' ') {
                break;
            }
            c0 += 1;
        }

        if (c0 == c1) {
            return Optional.empty();
        }

        // Skip until beginning of subtype.
        while (c0 < c1) {
            final var c = contentType.charAt(c0);
            c0 += 1;
            if (c == '/') {
                break;
            }
        }

        // If a suffix is present in subtype, skip everything else.
        for (var cx = c1; cx > c0; ) {
            final var c = contentType.charAt(--cx);
            if (c == '+' || c == '-') { // EXI uses '-' as suffix delimiter.
                c0 = cx + 1;
                break;
            }
        }

        return Optional.of(Encoding.getOrCreate(contentType.substring(c0, c1)));
    }

    /**
     * Determines which, if any, out of provided {@code encodings} could be
     * used to decode and/or encode objects from/to the media type specified in
     * {@code contentType}, which is assumed to be an HTTP "content-type"
     * header field value.
     * <p>
     * While RFC 6838, Section 4.2.8 outlines requirements for how media types
     * should be structured such as to make their encodings more apparent, the
     * RFC in question is only categorized as "BEST CURRENT PRACTICE", which
     * means that it becomes practically impossible to avoid using some
     * guesswork when writing a routine such as this. This particular
     * implementation will require that a media type matching an encoding has a
     * subtype <i>or suffix</i> equal to the name of the encoding in question.
     * Types are ignored.
     * <p>
     * While Section 4.2.8 of RFC 6838 designates designates "+" as suffix
     * delimiter, the media types using the EXI encoding, such as SenML, seem
     * to use "-" as delimiter, at least if looking at the IANA <i>Media
     * Types</i> registry. For this reason, both "+" and "-" are considered
     * valid suffix designators. For example the {@link Encoding#JSON
     * JSON} encoding will match both the {@code "application/json"} and {@code
     * "application/senml+json"} media types. Furthermore, the {@link
     * Encoding#EXI EXI} encoding will match {@code
     * "application/exi"}, which is non-standard at the time of writing, as
     * well as {@code "application/senml-exi"}.
     * <p>
     * More about HTTP content types can be read in RFC 7231, Section 3.1.1.5.
     *
     * @param encodings   A list of candidate encodings.
     * @param contentType A content type, assumed to follow the specification
     *                    for the "content-type" HTTP header field.
     * @return A compatible candidate encoding, if any such exists.
     * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2.8">RFC 6838, Section 4.2.8</a>
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">RFC 7231, Section 3.1.1.5</a>
     * @see <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types</a>
     */
    public static Optional<Encoding> findEncodingCompatibleWithContentType(
        final List<Encoding> encodings,
        final String contentType
    ) {
        Objects.requireNonNull(encodings, "Expected encodings");
        if (encodings.size() == 0) {
            throw new IllegalArgumentException("Expected encodings.size() > 0");
        }
        if (contentType == null || contentType.length() == 0) {
            return Optional.empty();
        }

        int c0 = 0, c1;

        // Find end of media type.
        c1 = contentType.indexOf(';');
        if (c1 == -1) {
            c1 = contentType.length();
        }

        // Trim trailing and leading whitespace.
        while (c1 != 0) {
            if (contentType.charAt(--c1) > ' ') {
                c1 += 1;
                break;
            }
        }
        while (c0 < c1) {
            if (contentType.charAt(c0) > ' ') {
                break;
            }
            c0 += 1;
        }

        if (c0 == c1) {
            return Optional.of(encodings.get(0));
        }

        // Skip until beginning of subtype.
        while (c0 < c1) {
            final var c = contentType.charAt(c0);
            c0 += 1;
            if (c == '/') {
                break;
            }
        }

        // If a suffix is present in subtype, skip everything else.
        for (var cx = c1; cx > c0; ) {
            final var c = contentType.charAt(--cx);
            if (c == '+' || c == '-') { // EXI uses '-' as suffix delimiter.
                c0 = cx + 1;
                break;
            }
        }

        // Find an encoding that matches the subtype or suffix ignoring case.
        nextEncoding:
        for (final var encoding : encodings) {
            final var name = encoding.name();
            if (c1 - c0 != name.length()) {
                continue;
            }
            var cx = c0;
            var nx = 0;
            while (cx < c1) {
                final var cc = Character.toLowerCase(contentType.charAt(cx));
                final var nc = Character.toLowerCase(name.charAt(nx));
                if (cc != nc) {
                    continue nextEncoding;
                }
                cx += 1;
                nx += 1;
            }
            return Optional.of(encoding);
        }
        return Optional.empty();
    }

    /**
     * Determines which, if any, out of provided {@code encodings} could be
     * used to decode and/or encode objects from/to any one media type
     * specified in any out of the given {@code acceptHeaders}, which are
     * assumed to be a list of HTTP "header" field values.
     * <p>
     * This method uses a similar matching schema as
     * {@link #findEncodingCompatibleWithContentType(List, String)}, with the
     * exception that it also respects the presence of wildcards ({@code *})
     * and q-factors, which are ignored, in the accept headers.
     * <p>
     * The accept header syntax is specified in RFC 7231, Section 5.3.2.
     *
     * @param encodings     A list of candidate encodings.
     * @param acceptHeaders A content type, assumed to follow the specification
     *                      for the "content-type" HTTP header field.
     * @return A compatible candidate encoding, if any such exists.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">RFC 7231, Section 5.3.2</a>
     */
    public static Optional<Encoding> findEncodingCompatibleWithAcceptHeaders(
        final List<Encoding> encodings,
        final List<String> acceptHeaders
    ) {
        Objects.requireNonNull(encodings, "Expected encodings");
        if (encodings.size() == 0) {
            throw new IllegalArgumentException("Expected encodings.size() > 0");
        }
        if (acceptHeaders == null || acceptHeaders.size() == 0) {
            return Optional.empty();
        }

        for (final var acceptHeader : acceptHeaders) {
            final var encoding = findEncodingCompatibleWithAcceptHeader(encodings, acceptHeader);
            if (encoding != null) {
                return Optional.of(encoding);
            }
        }

        return Optional.empty();
    }

    private static Encoding findEncodingCompatibleWithAcceptHeader(
        final List<Encoding> encodings,
        final String acceptHeader
    ) {
        int a0 = 0, a1, a2 = 0;
        final int a3 = acceptHeader.length();

        for (char c; a2 < a3; a0 = a2) {

            // Find end of current media type and end of current entry.
            foundSemicolon:
            {
                while (a2 < a3) {
                    c = acceptHeader.charAt(a2++);
                    if (c == ';') {
                        a1 = a2 - 1;
                        while (a2 < a3) {
                            c = acceptHeader.charAt(a2++);
                            if (c == ',') { break; }
                        }
                        break foundSemicolon;
                    }
                    if (c == ',') {
                        a1 = a2 - 1;
                        break foundSemicolon;
                    }
                }
                a1 = a2;
            }

            // Trim trailing and leading whitespace from media type.
            while (a1 != 0) {
                if (acceptHeader.charAt(--a1) > ' ') {
                    a1 += 1;
                    break;
                }
            }
            while (a0 < a1) {
                if (acceptHeader.charAt(a0) > ' ') {
                    break;
                }
                a0 += 1;
            }

            // Skip until beginning of subtype.
            while (a0 < a1) {
                c = acceptHeader.charAt(a0);
                a0 += 1;
                if (c == '/') {
                    break;
                }
            }

            // If subtype is "*", return the first of the provided encodings.
            if (a0 + 1 == a1 && acceptHeader.charAt(a0) == '*') {
                return encodings.get(0);
            }

            // If a suffix is present in subtype, skip everything else.
            for (var ax = a1; ax > a0; ) {
                c = acceptHeader.charAt(--ax);
                if (c == '+' || c == '-') { // EXI uses '-' as suffix delimiter.
                    a0 = ax + 1;
                    break;
                }
            }

            // Find an encoding that matches the subtype or suffix ignoring case.
            nextEncoding:
            for (final var encoding : encodings) {
                final var name = encoding.name();
                if (a1 - a0 != name.length()) {
                    continue;
                }
                var ax = a0;
                var nx = 0;
                while (ax < a1) {
                    final var ac = Character.toLowerCase(acceptHeader.charAt(ax));
                    final var nc = Character.toLowerCase(name.charAt(nx));
                    if (ac != nc) {
                        continue nextEncoding;
                    }
                    ax += 1;
                    nx += 1;
                }
                return encoding;
            }
        }
        return null;
    }
}
