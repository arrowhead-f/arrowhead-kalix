package se.arkalix.encoding;

/**
 * Represents the rejection of an encoding registration due to another encoding
 * with the same name already being registered.
 */
public class EncodingAlreadyRegistered extends EncodingException {
    /**
     * Creates new exception.
     *
     * @param encoding Encoding preventing the registration of new encoding
     *                 with the same name.
     */
    public EncodingAlreadyRegistered(final Encoding encoding) {
        super(encoding, null, null, true, true);
    }

    @Override
    public String toString() {
        return "The encoding " + encoding() + " is already registered";
    }
}
