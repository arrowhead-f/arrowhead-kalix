package se.arkalix.net;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * a codec being specified incorrectly.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
 */
public class MessageCodecMisspecified extends MessageException {
    private final Object codec;

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     * @param codec   Unexpectedly formatted codec.
     */
    public MessageCodecMisspecified(final Message message, final Object codec) {
        this(message, codec, null);
    }

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     * @param codec   Unexpectedly formatted codec.
     * @param cause   Description of formatting error, if any.
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
