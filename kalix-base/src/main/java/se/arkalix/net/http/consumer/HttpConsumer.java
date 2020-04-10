package se.arkalix.net.http.consumer;

import se.arkalix.ArConsumer;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

import static se.arkalix.descriptor.TransportDescriptor.HTTP;

/**
 * Client useful for consuming an {@link se.arkalix.security secure or
 * insecure} HTTP {@link se.arkalix service}.
 * <p>
 * This class provides similar functionality to {@link HttpClient}, with the
 * exception that hostnames, encodings, authorization tokens and other
 * details are managed automatically.
 */
@SuppressWarnings("unused")
public class HttpConsumer implements ArConsumer {
    private static final HttpConsumerFactory factory = new HttpConsumerFactory();

    private final HttpClient client;
    private final ServiceDescription service;
    private final EncodingDescriptor encoding;
    private final String authorization;

    /**
     * Creates new consumer configured to support att encodings returned by
     * {@link EncodingDescriptor#dtoEncodings()}.
     *
     * @param client  HTTP client to use for consuming {@code service}.
     * @param service Service to consume.
     * @throws NullPointerException     If {@code client} or {@code service} is
     *                                  {@code null}.
     * @throws IllegalArgumentException If {@code client} is configured to use
     *                                  HTTPS and the {@link
     *                                  ServiceDescription#security()} method
     *                                  of the given {@code service} returns
     *                                  {@link SecurityDescriptor#NOT_SECURE},
     *                                  or if {@code client} is configured to
     *                                  use plain HTTP and the given {@code
     *                                  service} is secure returns any other
     *                                  security/access policy than
     *                                  {@link SecurityDescriptor#NOT_SECURE}.
     * @throws IllegalStateException    If {@code service} does not support any
     *                                  interface triplet compatible with the
     *                                  given {@code client} and any one out of
     *                                  the default DTO encodings.
     */
    public HttpConsumer(final HttpClient client, final ServiceDescription service) {
        this(client, service, null);
    }

    /**
     * Creates new consumer.
     *
     * @param client    HTTP client to use for consuming {@code service}.
     * @param service   Service to consume.
     * @param encodings Supported request/response encodings. If specified as
     *                  {@code null}, the encodings returned by
     *                  {@link EncodingDescriptor#dtoEncodings()} are used.
     * @throws NullPointerException     If {@code client} or {@code service} is
     *                                  {@code null}.
     * @throws IllegalArgumentException If {@code client} is configured to use
     *                                  HTTPS and the {@link
     *                                  ServiceDescription#security()} method
     *                                  of the given {@code service} returns
     *                                  {@link SecurityDescriptor#NOT_SECURE},
     *                                  or if {@code client} is configured to
     *                                  use plain HTTP and the given {@code
     *                                  service} is secure returns any other
     *                                  security/access policy than
     *                                  {@link SecurityDescriptor#NOT_SECURE}.
     * @throws IllegalStateException    If {@code service} does not support any
     *                                  interface triplet compatible with the
     *                                  given {@code client} and any one out of
     *                                  the given {@code encodings}.
     */
    public HttpConsumer(
        final HttpClient client,
        final ServiceDescription service,
        final Collection<EncodingDescriptor> encodings)
    {
        this.client = Objects.requireNonNull(client, "Expected client");
        this.service = Objects.requireNonNull(service, "Expected service");

        final var isSecure = service.security() != SecurityDescriptor.NOT_SECURE;

        if (isSecure != isSecure()) {
            if (isSecure) {
                throw new IllegalArgumentException("The provided HttpClient " +
                    "is configured to run in insecure mode, while the " +
                    "provided service \"" + service.name() + "\" is not; " +
                    "cannot consume service");
            }
            else {
                throw new IllegalArgumentException("The provided HttpClient " +
                    "is configured to run in secure mode, while the " +
                    "provided service \"" + service.name() + "\" is not; " +
                    "cannot consume service");
            }
        }

        if (isSecure && !client.isIdentifiable()) {
            throw new IllegalArgumentException("" +
                "The provided HttpClient is not associated with an owned " +
                "identity, even though secure mode is enabled; cannot " +
                "consume service");
        }

        final var compatibleEntry = service.interfaceTokens()
            .entrySet()
            .stream()
            .filter(entry -> {
                final var descriptor = entry.getKey();
                return descriptor.transport() == HTTP &&
                    descriptor.isSecure() == isSecure &&
                    encodings.contains(descriptor.encoding());
            })
            .sorted((a, b) -> b.getValue().length() - a.getValue().length())
            .findAny()
            .orElseThrow(() -> {
                final var builder = new StringBuilder("The service \"")
                    .append(service.name())
                    .append("\" does not support any ")
                    .append(isSecure ? "secure" : "insecure")
                    .append(" HTTP interface with any of the encodings [");

                var isFirst = true;
                for (final var encoding : encodings) {
                    if (!isFirst) {
                        builder.append(", ");
                    }
                    else {
                        isFirst = false;
                    }
                    builder.append(encoding.name());
                }

                builder.append("]; cannot consume service");

                return new IllegalStateException(builder.toString());
            });

        encoding = compatibleEntry.getKey().encoding();

        final var token = compatibleEntry.getValue();
        authorization = token != null && token.length() > 0
            ? "Bearer " + token
            : null;
    }

    /**
     * @return Default {@link HttpConsumerFactory}.
     */
    public static HttpConsumerFactory factory() {
        return factory;
    }

    @Override
    public ServiceDescription service() {
        return service;
    }

    /**
     * @return {@code true} only if this consumer is configured to run in
     * secure mode.
     */
    public boolean isSecure() {
        return client.isSecure();
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
            .mapResult(result -> {
                if (result.isFailure()) {
                    return Result.failure(result.fault());
                }
                final var connection = result.value();
                final SystemIdentity identity;
                if (isSecure()) {
                    identity = new SystemIdentity(connection.certificateChain());
                    if (!Objects.equals(identity.publicKey(), service.provider().publicKey())) {
                        connection.close();
                        return Result.failure(new HttpConsumerConnectionException("" +
                            "The public key known to be associated with the " +
                            "the consumed system \"" + service.provider().name() +
                            "\" does not match the public key in the " +
                            "certificate retrieved when connecting to it; " +
                            "cannot connect to service"));
                    }

                    if (!Objects.equals(client.identity().cloud(), identity.cloud())) {
                        connection.close();
                        return Result.failure(new HttpConsumerConnectionException("" +
                            "The consumed system \"" + service.provider().name() +
                            "\" does belong to the same local cloud as this " +
                            "system; cannot connect to service"));
                    }
                }
                else {
                    identity = null;
                }
                return Result.success(new HttpConsumerConnection(connection, encoding, identity, authorization));
            });
    }

    /**
     * Connects to the system providing the service represented by this
     * consumer, sends {@code request}, closes connection and then completes
     * the returned {@code Future} with the result.
     *
     * @param request Request to send.
     * @return {@link Future} completed with the request response or an error.
     * @throws NullPointerException If {@code request} is {@code null}.
     */
    public Future<HttpConsumerResponse> send(final HttpConsumerRequest request) {
        Objects.requireNonNull(request, "Expected request");
        return connect().flatMap(connection -> connection.sendAndClose(request));
    }
}
