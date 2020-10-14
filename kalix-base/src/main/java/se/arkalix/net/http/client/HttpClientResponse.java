package se.arkalix.net.http.client;

import se.arkalix.net.http.HttpIncomingResponse;

/**
 * An HTTP response received via an {@link HttpClient}.
 */
public interface HttpClientResponse extends HttpIncomingResponse<HttpClientResponse, HttpClientRequest> {
    /**
     * Gets connection through which this response was received.
     *
     * @return Response connection.
     */
    HttpClientConnection connection();
}
