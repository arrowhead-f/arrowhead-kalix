package se.arkalix.encoding;

/**
 * Represents the inability to encode or decode some string of bytes, caused by
 * an unsupported encoding being specified.
 */
public class EncodingUnsupported extends EncodingException {
    /**
     * Creates new exception.
     *
     * @param encoding Unsupported encoding.
     */
    public EncodingUnsupported(final Encoding encoding) {
        super(encoding, null, null, true, true);
    }

    @Override
    public String getMessage() {
        return "The encoding " + encoding() + " is not supported by the " +
            "executed operation";
    }
}
