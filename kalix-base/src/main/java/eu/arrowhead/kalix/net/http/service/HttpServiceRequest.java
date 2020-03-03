package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * An incoming HTTP request, handled by a {@link HttpService}, that has not yet
 * been matched against any validator, route or catcher.
 */
public abstract class HttpServiceRequest implements HttpServiceRequestFull {
    private final HttpVersion version;
    private final HttpHeaders headers;
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> queryParameters;
    private final Supplier<HttpRequester> requesterSupplier;

    protected HttpServiceRequest(final Builder<?, ? extends HttpServiceRequest> builder) {
        version = Objects.requireNonNull(builder.version, "Expected version");
        headers = Objects.requireNonNull(builder.headers, "Expected headers");
        method = Objects.requireNonNull(builder.method, "Expected method");
        path = Objects.requireNonNull(builder.path, "Expected path");
        queryParameters = Objects.requireNonNull(builder.queryParameters, "Expected queryParameters");
        requesterSupplier = Objects.requireNonNull(builder.requesterSupplier, "Expected requesterSupplier");
    }

    @Override
    public Optional<String> header(final String name) {
        return headers.get(name);
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String pathParameter(final int index) {
        throw new IndexOutOfBoundsException("No path parameters are available");
    }

    @Override
    public List<String> pathParameters() {
        return Collections.emptyList();
    }

    @Override
    public Optional<String> queryParameter(final String name) {
        return Optional.ofNullable(queryParameters.get(name));
    }

    @Override
    public Map<String, String> queryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    @Override
    public HttpRequester requester() {
        return requesterSupplier.get();
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    /**
     * @param pathParameters Path parameters to provide via wrapper.
     * @return Wrapped version of this object that cannot be cast back to
     * its original class or any of its interfaces.
     */
    HttpServiceRequestFull wrapFullWithPathParameters(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequestFull() {
            @Override
            public <R extends DataReadable> Future<R> bodyAs(final Class<R> class_) {
                return self.bodyAs(class_);
            }

            @Override
            public Future<byte[]> bodyAsBytes() {
                return self.bodyAsBytes();
            }

            @Override
            public InputStream bodyAsStream() {
                return self.bodyAsStream();
            }

            @Override
            public Future<String> bodyAsString() {
                return self.bodyAsString();
            }

            @Override
            public Future<?> bodyToPath(final Path path) {
                return self.bodyToPath(path);
            }

            @Override
            public Optional<String> header(final String name) {
                return self.header(name);
            }

            @Override
            public HttpHeaders headers() {
                return self.headers();
            }

            @Override
            public HttpMethod method() {
                return self.method();
            }

            @Override
            public String path() {
                return self.path();
            }

            @Override
            public String pathParameter(final int index) {
                return pathParameters.get(index);
            }

            @Override
            public List<String> pathParameters() {
                return pathParameters;
            }

            @Override
            public Optional<String> queryParameter(final String name) {
                return self.queryParameter(name);
            }

            @Override
            public Map<String, String> queryParameters() {
                return self.queryParameters();
            }

            @Override
            public HttpRequester requester() {
                return self.requester();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }

    /**
     * @param pathParameters Path parameters to provide via wrapper.
     * @return Wrapped version of this object that cannot be cast back to
     * its original class or any of its interfaces.
     */
    HttpServiceRequestHead wrapHeadWithPathParameters(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequestHead() {
            @Override
            public Optional<String> header(final String name) {
                return self.header(name);
            }

            @Override
            public HttpHeaders headers() {
                return self.headers();
            }

            @Override
            public HttpMethod method() {
                return self.method();
            }

            @Override
            public String path() {
                return self.path();
            }

            @Override
            public String pathParameter(final int index) {
                return pathParameters.get(index);
            }

            @Override
            public List<String> pathParameters() {
                return pathParameters;
            }

            @Override
            public Optional<String> queryParameter(final String name) {
                return self.queryParameter(name);
            }

            @Override
            public Map<String, String> queryParameters() {
                return self.queryParameters();
            }

            @Override
            public HttpRequester requester() {
                return self.requester();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }
        };
    }

    /**
     * Builder for constructing {@link HttpServiceRequest} instances.
     */
    public static abstract class Builder<B extends Builder<?, ? extends R>, R extends HttpServiceRequest> {
        private HttpVersion version;
        private HttpHeaders headers;
        private HttpMethod method;
        private String path;
        private Map<String, String> queryParameters;
        private Supplier<HttpRequester> requesterSupplier;

        protected abstract B self();

        /**
         * @param version Request HTTP version.
         * @return This builder.
         */
        public B version(final HttpVersion version) {
            this.version = version;
            return self();
        }

        /**
         * @param headers Request headers.
         * @return This builder.
         */
        public B headers(final HttpHeaders headers) {
            this.headers = headers;
            return self();
        }

        /**
         * @param method Request method.
         * @return This builder.
         */
        public B method(final HttpMethod method) {
            this.method = method;
            return self();
        }

        /**
         * @param path Request path.
         * @return This builder.
         */
        public B path(final String path) {
            this.path = path;
            return self();
        }

        /**
         * @param queryParameters Request query parameters.
         * @return This builder.
         */
        public B queryParameters(final Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return self();
        }

        /**
         * @param requesterSupplier Function useful for resolving the original
         *                          sender of the built request.
         * @return This builder.
         */
        public B requesterSupplier(final Supplier<HttpRequester> requesterSupplier) {
            this.requesterSupplier = requesterSupplier;
            return self();
        }

        /**
         * @return New HTTP request object.
         */
        public abstract R build();
    }
}
