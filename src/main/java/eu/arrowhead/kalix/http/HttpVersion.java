package eu.arrowhead.kalix.http;

/**
 * Denotes HTTP protocol version.
 */
public enum HttpVersion {
    /**
     * HTTP version 0.9.
     */
    HTTP_09,

    /**
     * HTTP version 1.0.
     */
    HTTP_10,

    /**
     * HTTP version 1.1.
     */
    HTTP_11,

    /**
     * HTTP version 2.0.
     */
    HTTP_20,

    /**
     * HTTP version 3.0.
     */
    HTTP_30,

    /**
     * Any other version.
     */
    UNKNOWN,
}
