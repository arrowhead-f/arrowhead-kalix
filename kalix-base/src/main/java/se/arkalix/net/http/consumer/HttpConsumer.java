package se.arkalix.net.http.consumer;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

public class HttpConsumer {
    private final HttpClient client;
    private final ServiceDescription service;
    private final EncodingDescriptor encoding;

    public HttpConsumer(final HttpClient client, final ServiceDescription service) {
        this.client = client;
        this.service = service;
        encoding = service.interfaces().iterator().next().encoding(); // TODO

        /*
        if (!remoteIsSecure && clientIsSecure) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "secure HTTP connection to \"" + remoteService.name() + "\"" +
                "; the service is running in insecure mode"));
        }
        if (remoteIsSecure && !clientIsSecure) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "insecure HTTP connection to \"" + remoteService.name() + "\"" +
                "; the service is running in secure mode"));
        }

        final var isHttpSupported = remoteService.interfaces()
            .stream().anyMatch(triplet -> triplet.isSecure() == clientIsSecure &&
                triplet.transport() == TransportDescriptor.HTTP);

        if (!isHttpSupported) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "HTTP connection to \"" + remoteService.name() + "\"; the " +
                "service does not support HTTP" + (clientIsSecure ? "S" : "")));
        }*/
    }

    /**
     * Creates new {@code HttpClientConnection} for consuming the service
     * represented by this consumer.
     *
     * @return {@link Future} completed with a new consumer connection if and
     * when a TCP connection has been established and any TLS handshake
     * completed with the service represented by this consumer.
     */
    public Future<HttpConsumerConnection> connect() {
        return connect(null);
    }


    /**
     * Creates new {@code HttpClientConnection} for consuming the service
     * represented by this consumer via the specified
     * {@code localSocketAddress}.
     *
     * @param localSocketAddress Socket address of local network interface to
     *                           use when communicating with the service
     *                           provider.
     * @return {@link Future} completed with a new consumer connection if and
     * when a TCP connection has been established and any TLS handshake
     * completed with the service represented by this consumer.
     */
    public Future<HttpConsumerConnection> connect(final InetSocketAddress localSocketAddress) {
        return client.connect(service.provider().socketAddress(), localSocketAddress)
            .map(connection -> new HttpConsumerConnection(connection, encoding, service.provider().identity()));
    }


    /**
     * Connects to the system providing the service represented by this
     * consumer, sends {@code request}, closes connection and then completes
     * the returned {@code Future} with the result.
     *
     * @param request Request to send.
     * @return Future completed with the request response or an error.
     * @throws NullPointerException If {@code remoteService} or {@code request}
     *                              is {@code null}.
     */
    public Future<HttpConsumerResponse> send(final HttpConsumerRequest request) {
        Objects.requireNonNull(request, "Expected request");
        request.encodingIfUnset(() -> encoding.asDtoEncoding().orElseThrow(() ->
            new IllegalStateException("No DTO support is available for the " +
                "encoding \"" + encoding + "\"; the request body must be " +
                "encoded some other way")));
        return connect().flatMap(connection -> connection.sendAndClose(request));
    }
}
