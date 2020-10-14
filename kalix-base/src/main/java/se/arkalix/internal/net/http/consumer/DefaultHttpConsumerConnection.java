package se.arkalix.internal.net.http.consumer;

import se.arkalix.ArSystem;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoWritable;
import se.arkalix.net.MessageEncodingUnsupported;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.consumer.HttpConsumerConnection;
import se.arkalix.net.http.consumer.HttpConsumerRequest;
import se.arkalix.net.http.consumer.HttpConsumerResponse;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

class DefaultHttpConsumerConnection implements HttpConsumerConnection {
    private final ArSystem localSystem;
    private final EncodingDescriptor encoding;
    private final String authorization;
    private final SystemIdentity remoteIdentity;
    private final HttpClientConnection connection;

    DefaultHttpConsumerConnection(
        final ArSystem localSystem,
        final EncodingDescriptor encoding,
        final String authorization,
        final SystemIdentity remoteIdentity,
        final HttpClientConnection connection
    ) {
        this.localSystem = Objects.requireNonNull(localSystem, "Expected localSystem");
        this.encoding = Objects.requireNonNull(encoding, "Expected encoding");
        this.authorization = authorization;
        this.remoteIdentity = remoteIdentity;
        this.connection = Objects.requireNonNull(connection, "Expected connection");
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

    @SuppressWarnings("unchecked")
    private void prepare(final HttpConsumerRequest request) {
        Objects.requireNonNull(request, "Expected request");

        if (request.encoding().isEmpty()) {
            final var body = request.body().orElse(null);
            if (body instanceof DtoWritable || body instanceof List) {
                final var dtoEncoding = encoding.asDto()
                    .orElseThrow(() -> new MessageEncodingUnsupported(request, encoding));

                if (body instanceof DtoWritable) {
                    request.body(dtoEncoding, (DtoWritable) body);
                }
                else {
                    request.body(dtoEncoding, (List<DtoWritable>) body);
                }
            }
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
