package eu.arrowhead.kalix.dto;

public class ReadException extends Exception {
    public ReadException(final Format format, final String message, final String value, final int offset) {
        super("[" + format + "] " + message + " `" + value + "` at offset " + offset);
    }
}
