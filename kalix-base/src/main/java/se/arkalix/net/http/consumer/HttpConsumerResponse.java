package se.arkalix.net.http.consumer;

import se.arkalix.description.SystemIdentityDescription;
import se.arkalix.net.http.HttpIncomingResponse;

/**
 * An HTTP response received via an {@link HttpConsumer}.
 */
public interface HttpConsumerResponse extends HttpIncomingResponse<HttpConsumerResponse, HttpConsumerRequest> {
    /**
     * Gets connection through which this response was received.
     *
     * @return Response connection.
     */
    HttpConsumerConnection connection();

    /**
     * Gets a description of the Arrowhead system providing the service that
     * produced this response.
     *
     * @return Provider system identity description.
     */
    default SystemIdentityDescription provider() {
        return connection().remoteSystem();
    }
}
