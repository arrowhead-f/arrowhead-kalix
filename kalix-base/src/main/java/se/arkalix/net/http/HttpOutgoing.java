package se.arkalix.net.http;

import se.arkalix.net.MessageOutgoing;

import java.util.Optional;

/**
 * An outgoing HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpOutgoing<Self> extends HttpMessage, MessageOutgoing<Self> {
    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    Self header(final CharSequence name, final CharSequence value);

    /**
     * @return Currently set HTTP version, if any.
     */
    Optional<HttpVersion> version();

    /**
     * Sets HTTP version.
     * <p>
     * Note that only HTTP/1.0 and HTTP/1.1 are supported by this version of
     * Arrowhead Kalix. If no version is set, HTTP/1.1 is used by default.
     *
     * @param version Desired HTTP version.
     * @return This request.
     */
    Self version(final HttpVersion version);
}
