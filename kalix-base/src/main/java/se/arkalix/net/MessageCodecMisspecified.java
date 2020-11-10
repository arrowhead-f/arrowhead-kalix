package se.arkalix.net;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * an codec being specified incorrectly.
 */
public class MessageCodecMisspecified extends MessageException {
    private final Object codec;

    /**
     * Creates new exception.
     *
     * @param message  Offending message.
     * @param codec Unexpectedly formatted codec.
     */
    public MessageCodecMisspecified(final Message message, final Object codec) {
        this(message, codec, null);
    }

    /**
     * Creates new exception.
     *
     * @param message  Offending message.
     * @param codec Unexpectedly formatted codec.
     * @param cause    Description of formatting error, if any.
     */
    public MessageCodecMisspecified(final Message message, final Object codec, final Throwable cause) {
        super(message, cause);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    /**
     * Gets codec causing this exception to be thrown.
     *
     * @return Invalid codec descriptor.
     */
    public Object codec() {
        return codec;
    }

    @Override
    public String getMessage() {
        return "Message codec \"" + codec + "\" has an unexpected " +
            "format; unable to encode or decode " + super.message();
    }
}
