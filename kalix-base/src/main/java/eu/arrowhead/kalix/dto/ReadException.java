package eu.arrowhead.kalix.dto;

public class ReadException extends Exception {
    public ReadException(final DataEncoding dataEncoding, final String message, final String value, final int offset) {
        super("Failed to read " + dataEncoding + "; cause: " + message + " `" + value + "` at offset " + offset);
    }
}
