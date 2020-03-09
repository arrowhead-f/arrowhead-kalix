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
    JSON("application/json"),
    ;

    private final String mediaType;

    DataEncoding(final String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * @return RFC 6838 compatible media type corresponding to this encoding.
     * @see <a href="https://tools.ietf.org/html/rfc6838">RFC 6838</a>
     */
    public String asMediaType() {
        return mediaType;
    }
}
