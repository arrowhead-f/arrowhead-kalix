package se.arkalix.codec;

import java.util.Objects;

/**
 * Represents the failure to carry out an operation related to an {@link
 * CodecType}.
 */
public abstract class CodecException extends RuntimeException {
    private final CodecType codecType;

    /**
     * Constructs a new codec exception with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled
     * or disabled.
     *
     * @param codecType          Codec subject to this exception.
     * @param message            Detail message, or {@code null}.
     * @param cause              Cause, or {@code null}.
     * @param enableSuppression  Whether or not suppression is enabled or
     *                           disabled for this exception.
     * @param writableStackTrace Whether or not the stack trace of this
     *                           exception should be writable.
     */
    protected CodecException(
        final CodecType codecType,
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.codecType = Objects.requireNonNull(codecType, "codec");
    }

    /**
     * Gets codec subject of this exception.
     *
     * @return Subject codec.
     */
    public CodecType codec() {
        return codecType;
    }
}
