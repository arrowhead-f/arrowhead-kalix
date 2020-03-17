package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.internal.net.http.service.HttpServiceInternal;

import java.util.Objects;

/**
 * Keeps track of state related to an incoming HTTP request.
 */
public class HttpRouteTask {
    private final String basePath;
    private final HttpServiceRequest request;
    private final HttpServiceResponse response;

    private HttpRouteTask(final Builder builder) {
        basePath = Objects.requireNonNull(builder.basePath, "Expected basePath");
        request = Objects.requireNonNull(builder.request, "Expected request");
        response = Objects.requireNonNull(builder.response, "Expected response");
    }

    /**
     * @return The base path of the {@link HttpServiceInternal} that created this task.
     */
    public String basePath() {
        return basePath;
    }

    /**
     * @return The routed HTTP request.
     */
    public HttpServiceRequest request() {
        return request;
    }

    /**
     * @return Response state object.
     */
    public HttpServiceResponse response() {
        return response;
    }

    public static class Builder {
        private String basePath;
        private HttpServiceRequest request;
        private HttpServiceResponse response;

        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder request(final HttpServiceRequest request) {
            this.request = request;
            return this;
        }

        public Builder response(final HttpServiceResponse response) {
            this.response = response;
            return this;
        }

        public HttpRouteTask build() {
            return new HttpRouteTask(this);
        }
    }
}
