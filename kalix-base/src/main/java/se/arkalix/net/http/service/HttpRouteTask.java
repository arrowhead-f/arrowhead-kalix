package se.arkalix.net.http.service;

import java.util.Objects;
import java.util.Optional;

/**
 * Keeps track of state related to an incoming HTTP request.
 */
public class HttpRouteTask {
    private final String basePath;
    private final HttpServiceRequest request;
    private final HttpServiceResponse response;

    private HttpRouteTask(final Builder builder) {
        basePath = builder.basePath;
        request = Objects.requireNonNull(builder.request, "Expected request");
        response = Objects.requireNonNull(builder.response, "Expected response");
    }

    /**
     * @return The {@link HttpService#basePath(String) base path} of the
     * {@link HttpService} for which this task was created, if any.
     */
    public Optional<String> basePath() {
        return Optional.ofNullable(basePath);
    }

    /**
     * @return The byte length of the {@link HttpService#basePath(String) base
     * path} of the {@link HttpService} for which this task was created, or 0
     * if no base path was specified.
     */
    public int basePathLengthOrZero() {
        return basePath != null ? basePath.length() : 0;
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

    /**
     * Builder useful for creating {@link HttpRouteTask} instances.
     */
    public static class Builder {
        private String basePath;
        private HttpServiceRequest request;
        private HttpServiceResponse response;

        /**
         * {@link HttpService#basePath(String) Base path} of the {@link
         * HttpService} to handle the created {@link HttpRouteTask}.
         *
         * @param basePath Base path to associate with created task.
         * @return This builder.
         * @see <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>
         */
        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * Incoming HTTP request handled by the created {@link HttpRouteTask}.
         *
         * @param request Incoming HTTP request.
         * @return This builder.
         */
        public Builder request(final HttpServiceRequest request) {
            this.request = request;
            return this;
        }

        /**
         * Response object constructed by the service handling the created
         * {@link HttpRouteTask}.
         *
         * @param response Unfinished outgoing HTTP response.
         * @return This builder.
         */
        public Builder response(final HttpServiceResponse response) {
            this.response = response;
            return this;
        }

        /**
         * @return New {@link HttpRouteTask}.
         */
        public HttpRouteTask build() {
            return new HttpRouteTask(this);
        }
    }
}
