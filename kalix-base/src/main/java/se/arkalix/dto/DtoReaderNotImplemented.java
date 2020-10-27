package se.arkalix.dto;

import java.util.Objects;

/**
 * Signifies the failure to write a {@link se.arkalix.dto DTO class instance}
 * to some arbitrary target due to the DTO class not implementing a required
 * interface.
 */
public class DtoReaderNotImplemented extends DtoReadException {
    private final Class<? extends DtoReadable> unimplementedInterface;

    /**
     * Creates new exception.
     *
     * @param reader                 DTO reader that could not read given
     *                               {@code target} class.
     * @param target                 DTO class that could not be read.
     * @param unimplementedInterface Interface not properly implemented by the
     *                               {@code target} DTO class.
     */
    public DtoReaderNotImplemented(
        final DtoReader reader,
        final Class<? extends DtoReadable> target,
        final Class<? extends DtoReadable> unimplementedInterface
    ) {
        super(reader, target);
        this.unimplementedInterface = Objects.requireNonNull(unimplementedInterface, "unimplementedInterface");
    }

    /**
     * Gets interface that must be implemented by {@link #target()} for it to
     * be readable using {@link #reader()}.
     *
     * @return Unimplemented {@link DtoReadable} interface.
     */
    @SuppressWarnings("unused")
    public Class<? extends DtoReadable> unimplementedInterface() {
        return unimplementedInterface;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ": the target class is either not " +
            "implementing " + unimplementedInterface + " or is missing a " +
            "required static method; if the class was produced by the DTO " +
            "code generator, this is likely caused by its input interface " +
            "not missing an argument for " + reader().encoding() + " in its " +
            "@DtoReadableAs annotation";
    }
}
