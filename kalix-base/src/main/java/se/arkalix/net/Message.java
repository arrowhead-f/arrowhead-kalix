package se.arkalix.net;

import se.arkalix.codec.CodecType;

import java.util.Optional;

/**
 * Base interface for network messages.
 */
public interface Message {
    /**
     * Codec used to encode the contents of this message, if specified.
     *
     * @return Message codec, if specified.
     * @throws MessageCodecMisspecified If a message codec is specified in the
     *                                  message, but it cannot be interpreted.
     */
    Optional<CodecType> codecType();
}
