package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.*;
import java.util.function.Supplier;

/**
 * An incoming HTTP request, handled by a {@link HttpService}, that has not yet
 * been matched against any validator, route or catcher.
 */
public class HttpServiceRequest implements HttpServiceRequestFull {
    private final HttpVersion version;
    private final HttpMethod method;
    private final String path;
    private final Map<String, List<String>> queryParameters;
    private final HttpHeaders headers;
    private final HttpServiceRequestBody body;
    private final Supplier<HttpRequester> requesterSupplier;

    protected HttpServiceRequest(final Builder builder) {
        version = Objects.requireNonNull(builder.version, "Expected version");
        method = Objects.requireNonNull(builder.method, "Expected method");
        path = Objects.requireNonNull(builder.path, "Expected path");
        queryParameters = Objects.requireNonNull(builder.queryParameters, "Expected queryParameters");
        headers = Objects.requireNonNull(builder.headers, "Expected headers");
        body = Objects.requireNonNull(builder.body, "Expected body");
        requesterSupplier = Objects.requireNonNull(builder.requesterSupplier, "Expected requesterSupplier");
    }

    @Override
    public <R extends DataReadable> Future<? extends R> bodyAs(final Class<R> class_) {
        return body.bodyAs(class_);
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
    public List<String> queryParameters(final String name) {
        final var parameters = queryParameters.get(name);
        return parameters != null
            ? Collections.unmodifiableList(parameters)
            : Collections.emptyList();
    }

    @Override
    public List<String> pathParameters() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> queryParameters() {
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
            public <R extends DataReadable> Future<? extends R> bodyAs(final Class<R> class_) {
                return self.bodyAs(class_);
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
            public List<String> queryParameters(final String name) {
                return self.queryParameters(name);
            }

            @Override
            public Map<String, List<String>> queryParameters() {
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
            public List<String> queryParameters(final String name) {
                return self.queryParameters(name);
            }

            @Override
            public Map<String, List<String>> queryParameters() {
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
    public static class Builder {
        private HttpVersion version;
        private HttpMethod method;
        private String path;
        private Map<String, List<String>> queryParameters;
        private HttpHeaders headers;
        private HttpServiceRequestBody body;
        private Supplier<HttpRequester> requesterSupplier;

        /**
         * @param version Request HTTP version.
         * @return This builder.
         */
        public Builder version(final HttpVersion version) {
            this.version = version;
            return this;
        }

        /**
         * @param method Request method.
         * @return This builder.
         */
        public Builder method(final HttpMethod method) {
            this.method = method;
            return this;
        }

        /**
         * @param path Request path.
         * @return This builder.
         */
        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        /**
         * @param queryParameters Request query parameters.
         * @return This builder.
         */
        public Builder queryParameters(final Map<String, List<String>> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        /**
         * @param headers Request headers.
         * @return This builder.
         */
        public Builder headers(final HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        /**
         * @param body Handler used to execute one out of several possible
         *             request body retrieval strategies.
         * @return This builder.
         */
        public Builder body(final HttpServiceRequestBody body) {
            this.body = body;
            return this;
        }

        /**
         * @param requesterSupplier Function useful for lazily assembling data
         *                          about the original sender of the built
         *                          request.
         * @return This builder.
         */
        public Builder requesterSupplier(final Supplier<HttpRequester> requesterSupplier) {
            this.requesterSupplier = requesterSupplier;
            return this;
        }

        /**
         * @return New HTTP request object.
         */
        public HttpServiceRequest build() {
            return new HttpServiceRequest(this);
        }
    }
}
