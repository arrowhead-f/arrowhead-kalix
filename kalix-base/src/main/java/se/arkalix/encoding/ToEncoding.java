package se.arkalix.encoding;

/**
 * Any type that can be converted into an {@link Encoding}.
 */
public interface ToEncoding {
    /**
     * Converts this object to an {@link Encoding} instance.
     *
     * @return New, or cached, encoding instance.
     */
    Encoding toEncoding();
}
