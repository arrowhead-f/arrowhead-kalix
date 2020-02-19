package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote host.
 */
public class HttpClient {
    private InetSocketAddress host;
    private HttpToken token;
    private HttpTokenRequester tokenRequester;

    /**
     * Creates new {@link HttpClient} for communicating with {@code host}.
     *
     * @param host Hostname/port or IP/port of target host.
     */
    public HttpClient(final InetSocketAddress host) {
        this.host = host;
    }

    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        return null;
    }

    private Future<HttpClientResponse> send(final HttpClientRequest request, final int retries) throws HttpClientException {
        if (retries <= 0) {
            throw new HttpClientException();
        }
        if (token != null && token.isExpired()) {
            if (tokenRequester != null) {
                return tokenRequester.request(token).flatMap(token -> {
                    this.token = token;
                    return send(request, retries - 1);
                });
            }
            else {
                token = null; // TODO: Throw exception?
            }
        }

        return null;
    }
}
