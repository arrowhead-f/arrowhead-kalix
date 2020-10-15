package se.arkalix.net.http.consumer;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.internal.net.http.consumer.DefaultHttpConsumer;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static se.arkalix.descriptor.TransportDescriptor.HTTP;

/**
 * Client useful for consuming an {@link se.arkalix.security secure or
 * insecure} HTTP {@link se.arkalix service}.
 * <p>
 * This class provides similar functionality to {@link HttpClient}, with the
 * exception that hostnames, encodings, authorization tokens and other
 * details may be managed automatically.
 */
public interface HttpConsumer extends ArConsumer {
    /**
     * Creates new service consumer.
     *
     * @param system  Arrowhead system consuming {@code service}.
     * @param service Service to be consumed.
     * @throws NullPointerException     If {@code system} or {@code service} is
     *                                  {@code null}.
     * @throws IllegalArgumentException If the security modes of {@code system}
     *                                  and {@code service} do not match (e.g.
     *                                  the system is secure but the service is
     *                                  not).
     */
    static HttpConsumer create(final ArSystem system, final ServiceDescription service) {
        return create(system, service, null);
    }

    /**
     * Creates new service consumer, limiting itself to a subset of the
     * encodings supported by {@code service}.
     *
     * @param system    Arrowhead system consuming {@code service}.
     * @param service   Service to be consumed.
     * @param encodings Supported request/response encodings. If specified as
     *                  {@code null}, the encodings returned by
     *                  {@link EncodingDescriptor#allWithDtoSupport()} are used.
     * @throws NullPointerException     If {@code system} or {@code service} is
     *                                  {@code null}.
     * @throws IllegalArgumentException If the security modes of {@code system}
     *                                  and {@code service} do not match (e.g.
     *                                  the system is secure but the service is
     *                                  not).
     */
    static HttpConsumer create(
        final ArSystem system,
        final ServiceDescription service,
        final Collection<EncodingDescriptor> encodings
    ) {
        return factory().create(system, service, encodings);
    }

    /**
     * Gets default {@link Factory}, used to create new {@link HttpConsumer}
     * instances.
     *
     * @return Default {@link Factory}.
     */
    static Factory factory() {
        return Factory.instance;
    }

    /**
     * Creates new {@code HttpClientConnection} for communicating with the
     * service associated with this consumer.
     *
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     */
    default Future<HttpConsumerConnection> connect() {
        return connect(null);
    }

    /**
     * Creates new {@code HttpClientConnection} for communicating with the
     * service associated with this consumer.
     *
     * @param localSocketAddress Socket address of local network interface to
     *                           use when communicating with remote host.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     */
    Future<HttpConsumerConnection> connect(final InetSocketAddress localSocketAddress);

    /**
     * Connects to remote host at {@code remoteSocketAddress}, sends
     * {@code request}, closes connection and then completes the returned
     * {@code Future} with the result.
     *
     * @param request Request to send.
     * @return Future completed with the request response or an error.
     * @throws NullPointerException If {@code remoteSocketAddress} or
     *                              {@code request} is {@code null}.
     */
    default Future<HttpConsumerResponse> send(final HttpConsumerRequest request) {
        Objects.requireNonNull(request, "Expected request");
        return connect()
            .flatMap(connection -> connection.sendAndClose(request));
    }

    /**
     * Determines whether or not this HTTP client communicates via HTTPS or not.
     *
     * @return {@code true} only if this client uses HTTPS.
     */
    boolean isSecure();

    /**
     * Class used for creating {@link HttpConsumer} instances.
     * <p>
     * This class is primarily useful as input to the {@link
     * se.arkalix.query.ServiceQuery#oneUsing(ArConsumerFactory) oneUsing()}
     * and {@link se.arkalix.query.ServiceQuery#allUsing(ArConsumerFactory)
     * allUsing()} methods of the {@link se.arkalix.query.ServiceQuery
     * ServiceQuery} class, an instance of which is returned by the {@link
     * ArSystem#consume()} method. See the {@link se.arkalix.net.http.consumer
     * package documentation} for more details about how this class can be
     * used.
     * <p>
     * Use the {@link HttpConsumer#factory()} method to get an instance of this
     * class.
     */
    class Factory implements ArConsumerFactory<HttpConsumer> {
        private static final Factory instance = new Factory();

        @Override
        public Collection<TransportDescriptor> serviceTransports() {
            return Collections.singleton(HTTP);
        }

        @Override
        public HttpConsumer create(
            final ArSystem system,
            final ServiceDescription service,
            final Collection<EncodingDescriptor> encodings
        ) {
            return new DefaultHttpConsumer(system, service, encodings);
        }
    }
}
