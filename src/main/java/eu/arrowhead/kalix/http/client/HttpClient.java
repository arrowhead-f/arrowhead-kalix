package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote host.
 */
public class HttpClient {
    private final InetSocketAddress host;
    private final HttpQualifier qualifier;

    /**
     * Creates new {@link HttpClient} for communicating with {@code host}.
     *
     * @param host Hostname/port or IP/port of target host.
     */
    public HttpClient(final InetSocketAddress host) {
        this.host = host;
        this.qualifier = null;
    }

    /**
     * Creates new {@link HttpClient} for communicating with {@code host},
     * using given {@link HttpQualifier} to qualify outgoing HTTP requests.
     * Qualification may entail adding authorization headers, or other details,
     * to sent requests.
     *
     * @param host      Hostname/port or IP/port of target host.
     * @param qualifier Client request qualifier.
     */
    public HttpClient(final InetSocketAddress host, final HttpQualifier qualifier) {
        this.host = host;
        this.qualifier = qualifier;
    }

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClient}.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        /*if (qualifier != null) {
            return qualifier.apply(request)
                .flatMap(() -> {
                    // Actual send request.
                });
        }*/
        throw new UnsupportedOperationException();
    }
}
