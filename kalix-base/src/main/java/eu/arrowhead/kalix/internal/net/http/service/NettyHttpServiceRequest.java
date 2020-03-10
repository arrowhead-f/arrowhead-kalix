package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.internal.net.http.NettyHttpPeer;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpPeer;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.FutureProgress;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static eu.arrowhead.kalix.internal.net.http.NettyHttpAdapters.adapt;

@Internal
public class NettyHttpServiceRequest implements HttpServiceRequest {
    private final NettyHttpBodyReceiver body;
    private final EncodingDescriptor encoding;
    private final QueryStringDecoder queryStringDecoder;
    private final HttpRequest request;
    private final NettyHttpPeer requester;

    private HttpHeaders headers = null;
    private HttpMethod method = null;
    private HttpVersion version = null;

    private NettyHttpServiceRequest(final Builder builder) {
        body = Objects.requireNonNull(builder.body, "Expected body");
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        queryStringDecoder = Objects.requireNonNull(builder.queryStringDecoder, "Expected queryStringDecoder");
        request = Objects.requireNonNull(builder.request, "Expected request");
        requester = Objects.requireNonNull(builder.requester, "Expected requester");
    }

    @Override
    public <R extends DataReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
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
            headers = new HttpHeaders(request.headers());
        }
        return headers;
    }

    @Override
    public HttpMethod method() {
        if (method == null) {
            method = adapt(request.method());
        }
        return method;
    }

    @Override
    public String path() {
        return queryStringDecoder.path();
    }

    @Override
    public List<String> pathParameters() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> queryParameters() {
        return Collections.unmodifiableMap(queryStringDecoder.parameters());
    }

    @Override
    public HttpPeer requester() {
        return requester;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = adapt(request.protocolVersion());
        }
        return version;
    }

    public static class Builder {
        private NettyHttpBodyReceiver body;
        private HttpRequest request;
        private EncodingDescriptor encoding;
        private NettyHttpPeer requester;
        private QueryStringDecoder queryStringDecoder;

        public Builder body(final NettyHttpBodyReceiver body) {
            this.body = body;
            return this;
        }

        public Builder encoding(final EncodingDescriptor encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder queryStringDecoder(final QueryStringDecoder queryStringDecoder) {
            this.queryStringDecoder = queryStringDecoder;
            return this;
        }

        public Builder request(final HttpRequest request) {
            this.request = request;
            return this;
        }

        public Builder requester(final NettyHttpPeer requester) {
            this.requester = requester;
            return this;
        }

        public NettyHttpServiceRequest build() {
            return new NettyHttpServiceRequest(this);
        }
    }
}
