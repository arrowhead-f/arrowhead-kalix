package se.arkalix.codec;

import se.arkalix.io.buf.BufferWriter;

import java.util.Objects;

/**
 * Represents the inability to encode some arbitrary object into a string of
 * bytes in a certain codec, caused by the object somehow violating the
 * codec it is intended to be represented in.
 */
public class EncodableConstraintViolated extends CodecException {
    private final BufferWriter writer;
    private final Object object;
    private final String description;

    /**
     * Creates new exception.
     *
     * @param codecType   Codec being applied when exception was thrown.
     * @param writer      Writer to which encoded object was to be written.
     * @param object      Object violating codec, if relevant.
     * @param description Description of failure.
     */
    public EncodableConstraintViolated(
        final CodecType codecType,
        final BufferWriter writer,
        final Object object,
        final String description
    ) {
        super(codecType, null, null, true, true);
        this.writer = Objects.requireNonNull(writer, "writer");
        this.object = object;
        this.description = Objects.requireNonNull(description, "description");
    }

    /**
     * Writer the encoded form of the encoded object was to be written to.
     * <p>
     * The writer is only guaranteed to be positioned at the point where the
     * offending object should have been encoded <i>if</i> the writer has not
     * been written to since this exception was thrown.
     *
     * @return Encodable throwing this exception.
     */
    public BufferWriter writer() {
        return writer;
    }

    /**
     * Concrete object being encoded when this exception was thrown.
     * <p>
     * Note that {@code null} is a possible return value.
     *
     * @return Offending object.
     */
    public Object object() {
        return object;
    }

    @Override
    public String getMessage() {
        return "Codec " + codec() + " cannot encode " + object + "; " +
            description;
    }
}
