package eu.arrowhead.kalix.http.client;

import java.time.Instant;

/**
 * An arbitrary authentication token that can be added to HTTP requests.
 */
public class HttpToken {
    private final String contents;
    private final Instant expiration;

    /**
     * Creates new token from given {@code contents} and {@code expiration}.
     *
     * @param contents   Raw token contents.
     * @param expiration Date and time at which token expires.
     */
    public HttpToken(final String contents, final Instant expiration) {
        this.contents = contents;
        this.expiration = expiration;
    }

    /**
     * @return Raw token.
     */
    public String contents() {
        return contents;
    }

    /**
     * @return Date and time at which this token expires.
     */
    public Instant expiration() {
        return expiration;
    }

    /**
     * @return Whether or not this token has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiration);
    }
}
