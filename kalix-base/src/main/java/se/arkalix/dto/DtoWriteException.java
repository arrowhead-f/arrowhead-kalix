package se.arkalix.dto;

import java.util.Objects;

/**
 * Signifies the failure to write one or more {@link se.arkalix.dto DTO class
 * instances} to some arbitrary target.
 */
public class DtoWriteException extends DtoException {
    private final DtoWriter writer;
    private final Object subject;

    /**
     * Creates new {@link se.arkalix.dto DTO} write exception.
     *
     * @param writer  Writer that failed to write {@code subject}.
     * @param subject Object being written when exception was thrown.
     */
    public DtoWriteException(final DtoWriter writer, final Object subject) {
        this(writer, subject, null, null);
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} write exception.
     *
     * @param writer  Writer that failed to write {@code subject}.
     * @param subject Object being written when exception was thrown.
     * @param message Description of failure, if any.
     */
    public DtoWriteException(final DtoWriter writer, final Object subject, final String message) {
        this(writer, subject, message, null);
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} write exception.
     *
     * @param writer  Writer that failed to write {@code subject}.
     * @param subject Object being written when exception was thrown.
     * @param message Description of failure, if any.
     * @param cause   Cause of failure, if any.
     */
    public DtoWriteException(
        final DtoWriter writer,
        final Object subject,
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
        this.writer = Objects.requireNonNull(writer, "writer");
        this.subject = Objects.requireNonNull(subject, "subject");
    }

    /**
     * Gets DTO writer that threw this exception.
     *
     * @return Throwing DTO writer.
     */
    public DtoWriter writer() {
        return writer;
    }

    /**
     * Gets object that did not get written due to this exception being thrown.
     *
     * @return Object subject of DTO write.
     */
    public Object subject() {
        return subject;
    }

    @Override
    public String getMessage() {
        final var message = super.getMessage();
        return "Failed to write " + writer.encoding() + " representation of " +
            subject.getClass() + (message != null ? ": " + message : "");
    }
}
