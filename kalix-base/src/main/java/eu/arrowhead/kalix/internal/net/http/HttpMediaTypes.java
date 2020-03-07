package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Various utility methods related to HTTP and Media/MIME Types.
 */
public class HttpMediaTypes {
    private HttpMediaTypes() {}

    /**
     * Determines which, if any, out of provided {@code encodings} could be
     * used to decode and/or encode objects from/to the media type specified in
     * {@code contentType}.
     *
     * @param encodings   A list of candidate encodings.
     * @param contentType A content type, assumed to follow the specification
     *                    for the "content-type" HTTP header field.
     * @return A compatible candidate encoding, if any such exists.
     */
    public static Optional<EncodingDescriptor> findEncodingCompatibleWith(
        final EncodingDescriptor[] encodings,
        final String contentType)
    {
        Objects.requireNonNull(encodings, "Expected encodings");
        if (encodings.length == 0 || contentType == null || contentType.length() < 3) {
            return Optional.empty();
        }
        var c0 = 0;

        // Find end of media type.
        var c1 = contentType.indexOf(';');
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
            if (contentType.charAt(c0++) > ' ') {
                c0 -= 1;
                break;
            }
        }

        // Use default encoding if media type is empty.
        if (c0 == c1) {
            return Optional.empty();
        }

        final var mediaType = contentType.substring(0, c1);
        for (final var encoding : encodings) {
            if (encoding.asMediaTypePattern().matcher(mediaType).find(c0)) {
                return Optional.of(encoding);
            }
        }
        return Optional.empty();
    }

    public static Optional<EncodingDescriptor> findEncodingCompatibleWith(
        final EncodingDescriptor[] encodings,
        final List<String> acceptHeaders)
    {
        Objects.requireNonNull(encodings, "Expected encodings");
        Objects.requireNonNull(acceptHeaders, "Expected acceptHeaders");
        if (encodings.length == 0 || acceptHeaders.size() == 0) {
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

    private static EncodingDescriptor findEncodingCompatibleWithAcceptHeader(
        final EncodingDescriptor[] encodings,
        final String acceptHeader)
    {
        int a0 = 0, a1 = 0, a2 = 0;
        final int a3 = acceptHeader.length();

        for (char c; a2 < a3; a0 = a2) {

            // Find end of current media type and of current entry.
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
                if (acceptHeader.charAt(a0++) > ' ') {
                    a0 -= 1;
                    break;
                }
            }

            // Ensure type is either "*" or "application".
            if (acceptHeader.charAt(a0) == '*' && acceptHeader.charAt(a0 + 1) == '/') {
                a0 += 2;
            }
            else if (acceptHeader.startsWith("application/", a0)) {
                a0 += 12;
            }
            else {
                continue;
            }

            // Ensure subtype is either "*" or name of one out of encodings.
            if (a0 + 1 == a1 && acceptHeader.charAt(a0) == '*') {
                // Any encoding will do. Return the first.
                return encodings[0];
            }
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
