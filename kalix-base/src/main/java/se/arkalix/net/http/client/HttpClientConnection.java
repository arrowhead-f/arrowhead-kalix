package se.arkalix.net.http.client;

import se.arkalix.net.http.HttpIncomingResponse;
import se.arkalix.net.http.HttpOutgoingConnection;
import se.arkalix.net.http.HttpOutgoingRequest;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * Represents an HTTP connection established between a local {@link HttpClient}
 * and some remote HTTP server.
 */
public interface HttpClientConnection extends HttpOutgoingConnection<HttpClientRequest, HttpIncomingResponse> {}
