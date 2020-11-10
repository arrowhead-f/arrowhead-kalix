package se.arkalix.codec;

/**
 * Represents the inability to encode or decode some string of bytes, caused by
 * an unsupported codec being specified.
 */
public class CodecUnsupported extends CodecException {
    /**
     * Creates new exception.
     *
     * @param codecType Unsupported codec.
     */
    public CodecUnsupported(final CodecType codecType) {
        super(codecType, null, null, true, true);
    }

    @Override
    public String getMessage() {
        return "The codec " + codec() + " is not supported by the " +
            "executed operation";
    }
}
