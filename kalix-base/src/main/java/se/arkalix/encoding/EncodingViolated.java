package se.arkalix.encoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Represents the inability to encode or decode some string of bytes, caused by
 * the string of bytes not conforming to the encoding it is expected to use.
 */
public class EncodingViolated extends EncodingException {
    private static final Logger logger = LoggerFactory.getLogger(EncodingViolated.class);

    private final Encoding encoding;

    /**
     * Creates new exception.
     *
     * @param encoding Encoding not adhered to.
     */
    public EncodingViolated(final Encoding encoding, final ) {
        super(null, null, true, logger.isDebugEnabled());
        this.encoding = Objects.requireNonNull(encoding);
    }

    /**
     * Gets name of unsupported encoding causing this exception to be thrown.
     *
     * @return Name of unsupported encoding.
     */
    public Encoding encoding() {
        return encoding;
    }

    @Override
    public String getMessage() {
        return "The encoding '\"" + encoding + "\"' is not supported for" +
            "the tried operation";
    }
}
