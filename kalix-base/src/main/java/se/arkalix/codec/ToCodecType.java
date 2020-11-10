package se.arkalix.codec;

/**
 * Any type that can be converted into an {@link CodecType}.
 */
public interface ToCodecType {
    /**
     * Converts this object to an {@link CodecType} instance.
     *
     * @return New, or cached, codec instance.
     */
    CodecType toCodecType();
}
