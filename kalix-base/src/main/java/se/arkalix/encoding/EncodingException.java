package se.arkalix.encoding;

import java.util.Objects;

/**
 * Represents the failure to carry out an operation related to an {@link
 * Encoding}.
 */
public abstract class EncodingException extends RuntimeException {
    private final Encoding encoding;

    /**
     * Constructs a new encoding exception with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled
     * or disabled.
     *
     * @param message            Detail message, or {@code null}.
     * @param cause              Cause, or {@code null}.
     * @param enableSuppression  Whether or not suppression is enabled or
     *                           disabled for this exception.
     * @param writableStackTrace Whether or not the stack trace of this
     *                           exception should be writable.
     */
    protected EncodingException(
        final Encoding encoding,
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.encoding = Objects.requireNonNull(encoding, "encoding");
    }

    /**
     * Gets encoding subject of this exception.
     *
     * @return Subject encoding.
     */
    public Encoding encoding() {
        return encoding;
    }
}
