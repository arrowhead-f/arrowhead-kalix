package eu.arrowhead.kalix.internal;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Collection;

@Internal
public interface SystemServer {
    Future<InetSocketAddress> start();

    Future<?> stop();

    Collection<? extends ArrowheadService> providedServices();

    /**
     * Registers given {@code service} with server.
     * <p>
     * Calling this method with a service that is already being provided has no
     * effect.
     *
     * @param service Service to be provided by this server.
     * @return {@code true} only if {@code service} was not previously provided
     * by this server.
     * @throws NullPointerException  If {@code service} is {@code null}.
     * @throws IllegalStateException If {@code service} configuration conflicts
     *                               with an already provided service.
     */
    boolean provideService(final ArrowheadService service);

    /**
     * Deregisters given {@code service} from this server, immediately making
     * it inaccessible .
     * <p>
     * Calling this method with a service that is not currently provided has no
     * effect.
     *
     * @param service Service to no longer be provided by this server.
     * @return {@code true} only if a service was deregistered.
     * @throws NullPointerException If {@code service} is {@code null}.
     */
    boolean dismissService(final ArrowheadService service);

    /**
     * Deregisters all currently registered services from this server.
     */
    void dismissAllServices();
}
