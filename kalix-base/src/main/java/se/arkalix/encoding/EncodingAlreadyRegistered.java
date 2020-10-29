package se.arkalix.encoding;

import java.util.Objects;

/**
 * Represents the rejection of an encoding registration due to another encoding
 * with the same name already being registered.
 */
public class EncodingAlreadyRegistered extends EncodingException {
    private final Encoding encoding;

    /**
     * Creates new exception.
     *
     * @param encoding Encoding preventing the registration of new encoding
     *                 with the same name.
     */
    public EncodingAlreadyRegistered(final Encoding encoding) {
        super(null, null, true, true);
        this.encoding = Objects.requireNonNull(encoding, "encoding");
    }

    /**
     * Gets encoding that prevented the registration of an additional encoding
     * with the same name.
     *
     * @return Encoding preventing the registration of new encoding with the
     * same name.
     */
    public Encoding encoding() {
        return encoding;
    }

    @Override
    public String toString() {
        return "The encoding " + encoding + " is already registered";
    }
}
