package se.arkalix.internal.net.http.client;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponse;
import se.arkalix.internal.net.NettyMessageIncoming;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpOutgoingRequest;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.util.annotation.Internal;

import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static se.arkalix.internal.net.http.NettyHttpConverters.convert;

@Internal
public class NettyHttpClientResponse extends NettyMessageIncoming implements HttpClientResponse {
    private final HttpClientConnection connection;
    private final HttpClientRequest request;
    private final HttpResponse inner;

    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    public NettyHttpClientResponse(
        final ByteBufAllocator alloc,
        final HttpClientConnection connection,
        final HttpClientRequest request,
        final HttpResponse inner
    ) {
        super(alloc, Objects.requireNonNull(inner, "Expected inner")
            .headers().getInt(CONTENT_LENGTH, 0));
        this.connection = Objects.requireNonNull(connection, "Expected connection");
        this.request = Objects.requireNonNull(request, "Expected request");
        this.inner = inner;
    }

    @Override
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new HttpHeaders(inner.headers());
        }
        return headers;
    }

    @Override
    public HttpClientResponse clearHeaders() {
        inner.headers().clear();
        return this;
    }

    @Override
    public HttpClientRequest request() {
        return request;
    }

    @Override
    public HttpStatus status() {
        if (status == null) {
            status = convert(inner.status());
        }
        return status;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = convert(inner.protocolVersion());
        }
        return version;
    }

    @Override
    public HttpClientConnection connection() {
        return connection;
    }
}
