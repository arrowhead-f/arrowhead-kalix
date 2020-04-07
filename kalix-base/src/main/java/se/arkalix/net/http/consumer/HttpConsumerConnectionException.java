package se.arkalix.net.http.consumer;

import se.arkalix.net.http.client.HttpClientConnectionException;

public class HttpConsumerConnectionException extends HttpClientConnectionException {
    public HttpConsumerConnectionException(final String message) {
        super(message);
    }
}
