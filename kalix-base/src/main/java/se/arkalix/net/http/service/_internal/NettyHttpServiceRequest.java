package se.arkalix.net.http.service._internal;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.net._internal.NettyMessageIncoming;
import se.arkalix.net.http._internal.NettyHttpConverters;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceConnection;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.annotation.Internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Internal
public class NettyHttpServiceRequest extends NettyMessageIncoming implements HttpServiceRequest {
    private final HttpServiceConnection connection;
    private final QueryStringDecoder queryStringDecoder;
    private final HttpRequest request;
    private final SystemRecordWithIdentity consumer;

    private HttpHeaders headers = null;
    private HttpMethod method = null;
    private Map<String, List<String>> queryParameters = null;
    private HttpVersion version = null;

    private NettyHttpServiceRequest(final Builder builder) {
        super(
            builder.alloc
        );
        connection = Objects.requireNonNull(builder.connection, "Expected connection");
        queryStringDecoder = Objects.requireNonNull(builder.queryStringDecoder, "Expected queryStringDecoder");
        request = builder.request;
        consumer = builder.consumer;
    }

    @Override
    public HttpHeaders headers() {
        if (headers == null) {
            headers = new HttpHeaders(request.headers());
        }
        return headers;
    }

    @Override
    public HttpServiceRequest clearHeaders() {
        request.headers().clear();
        return this;
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
    public HttpServiceConnection connection() {
        return connection;
    }

    @Override
    public SystemRecordWithIdentity consumer() {
        if (consumer == null) {
            throw new SecurityDisabled("Not in secure mode; consumer " +
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
        private ByteBufAllocator alloc;
        private HttpRequest request;
        private SystemRecordWithIdentity consumer;
        private HttpServiceConnection connection;
        private QueryStringDecoder queryStringDecoder;

        public Builder alloc(final ByteBufAllocator alloc) {
            this.alloc = alloc;
            return this;
        }

        public Builder connection(final HttpServiceConnection connection) {
            this.connection = connection;
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

        public Builder consumer(final SystemRecordWithIdentity consumer) {
            this.consumer = consumer;
            return this;
        }

        public NettyHttpServiceRequest build() {
            return new NettyHttpServiceRequest(this);
        }
    }
}
