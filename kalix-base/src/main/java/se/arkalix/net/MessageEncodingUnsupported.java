package se.arkalix.net;

import se.arkalix.descriptor.EncodingDescriptor;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * an unsupported encoding being specified.
 */
public class MessageEncodingUnsupported extends MessageException {
    private final EncodingDescriptor encoding;

    /**
     * Creates new exception.
     *
     * @param message  Offending message.
     * @param encoding Unsupported encoding.
     */
    public MessageEncodingUnsupported(final Message message, final EncodingDescriptor encoding) {
        super(message);
        this.encoding = Objects.requireNonNull(encoding);
    }

    /**
     * Gets unsupported encoding causing this exception to be thrown.
     *
     * @return Unsupported encoding descriptor.
     */
    public EncodingDescriptor encoding() {
        return encoding;
    }

    @Override
    public String getMessage() {
        return "Unsupported message encoding \"" + encoding + "\"; unable " +
            "to encode or decode " + super.message();
    }
}
