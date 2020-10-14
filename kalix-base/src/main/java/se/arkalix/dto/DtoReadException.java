package se.arkalix.dto;

/**
 * Signifies the failure to read one or more {@link se.arkalix.dto DTO class
 * instances} from some arbitrary source.
 */
public class DtoReadException extends Exception {
    private final Class<? extends DtoReadable> target;
    private final DtoEncoding dtoEncoding;
    private final String value;
    private final int offset;

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param target      Class the failed DTO read intended to instantiate.
     * @param dtoEncoding Encoding applied when reading failed.
     * @param message     Description of failure.
     * @param value       Offending value.
     * @param offset      Position of offending value in read source.
     */
    public DtoReadException(
        final Class<? extends DtoReadable> target,
        final DtoEncoding dtoEncoding,
        final String message,
        final String value,
        final int offset
    ) {
        super(message);
        this.target = target;
        this.dtoEncoding = dtoEncoding;
        this.value = value;
        this.offset = offset;
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param target      Class the failed DTO read intended to instantiate.
     * @param dtoEncoding Encoding applied when reading failed.
     * @param message     Description of failure.
     * @param value       Offending value.
     * @param offset      Position of offending value in read source.
     * @param cause       Exception causing this exception to be thrown.
     */
    public DtoReadException(
        final Class<? extends DtoReadable> target,
        final DtoEncoding dtoEncoding,
        final String message,
        final String value,
        final int offset,
        final Throwable cause
    ) {
        super(message, cause);
        this.target = target;
        this.dtoEncoding = dtoEncoding;
        this.value = value;
        this.offset = offset;
    }

    @Override
    public String getMessage() {
        return "Failed to read " + target.getName() + " from " + dtoEncoding +
            " source; the following issue occurred when reading '" + value +
            "' at source offset " + offset + ": " + super.getMessage();
    }

    /**
     * Gets the class the failed DTO read, represented by this exception, was
     * intended to create an instance of.
     *
     * @return Target DTO class.
     */
    public Class<? extends DtoReadable> target() {
        return target;
    }

    /**
     * Gets encoding used when reading DTO class.
     *
     * @return Used DTO encoding.
     */
    public DtoEncoding dtoEncoding() {
        return dtoEncoding;
    }

    /**
     * Gets the value read while this exception was thrown.
     *
     * @return Offending source value.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the source offset at which the offending value is located.
     *
     * @return Offending value source offset.
     */
    public int offset() {
        return offset;
    }
}
