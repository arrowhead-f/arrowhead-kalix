package se.arkalix.dto;

public class DtoReadException extends Exception {
    public DtoReadException(final DtoEncoding dtoEncoding, final String message, final String value, final int offset) {
        super("Failed to read " + dtoEncoding + "; cause: " + message + " `" + value + "` at offset " + offset);
    }
}
