package se.arkalix.internal.net.http.service;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import se.arkalix.description.ConsumerDescription;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.internal.net.http.NettyHttpConverters;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.security.NotSecureException;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

@Internal
public class NettyHttpServiceRequest implements HttpServiceRequest {
    private final NettyHttpBodyReceiver body;
    private final QueryStringDecoder queryStringDecoder;
    private final HttpRequest request;
    private final ConsumerDescription consumer;

    private HttpHeaders headers = null;
    private HttpMethod method = null;
    private Map<String, List<String>> queryParameters = null;
    private HttpVersion version = null;

    private NettyHttpServiceRequest(final Builder builder) {
        body = Objects.requireNonNull(builder.body, "Expected body");
        queryStringDecoder = Objects.requireNonNull(builder.queryStringDecoder, "Expected queryStringDecoder");
        request = Objects.requireNonNull(builder.request, "Expected request");
        consumer = builder.consumer;
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
        return body.bodyAs(class_);
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        return body.bodyAs(encoding, class_);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        return body.bodyAsByteArray();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final Class<R> class_) {
        return body.bodyAsList(class_);
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding,
        final Class<R> class_)
    {
        return body.bodyAsList(encoding, class_);
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
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new HttpHeaders(request.headers());
        }
        return headers;
    }

    @Override
    public HttpMethod method() {
        if (method == null) {
            method = NettyHttpConverters.convert(request.method());
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
        if (queryParameters == null) {
            queryParameters = Collections.unmodifiableMap(queryStringDecoder.parameters());
        }
        return queryParameters;
    }

    @Override
    public ConsumerDescription consumer() {
        if (consumer == null) {
            throw new NotSecureException("Not in secure mode; consumer " +
                "information unavailable");
        }
        return consumer;
    }

    @Override
    public HttpVersion version() {
        if (version == null) {
            version = NettyHttpConverters.convert(request.protocolVersion());
        }
        return version;
    }

    public static class Builder {
        private NettyHttpBodyReceiver body;
        private HttpRequest request;
        private ConsumerDescription consumer;
        private QueryStringDecoder queryStringDecoder;

        public Builder body(final NettyHttpBodyReceiver body) {
            this.body = body;
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

        public Builder consumer(final ConsumerDescription consumer) {
            this.consumer = consumer;
            return this;
        }

        public NettyHttpServiceRequest build() {
            return new NettyHttpServiceRequest(this);
        }
    }
}
