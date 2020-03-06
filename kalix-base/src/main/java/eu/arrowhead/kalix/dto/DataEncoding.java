package eu.arrowhead.kalix.dto;

/**
 * Enumerates the encodings that can be read and written by the Kalix DTO
 * package.
 */
public enum DataEncoding {
    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    JSON,

    /**
     * Some currently unsupported encoding.
     */
    UNSUPPORTED,
}
