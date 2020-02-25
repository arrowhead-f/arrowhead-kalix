package eu.arrowhead.kalix.dto;

public class WriteException extends Exception {
    public WriteException(final Format format, final String message) {
        super("[" + format + "] " + message);
    }
}
