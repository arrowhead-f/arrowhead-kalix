package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;

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
        if (encodings.length == 0) {
            throw new IllegalArgumentException("Expected encodings.length > 0");
        }
        useDefault:
        {
            if (contentType == null) {
                break useDefault;
            }

            // Find end of media type.
            var c1 = contentType.indexOf(';');
            if (c1 == -1) {
                c1 = contentType.length();
            }

            // Trim trailing and leading whitespace.
            while (c1-- != 0) {
                if (contentType.charAt(c1) > ' ') {
                    c1 += 1;
                    break;
                }
            }

            var c0 = 0;
            while (c0++ < c1) {
                if (contentType.charAt(c0) > ' ') {
                    c0 -= 1;
                    break;
                }
            }

            // Use default encoding if media type is empty.
            if (c0 == c1) {
                break useDefault;
            }

            final var mediaType = contentType.substring(c0, c1);
            for (final var encoding : encodings) {
                if (encoding.isUsedByMediaType(mediaType)) {
                    return Optional.of(encoding);
                }
            }
            return Optional.empty();
        }
        return Optional.of(encodings[0]);
    }
}
