package eu.arrowhead.kalix.http;

import java.util.Map;

public class HttpResponse {
    public void cancel() {
        // TODO.
    }

    public void cancel(final HttpStatus status) {

    }

    public void cancel(final HttpStatus status, final String message) {

    }

    public String header(final String name) {
        return null;
    }

    public HttpResponse header(final String name, final String value) {
        // TODO.
        return this;
    }

    public Map<String, String> headers() {
        return null;
    }

    public HttpResponse headers(final Map<String, String> headers) {
        // TODO.
        return this;
    }

    public HttpStatus status() {
        return null;
    }

    public HttpResponse status(final HttpStatus status) {
        // TODO.
        return this;
    }
}
