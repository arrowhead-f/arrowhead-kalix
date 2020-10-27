package se.arkalix.dto;

import java.util.Objects;

/**
 * Signifies the failure to write a {@link se.arkalix.dto DTO class instance}
 * to some arbitrary target due to the DTO class not implementing a required
 * interface.
 */
public class DtoWriterNotImplemented extends DtoWriteException {
    private final Class<? extends DtoWritable> unimplementedInterface;

    /**
     * Creates new exception.
     *
     * @param writer                 DTO writer that could not write given
     *                               {@code subject}.
     * @param subject                DTO class instance that could not be
     *                               written.
     * @param unimplementedInterface Interface not implemented by {@code
     *                               subject} DTO class.
     */
    public DtoWriterNotImplemented(
        final DtoWriter writer,
        final DtoWritable subject,
        final Class<? extends DtoWritable> unimplementedInterface
    ) {
        super(writer, subject);
        this.unimplementedInterface = Objects.requireNonNull(unimplementedInterface, "unimplementedInterface");
    }

    /**
     * Gets interface that must be implemented by {@link #subject()} for it to
     * be writable using {@link #writer()}.
     *
     * @return Unimplemented {@link DtoWritable} interface.
     */
    @SuppressWarnings("unused")
    public Class<? extends DtoWritable> unimplementedInterface() {
        return unimplementedInterface;
    }

    @Override
    public DtoWritable subject() {
        return (DtoWritable) super.subject();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": the subject class does not implement " +
            unimplementedInterface + "; if the class was produced by the " +
            "DTO code generator, this is likely caused by its input " +
            "interface missing an argument to its @DtoWritableAs annotation";
    }
}
