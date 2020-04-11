package se.arkalix.dto;

/**
 * Signifies the failure to read one or more {@link se.arkalix.dto DTO class
 * instances} from some arbitrary source.
 */
public class DtoReadException extends Exception {
    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param dtoEncoding Encoding applied when reading failed.
     * @param message     Description of failure.
     * @param value       Offending value.
     * @param offset      Position of offending value in read source.
     */
    public DtoReadException(final DtoEncoding dtoEncoding, final String message, final String value, final int offset) {
        super("Failed to read " + dtoEncoding + "; cause: " + message + " `" + value + "` at offset " + offset);
    }
}
