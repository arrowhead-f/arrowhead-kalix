package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.InputStream;
import java.nio.file.Path;

public class NettyHttpServiceRequest extends HttpServiceRequest {
    private final NettyHttpBodyHandler bodyHandler;

    protected NettyHttpServiceRequest(final Builder builder) {
        super(builder);
        bodyHandler = builder.bodyHandler;
    }

    @Override
    public <R extends DataReadable> Future<R> bodyAs(final Class<R> class_) {
        return bodyHandler.bodyAs(class_);
    }

    @Override
    public Future<byte[]> bodyAsBytes() {
        return bodyHandler.bodyAsBytes();
    }

    @Override
    public InputStream bodyAsStream() {
        return bodyHandler.bodyAsStream();
    }

    @Override
    public Future<String> bodyAsString() {
        return bodyHandler.bodyAsString();
    }

    @Override
    public Future<?> bodyToPath(final Path path) {
        return bodyHandler.bodyToPath(path);
    }

    public static class Builder extends HttpServiceRequest.Builder<Builder, NettyHttpServiceRequest> {
        private NettyHttpBodyHandler bodyHandler;

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * @param version Request HTTP version.
         * @return This builder.
         */
        public Builder version(final HttpVersion version) {
            return version(eu.arrowhead.kalix.net.http.HttpVersion
                .getOrCreate(version.majorVersion(), version.minorVersion()));
        }

        /**
         * @param headers Request headers.
         * @return This builder.
         */
        public Builder headers(final HttpHeaders headers) {
            return headers(new NettyHttpHeaders(headers));
        }

        /**
         * @param method Request method.
         * @return This builder.
         */
        public Builder method(final HttpMethod method) {
            return method(eu.arrowhead.kalix.net.http.HttpMethod.valueOf(method.name()));
        }

        /**
         * @param bodyHandler Handler to use for regulating the receiving of
         *                    an incoming HTTP request body.
         * @return This builder.
         */
        public Builder bodyHandler(final NettyHttpBodyHandler bodyHandler) {
            this.bodyHandler = bodyHandler;
            return this;
        }

        @Override
        public NettyHttpServiceRequest build() {
            return new NettyHttpServiceRequest(this);
        }
    }
}
