package se.arkalix.plugin;

import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * The part of a {@link Plugin} that concretely handles {@link ArSystem} life-
 * cycle events.
 * <p>
 * All methods specified in this interface, except for {@link #onDetach()} and
 * {@link #onDetach(Throwable)}, must be thread-safe.
 */
@SuppressWarnings({"unused", "RedundantThrows"})
public interface PluginAttached {
    /**
     * Gets facade of attached plugin.
     * <p>
     * A plugin facade is an arbitrary implementation of the {@link
     * PluginFacade} interface that allows for other plugins attached to the
     * same system to interact with this plugin. Care should be taken to make
     * this method, if overridden, and the facade or facades it returns,
     * thread-safe.
     *
     * @return Plugin facade, if any.
     */
    @ThreadSafe
    default Optional<PluginFacade> facade() {
        return Optional.empty();
    }

    /**
     * Invoked when this plugin is about to be detached from its {@link
     * ArSystem}. A detached plugin is not notified of system life-cycle events.
     * <p>
     * This method is guaranteed to never be called more than once.
     * <p>
     * If this method throws an exception, {@link #onDetach(Throwable)} is
     * invoked with that exception.
     */
    default void onDetach() {}

    /**
     * Called to notify this plugin that it is about to be forcibly detached due
     * to unexpectedly throwing an exception when one of its methods was called,
     * or that {@link #onDetach()} threw an exception.
     * <p>
     * If this method throws an exception it will not be called again.
     *
     * @param cause The exception causing the plugin to be detached.
     */
    default void onDetach(final Throwable cause) {}

    /**
     * Called to notify this plugin that a new service is prepared for being
     * provided by its {@link ArSystem}.
     * <p>
     * This method provides the only opportunity for this plugin to affect the
     * internals of provided services. Note, however, that it being called does
     * not guarantee that {@link #onServiceProvided(ServiceRecord)} will
     * be called for the same {@code service}, as when this method is invoked
     * it has not yet been verified if the {@code service} has been configured
     * correctly or if its configuration clashes with an existing service. This
     * method must <i>never</i> assume that services it is given will be {@link
     * ArSystem#provide(ArService) provided} by its system.
     * <p>
     * If this method throws an exception the returned {@link Future} is failed
     * with the same exception. The plugin is not detached by uncaught
     * exceptions.
     *
     * @param service The service being added.
     * @return {@link Future} that must complete when this method is done
     * modifying or reacting to the given {@code service} being provided. If
     * the {@link Future} is completed with a fault, the service is never
     * provided and the fault is relayed to the caller trying to cause the
     * service to be provided.
     */
    @ThreadSafe
    default Future<?> onServicePrepared(final ArService service) throws Exception {
        return Future.done();
    }

    /**
     * Called to notify the plugin that a new service is about to be provided
     * by its {@link ArSystem}.
     * <p>
     * By the time this method is called, it is known that the {@code service}
     * in question will be, or already is, provided by this plugin's system.
     * Furthermore, {@link #onServiceDismissed(ServiceRecord)} is
     * guaranteed to be called for every {@code service} this method is
     * provided, given that the application is not shutdown abnormally.
     * <p>
     * If this method throws an exception the returned {@link Future} is failed
     * with the same exception. The plugin is not detached by uncaught
     * exceptions.
     *
     * @param service A description of the service being added.
     * @return {@link Future} that must complete when this method is done
     * reacting to the given {@code service} being provided. If the
     * {@link Future} is completed with a fault, the service is never provided
     * and the fault is relayed to the caller trying to cause the service to be
     * provided.
     */
    @ThreadSafe
    default Future<?> onServiceProvided(final ServiceRecord service) throws Exception {
        return Future.done();
    }

    /**
     * Called to notify the plugin that a service currently provided by its
     * {@link ArSystem} is about to be dismissed.
     * <p>
     * If this method throws an exception the plugin is detached and {@link
     * #onDetach(Throwable)} is invoked with the exception.
     *
     * @param service A description of the service being removed.
     */
    @ThreadSafe
    default void onServiceDismissed(final ServiceRecord service) {}

    /**
     * Called to notify the plugin that its {@link ArSystem} desires to resolve
     * the service described by the provided {@code query}.
     * <p>
     * If this method throws an exception the returned {@link Future} is failed
     * with the same exception. The plugin is not detached by uncaught
     * exceptions.
     *
     * @param query An incomplete description of the service being queried.
     * @return {@link Future} that will complete with a collection of service
     * descriptions, out of which some <i>may</i> match the provided query.
     */
    @ThreadSafe
    default Future<Collection<ServiceRecord>> onServiceQueried(final ServiceQuery query)
        throws Exception
    {
        return Future.success(Collections.emptyList());
    }
}
