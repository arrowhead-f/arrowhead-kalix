package eu.arrowhead.kalix.plugin;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.util.concurrent.Future;

/**
 * An {@link AhfSystem} plugin.
 * <p>
 * A plugin attaches to one or more systems in order to react to certain
 * system life-cycle events. Plugins could, for example, be designed to react
 * to services being added to a system by adding authentication mechanisms, or
 * to bring up services in response to file system events, among many other
 * possible examples. More then anything else, they provide a convenient means
 * of packaging and reusing complex behaviors that are frequently useful
 * together.
 */
public interface Plugin {
    /**
     * Called to notify the plugin that it now is attached to an
     * {@link AhfSystem}.
     * <p>
     * This method is guaranteed to be called exactly once for every system the
     * plugin is attached to.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     *
     * @param plug Plug, representing the plugin's connection to the system.
     */
    default void onAttach(final Plug plug) {}

    /**
     * Called to notify the plugin that it now is detached from its
     * {@link AhfSystem} and will receive no more notifications from it.
     * <p>
     * This event is caused either by (1) the {@link Plug#detach()} method of
     * the {@link Plug} owned by this plugin was called, or (2) the
     * {@link AhfSystem} is being irreversibly shut down. The two scenarios
     * can be told apart via the {@link Plug#isSystemShuttingDown()} method.
     * This method is guaranteed to never be called more than once per attached
     * system.
     * <p>
     * If this method throws an exception {@link #onDetach(Plug, Throwable)} is
     * invoked with that exception.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void onDetach(final Plug plug) {}

    /**
     * Called to notify the plugin that it was forcibly detached due to
     * unexpectedly throwing an exception when one of its methods was called,
     * or that {@link #onDetach(Plug)} threw an exception.
     * <p>
     * If this method throws an exception it will not be called again.
     *
     * @param plug  Plug, representing this plugin's connection to a system.
     * @param cause The exception causing the plugin to be detached.
     */
    default void onDetach(final Plug plug, final Throwable cause) {}

    /**
     * Called to notify the plugin that an attached {@link AhfSystem}
     * went from being stopped to being started.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     * Exceptions intended for the system starter must be provided via the
     * returned {@link Future}.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     * @return {@code Future} that must complete when this method is done
     * reacting to the attached system being started. If the {@code Future} is
     * completed with a fault, the service is never provided and the fault is
     * relayed to the caller trying to cause the service to be provided.
     */
    default Future<?> onSystemStarted(final Plug plug) {
        return Future.done();
    }

    /**
     * Called to notify the plugin that an attached {@link AhfSystem} is
     * being stopped.
     * <p>
     * This event is caused either by (1) the {@link Plug#detach()} method of
     * the {@link Plug} owned by this plugin was called, or (2) the
     * {@link AhfSystem} is being irreversibly shut down. The two scenarios
     * can be told apart via the {@link Plug#isSystemShuttingDown()} method.
     * This method is guaranteed to be called before {@link #onDetach(Plug)} if
     * a system is shutting down.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     * Exceptions intended for the system stopper must be provided via the
     * returned {@link Future}.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default Future<?> onSystemStopped(final Plug plug) {
        return Future.done();
    }

    /**
     * Called to notify the plugin that a new service is prepared for being
     * provided by an attached {@link AhfSystem}.
     * <p>
     * This method provides the only opportunity for this plugin to affect the
     * internals of provided services. Note, however, that it being called does
     * not guarantee that {@link #onServiceProvided(Plug, ServiceDescription)}
     * will be called for the same {@code service}, as when this method is
     * invoked it has not yet been verified if the {@code service} has been
     * configured correctly or if its configuration clashes with an existing
     * service. This method should <i>never</i> assume that services it is
     * provided will be provided by the attached system in question.
     * <p>
     * If this method throws an exception the returned {@code Future} is failed
     * with the same exception.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being added.
     * @return {@code Future} that must complete when this method is done
     * modifying or reacting to the given {@code service} being provided. If
     * the {@code Future} is completed with a fault, the service is never
     * provided and the fault is relayed to the caller trying to cause the
     * service to be provided.
     */
    default Future<?> onServicePrepared(final Plug plug, final AhfService service) {
        return Future.done();
    }

    /**
     * Called to notify the plugin that a new service is about to be provided
     * by an attached {@link AhfSystem}.
     * <p>
     * By the time this method is called, it is known that the {@code service}
     * in question will be, or already is, provided by the system in question.
     * Furthermore, {@link #onServiceDismissed(Plug, ServiceDescription)} is
     * guaranteed to be called for every {@code service} this method is
     * provided, given that the application is not shutdown abnormally.
     * <p>
     * If this method throws an exception the returned {@code Future} is failed
     * with the same exception.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being added.
     * @return {@code Future} that must complete when this method is done
     * reacting to the given {@code service} being provided. If the
     * {@code Future} is completed with a fault, the service is never provided
     * and the fault is relayed to the caller trying to cause the service to be
     * provided.
     */
    default Future<?> onServiceProvided(final Plug plug, final ServiceDescription service) {
        return Future.done();
    }

    /**
     * Called to notify the plugin that an existing service is about to be
     * dismissed by an attached {@link AhfSystem}.
     * <p>
     * If this method throws an exception the returned {@code Future} is failed
     * with the same exception.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being removed.
     * @return {@code Future} that must complete when this method is done
     * reacting to the described {@code service} being dismissed. If the
     * {@code Future} is completed with a fault, the service is never dismissed
     * and the fault is relayed to the caller trying to cause the service to be
     * dismissed.
     */
    default Future<?> onServiceDismissed(final Plug plug, final ServiceDescription service) {
        return Future.done();
    }
}
