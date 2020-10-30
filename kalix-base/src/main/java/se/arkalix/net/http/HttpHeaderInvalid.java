package se.arkalix.net.http;

import java.util.Objects;
import java.util.Optional;

public class HttpHeaderInvalid extends RuntimeException {
    private final String name;
    private final String value;

    public HttpHeaderInvalid(final String name, final String value) {
        this(name, value, null, null, true, true);
    }

    public HttpHeaderInvalid(final String name, final String value, final String message) {
        this(name, value, null, null, true, true);
    }

    public HttpHeaderInvalid(final String name, final String value, final String message, final Throwable cause) {
        this(name, value, message, cause, true, true);
    }

    public HttpHeaderInvalid(final String name, final String value, final Throwable cause) {
        this(name, value, null, cause, true, true);
    }

    protected HttpHeaderInvalid(
        final String name,
        final String value,
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.name = Objects.requireNonNull(name, "name");
        this.value = Objects.requireNonNull(value, "value");
    }

    public String name() {
        return name;
    }

    public Optional<String> value() {
        return Optional.ofNullable(value);
    }

    @Override
    public String getMessage() {
        final var message = super.getMessage();
        return "invalid HTTP header " + name + "=\"" + value + "\"" +
            (message != null ? "; " + message : "");
    }
}
