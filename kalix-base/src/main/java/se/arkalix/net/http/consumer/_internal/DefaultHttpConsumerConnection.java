package se.arkalix.net.http.consumer._internal;

import se.arkalix.ArSystem;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.MediaType;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.consumer.HttpConsumerConnection;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.net.http.consumer.HttpConsumerResponse;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

class DefaultHttpConsumerConnection implements HttpConsumerConnection {
    private final ArSystem localSystem;
    private final CodecType codecType;
    private final String authorization;
    private final SystemIdentity remoteIdentity;
    private final HttpClientConnection connection;

    DefaultHttpConsumerConnection(
        final ArSystem localSystem,
        final CodecType codecType,
        final String authorization,
        final SystemIdentity remoteIdentity,
        final HttpClientConnection connection
    ) {
        this.localSystem = Objects.requireNonNull(localSystem, "localSystem");
        this.codecType = Objects.requireNonNull(codecType, "codec");
        this.authorization = authorization;
        this.remoteIdentity = remoteIdentity;
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public SystemIdentity remoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public ArSystem localSystem() {
        return localSystem;
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        return connection.remoteSocketAddress();
    }

    @Override
    public InetSocketAddress localSocketAddress() {
        return connection.localSocketAddress();
    }

    @Override
    public boolean isLive() {
        return connection.isLive();
    }

    @Override
    public boolean isSecure() {
        return connection.isSecure();
    }

    @Override
    public Future<HttpConsumerResponse> send(final HttpConsumerRequest request) {
        prepare(request);
        return connection.send(request.unwrap())
            .map(response -> new DefaultHttpConsumerResponse(this, request, response));
    }

    @Override
    public Future<HttpConsumerResponse> sendAndClose(final HttpConsumerRequest request) {
        prepare(request);
        return connection.sendAndClose(request.unwrap())
            .map(response -> new DefaultHttpConsumerResponse(this, request, response));
    }

    private void prepare(final HttpConsumerRequest request) {
        Objects.requireNonNull(request, "request");

        final var headers = request.headers();
        if (!headers.contains("accept")) {
            headers.set("accept", MediaType.getOrCreate(codecType).toString());
        }

        if (request.codecType().isEmpty()) {
            request.body().ifPresent(body -> request.codecType(codecType));
        }

        if (authorization != null) {
            request.headers().setIfEmpty("authorization", authorization);
        }
    }

    @Override
    public Future<?> close() {
        return connection.close();
    }
}
