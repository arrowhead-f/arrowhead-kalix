package se.arkalix.internal;

import se.arkalix.ArService;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;

/**
 * A server, making an {@link ArSystem} able to provide {@link ArService}s
 * relying on a particular application-level transport protocol.
 */
@Internal
public interface ArServer {
    /**
     * Tests whether this server can provide the given service.
     *
     * @param service Tested service.
     * @return {@code true} only if this server can provide the given service.
     */
    boolean canProvide(ArService service);

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
    Future<ArServiceHandle> provide(ArService service);

    /**
     * @return Immutable collection of handles representing all services
     * currently provided by this server.
     */
    Collection<ArServiceHandle> providedServices();

    /**
     * Shuts server down, making it impossible to start it again.
     * <p>
     * All services owned by this server are removed.
     *
     * @return Future completed when shutting down is complete.
     */
    Future<?> close();

}
