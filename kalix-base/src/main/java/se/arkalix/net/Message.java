package se.arkalix.net;

import se.arkalix.encoding.Encoding;

import java.util.Optional;

/**
 * Base interface for network messages.
 */
public interface Message {
    /**
     * Encoding used to encode the contents of this message, if specified.
     *
     * @return Message encoding, if specified.
     * @throws MessageEncodingMisspecified If a message encoding is specified in the
     *                                message, but it cannot be interpreted.
     */
    Optional<Encoding> encoding();
}
