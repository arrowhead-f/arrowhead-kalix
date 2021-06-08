package se.arkalix.net.http.consumer._internal;

import se.arkalix.net.BodyIncoming;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.net.http.consumer.HttpConsumerConnection;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.net.http.consumer.HttpConsumerResponse;
import se.arkalix.util.annotation.Internal;

import java.util.Objects;

@Internal
public class DefaultHttpConsumerResponse implements HttpConsumerResponse {
    private final HttpConsumerConnection connection;
    private final HttpConsumerRequest request;
    private final HttpClientResponse inner;

    public DefaultHttpConsumerResponse(
        final HttpConsumerConnection connection,
        final HttpConsumerRequest request,
        final HttpClientResponse inner
    ) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.request = Objects.requireNonNull(request, "request");
        this.inner = inner;
    }

    @Override
    public HttpHeaders headers() {
        return inner.headers();
    }

    @Override
    public DefaultHttpConsumerResponse clearHeaders() {
        inner.headers().clear();
        return this;
    }

    @Override
    public HttpConsumerRequest request() {
        return request;
    }

    @Override
    public HttpStatus status() {
        return inner.status();
    }

    @Override
    public HttpVersion version() {
        return inner.version();
    }

    @Override
    public HttpConsumerConnection connection() {
        return connection;
    }

    @Override
    public BodyIncoming body() {
        return inner.body();
    }
}
