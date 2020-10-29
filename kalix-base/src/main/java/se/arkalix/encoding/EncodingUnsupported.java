package se.arkalix.encoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Represents the inability to encode or decode some string of bytes, caused by
 * an unsupported encoding being specified.
 */
public class EncodingUnsupported extends EncodingException {
    private static final Logger logger = LoggerFactory.getLogger(EncodingUnsupported.class);

    private final Encoding encoding;

    /**
     * Creates new exception.
     *
     * @param encoding Unsupported encoding.
     */
    public EncodingUnsupported(final Encoding encoding) {
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
