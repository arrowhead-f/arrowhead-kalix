package se.arkalix.codec;

/**
 * Represents the rejection of a codec registration due to another codec
 * with the same name already being registered.
 */
public class CodecAlreadyRegistered extends CodecException {
    /**
     * Creates new exception.
     *
     * @param codecType Codec type preventing the registration of new codec
     *                  with the same name.
     */
    public CodecAlreadyRegistered(final CodecType codecType) {
        super(codecType, null, null, true, true);
    }

    @Override
    public String toString() {
        return "The codec " + codec() + " is already registered";
    }
}
