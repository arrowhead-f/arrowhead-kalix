package se.arkalix.net;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Base interface for network messages.
 */
public interface Message {
    /**
     * Character set used to encode the contents of this message, if specified.
     *
     * @return Message character set, if specified.
     */
    Optional<Charset> charset();

    /**
     * Encoding used to encode the contents of this message, if specified.
     *
     * @return Message encoding, if specified.
     */
    Optional<Encoding> encoding();
}
