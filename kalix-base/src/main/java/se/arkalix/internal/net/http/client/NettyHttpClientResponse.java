package se.arkalix.internal.net.http.client;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponse;
import se.arkalix.internal.net.NettyMessageIncoming;
import se.arkalix.net.http.*;
import se.arkalix.util.annotation.Internal;

import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static se.arkalix.internal.net.http.NettyHttpConverters.convert;

@Internal
public class NettyHttpClientResponse extends NettyMessageIncoming implements HttpIncomingResponse {
    private final HttpOutgoingRequest<?> request;
    private final HttpResponse inner;

    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    public NettyHttpClientResponse(
        final ByteBufAllocator alloc,
        final HttpOutgoingRequest<?> request,
        final HttpResponse inner
    ) {
        super(alloc, Objects.requireNonNull(inner, "Expected inner")
            .headers().getInt(CONTENT_LENGTH, 0));
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
    public HttpOutgoingRequest<?> request() {
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
}
