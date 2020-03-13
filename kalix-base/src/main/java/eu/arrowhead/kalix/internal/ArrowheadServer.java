package eu.arrowhead.kalix.internal;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * A server, making an {@link ArrowheadSystem} able to provide
 * {@link ArrowheadService}s relying on a particular application-level
 * transport protocol.
 */
@Internal
public abstract class ArrowheadServer {
    private final ArrowheadSystem system;

    protected ArrowheadServer(final ArrowheadSystem system) {
        this.system = system;
    }

    /**
     * Starts server.
     *
     * @return Future completed with the socket address of the concrete local
     * network interface through which the server provides its services.
     */
    public abstract Future<InetSocketAddress> start();

    /**
     * Deregisters all services and stops server.
     *
     * @return Future completed when stopping is complete.
     */
    public abstract Future<?> stop();

    /**
     * Determines whether this server would be able to provide the given
     * {@code service}.
     *
     * @param service Service to test.
     * @return {@code true} only if {@code service} can be provided by this
     * server.
     */
    public abstract boolean canProvide(final ArrowheadService service);

    /**
     * @return Collection of all services provided by this server.
     */
    public abstract Collection<? extends ArrowheadService> providedServices();

    /**
     * Registers given {@code service} with server.
     * <p>
     * Calling this method with a service that is already being provided has no
     * effect.
     *
     * @param service Service to be provided by this server.
     * @return {@code true} only if {@code service} is not already provided.
     * @throws NullPointerException  If {@code service} is {@code null}.
     * @throws IllegalStateException If {@code service} configuration conflicts
     *                               with an already provided service.
     */
    public abstract boolean provideService(final ArrowheadService service);

    /**
     * Deregisters given {@code service} from this server, immediately making
     * it inaccessible.
     * <p>
     * Calling this method with a service that is not currently provided has no
     * effect.
     *
     * @param service Service to no longer be provided by this server.
     * @return {@code true} only if {@code service} was being provided.
     * @throws NullPointerException If {@code service} is {@code null}.
     */
    public abstract boolean dismissService(final ArrowheadService service);

    /**
     * @return Arrowhead system owning this server.
     */
    protected ArrowheadSystem system() {
        return system;
    }
}
