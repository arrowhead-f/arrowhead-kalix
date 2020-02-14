package eu.arrowhead.kalix.http;

/**
 * HTTP request methods.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-4">RFC 7231 Section 4</a>
 * @see <a href="https://tools.ietf.org/html/rfc5789">RFC 5789</a>
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    OPTIONS,
    CONNECT,
    PATCH,
    TRACE,
    ;

    /**
     * @return Whether or not this method is "safe" as defined in RFC 7231 Section 4.2.1.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.2.1">RFC 7231 Section 4.2.1</a>
     */
    public boolean isSafe() {
        return this == GET || this == HEAD || this == OPTIONS || this == TRACE;
    }

    /**
     * @return Whether or not this method is "idempotent" as defined in RFC 7231 Section 4.2.2.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-4.2.2">RFC 7231 Section 4.2.2</a>
     */
    public boolean isIdempotent() {
        return this == PUT || this == DELETE || isSafe();
    }
}










