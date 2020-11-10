package se.arkalix.net;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.CodecUnsupported;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * an unsupported codec being specified.
 */
public class MessageCodecUnsupported extends MessageException {
    private final CodecType codecType;

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     * @param cause   Unsupported codec exception causing this exception to
     *                be thrown.
     */
    public MessageCodecUnsupported(final Message message, final CodecUnsupported cause) {
        super(message, cause);
        this.codecType = Objects.requireNonNull(cause).codec();
    }

    /**
     * Gets name of unsupported codec causing this exception to be thrown.
     *
     * @return Unsupported codec descriptor.
     */
    public CodecType codec() {
        return codecType;
    }

    @Override
    public String getMessage() {
        return "Unsupported message codec \"" + codecType + "\"; unable " +
            "to encode or decode " + super.message();
    }
}
