package se.arkalix.net.http.service;

import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.net.BodyIncoming;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpIncomingRequest;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;

import java.util.List;
import java.util.Map;

/**
 * The head and body of an incoming HTTP request.
 */
@SuppressWarnings("unused")
public interface HttpServiceRequest extends HttpIncomingRequest<HttpServiceRequest> {
    /**
     * Gets connection through which this request was received.
     *
     * @return Request connection.
     */
    HttpServiceConnection connection();

    /**
     * Gets a description of the Arrowhead system attempting to consume a
     * service provided by a local system.
     *
     * @return Consumer system identity description.
     */
    default SystemRecordWithIdentity consumer() {
        return connection().remoteSystem();
    }

    default HttpServiceRequest cloneAndSet(final List<String> pathParameters) {
        final var self = this;
        return new HttpServiceRequest() {
            @Override
            public BodyIncoming body() {
                return self.body();
            }

            @Override
            public HttpServiceConnection connection() {
                return self.connection();
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
            public List<String> pathParameters() {
                return pathParameters;
            }

            @Override
            public Map<String, List<String>> queryParameters() {
                return self.queryParameters();
            }

            @Override
            public HttpVersion version() {
                return self.version();
            }

            @Override
            public HttpHeaders headers() {
                return self.headers();
            }

            @Override
            public HttpServiceRequest clearHeaders() {
                return self.clearHeaders();
            }
        };
    }
}
