package se.arkalix.dto;

public class DtoWriteException extends Exception {
    public DtoWriteException(final DtoEncoding dtoEncoding, final String message) {
        super("[" + dtoEncoding + "] " + message);
    }
}
