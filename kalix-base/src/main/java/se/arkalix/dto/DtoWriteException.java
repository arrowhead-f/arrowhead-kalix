package se.arkalix.dto;

/**
 * Signifies the failure to write one or more {@link se.arkalix.dto DTO class
 * instances} to some arbitrary target.
 */
public class DtoWriteException extends Exception {
    /**
     * Creates new {@link se.arkalix.dto DTO} write exception.
     *
     * @param dtoEncoding Encoding applied when writing failed.
     * @param message     Description of failure.
     */
    public DtoWriteException(final DtoEncoding dtoEncoding, final String message) {
        super("[" + dtoEncoding + "] " + message);
    }
}
