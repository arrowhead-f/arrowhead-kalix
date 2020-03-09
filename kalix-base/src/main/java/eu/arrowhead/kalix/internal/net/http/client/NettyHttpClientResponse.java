package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.internal.net.http.NettyHttpHeaders;
import eu.arrowhead.kalix.internal.net.http.NettyHttpPeer;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.client.HttpClientResponse;
import eu.arrowhead.kalix.net.http.HttpPeer;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.FutureProgress;
import io.netty.handler.codec.http.HttpResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import static eu.arrowhead.kalix.internal.net.http.NettyHttpAdapters.adapt;

@Internal
public class NettyHttpClientResponse implements HttpClientResponse {
    private final NettyHttpBodyReceiver body;
    private final EncodingDescriptor encoding;
    private final HttpResponse response;
    private final NettyHttpPeer responder;

    private HttpHeaders headers = null;
    private HttpStatus status = null;
    private HttpVersion version = null;

    private NettyHttpClientResponse(final Builder builder) {
        body = Objects.requireNonNull(builder.body, "Expected body");
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        response = Objects.requireNonNull(builder.response, "Expected response");
        responder = Objects.requireNonNull(builder.responder, "Expected responder");
    }

    @Override
    public <R extends DataReadable> FutureProgress<? extends R> bodyAs(final Class<R> class_) {
        return body.bodyAs(class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return body.bodyAsByteArray();
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        return body.bodyAsStream();
    }

    @Override
    public FutureProgress<String> bodyAsString() {
        return body.bodyAsString();
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        return body.bodyTo(path, append);
    }

    @Override
    public EncodingDescriptor encoding() {
        return encoding;
    }

    @Override
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new NettyHttpHeaders(response.headers());
        }
        return headers;
    }

    @Override
    public HttpPeer responder() {
        return responder;
    }

    @Override
    public HttpStatus status() {
        if (status == null) {
            status = adapt(response.status());
        }
        return status;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = adapt(response.protocolVersion());
        }
        return version;
    }

    public static class Builder {
        private NettyHttpBodyReceiver body;
        private HttpResponse response;
        private EncodingDescriptor encoding;
        private NettyHttpPeer responder;

        public Builder body(final NettyHttpBodyReceiver body) {
            this.body = body;
            return this;
        }

        public Builder encoding(final EncodingDescriptor encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder response(final HttpResponse response) {
            this.response = response;
            return this;
        }

        public Builder responder(final NettyHttpPeer responder) {
            this.responder = responder;
            return this;
        }

        public NettyHttpClientResponse build() {
            return new NettyHttpClientResponse(this);
        }
    }
}
