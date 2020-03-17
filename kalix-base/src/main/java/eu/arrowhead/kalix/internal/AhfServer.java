package eu.arrowhead.kalix.internal;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.AhfServiceHandle;
import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.stream.Stream;

/**
 * A server, making an {@link AhfSystem} able to provide {@link AhfService}s
 * relying on a particular application-level transport protocol.
 */
@Internal
public interface AhfServer {
    boolean canProvide(AhfService service);

    /**
     * Registers given {@code service} with server and returns handle that can
     * be used to deregister the service at a later time.
     * <p>
     * Calling this method with a service that is already being provided will
     * result in either an exception being thrown or a the returned
     * {@code Future} being failed.
     *
     * @param service Service to be provided by this server.
     * @return {@code Future} completed with handle for provided service.
     * @throws NullPointerException  If {@code service} is {@code null}.
     * @throws IllegalStateException If {@code service} configuration conflicts
     *                               with an already provided service.
     */
    Future<AhfServiceHandle> provide(AhfService service);

    /**
     * @return Stream of handles representing all services currently provided
     * by this server.
     */
    Stream<AhfServiceHandle> providedServices();

    /**
     * Starts server, making its services available.
     *
     * @return Future completed with the socket address of the concrete local
     * network interface through which the server provides its services.
     */
    Future<InetSocketAddress> start();

    /**
     * Stops server, making its services unavailable without removing them.
     *
     * @return Future completed when stopping is complete.
     */
    Future<?> stop();

    /**
     * Shuts server down, making it impossible to start it again.
     * <p>
     * All services owned by this server are removed.
     *
     * @return Future completed when shutting down is complete.
     */
    Future<?> shutdown();
}
