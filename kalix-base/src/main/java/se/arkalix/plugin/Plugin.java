package se.arkalix.plugin;

import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.Collections;

/**
 * An {@link ArSystem} plugin.
 * <p>
 * A plugin attaches to one or more systems in order to react to certain
 * system life-cycle events. Plugins could, for example, be designed to react
 * to services being added to a system by adding authentication mechanisms, or
 * to bring up services in response to file system events, among many other
 * possible examples. More than anything else, they provide a convenient means
 * of packaging and reusing complex behaviors that are frequently useful
 * together.
 */
@SuppressWarnings({"unused", "RedundantThrows"})
public interface Plugin {
    /**
     * Called to notify the plugin that it now is attached to an
     * {@link ArSystem}.
     * <p>
     * This method is guaranteed to be called exactly once for every system the
     * plugin is attached to.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     *
     * @param plug Plug, representing the plugin's connection to the system.
     */
    default void onAttach(final Plug plug) throws Exception {
        final var class_ = this.getClass();
        final var logger = LoggerFactory.getLogger(class_);
        if (logger.isDebugEnabled()) {
            logger.debug("\"{}\" plugin attached to \"{}\"", class_, plug.system().name());
        }
    }

    /**
     * Called to notify the plugin that this, and all other plugins given at
     * {@link ArSystem.Builder#plugins(Plugin...) system creation}, have been
     * attached to an {@link ArSystem}.
     * <p>
     * This method is guaranteed to be called exactly once for every system the
     * plugin is attached to.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     *
     * @param plug Plug, representing the plugin's connection to the system.
     */
    default void afterAttach(final Plug plug) throws Exception {}

    /**
     * Called to notify the plugin that it now is detached from its
     * {@link ArSystem} and will receive no more notifications from it.
     * <p>
     * This event is caused either by (1) the {@link Plug#detach()} method of
     * the {@link Plug} owned by this plugin was called, or (2) the
     * {@link ArSystem} is being irreversibly shut down. The two scenarios
     * can be told apart via the {@link Plug#isSystemShuttingDown()} method.
     * This method is guaranteed to never be called more than once per attached
     * system.
     * <p>
     * If this method throws an exception {@link #onDetach(Plug, Throwable)} is
     * invoked with that exception.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void onDetach(final Plug plug) throws Exception {
        final var class_ = this.getClass();
        final var logger = LoggerFactory.getLogger(class_);
        if (logger.isDebugEnabled()) {
            logger.debug("\"{}\" plugin detached from \"{}\"", class_, plug.system().name());
        }
    }

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
    default void onDetach(final Plug plug, final Throwable cause) throws Exception {
        final var class_ = this.getClass();
        final var logger = LoggerFactory.getLogger(class_);
        if (logger.isErrorEnabled()) {
            logger.error("\"" + class_ + "\" plugin detached forcibly from \"" + plug.system().name() + "\"", cause);
        }
    }

    /**
     * Called to notify the plugin that it, and all other plugins attached to
     * the same {@link ArSystem}, are about to be {@link #onDetach(Plug)
     * detached} from the {@link ArSystem} in question.
     * <p>
     * If this method throws an exception {@link #onDetach(Plug, Throwable)} is
     * invoked with that exception.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void beforeDetach(final Plug plug) {}

    /**
     * Called to notify the plugin that a new service is prepared for being
     * provided by an attached {@link ArSystem}.
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
     * with the same exception. The plugin is not detached.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being added.
     * @return {@code Future} that must complete when this method is done
     * modifying or reacting to the given {@code service} being provided. If
     * the {@code Future} is completed with a fault, the service is never
     * provided and the fault is relayed to the caller trying to cause the
     * service to be provided.
     */
    @ThreadSafe
    default Future<?> onServicePrepared(final Plug plug, final ArService service) throws Exception {
        return Future.done();
    }

    /**
     * Called to notify the plugin that a new service is about to be provided
     * by an attached {@link ArSystem}.
     * <p>
     * By the time this method is called, it is known that the {@code service}
     * in question will be, or already is, provided by the system in question.
     * Furthermore, {@link #onServiceDismissed(Plug, ServiceDescription)} is
     * guaranteed to be called for every {@code service} this method is
     * provided, given that the application is not shutdown abnormally.
     * <p>
     * If this method throws an exception the returned {@code Future} is failed
     * with the same exception. The plugin is not detached.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being added.
     * @return {@code Future} that must complete when this method is done
     * reacting to the given {@code service} being provided. If the
     * {@code Future} is completed with a fault, the service is never provided
     * and the fault is relayed to the caller trying to cause the service to be
     * provided.
     */
    @ThreadSafe
    default Future<?> onServiceProvided(final Plug plug, final ServiceDescription service) throws Exception {
        return Future.done();
    }

    /**
     * Called to notify the plugin that an existing service is about to be
     * dismissed by an attached {@link ArSystem}.
     * <p>
     * If this method throws an exception the plugin is detached and
     * {@link #onDetach(Plug, Throwable)} is invoked with the exception.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service A description of the service being removed.
     */
    @ThreadSafe
    default void onServiceDismissed(final Plug plug, final ServiceDescription service) throws Exception {}

    /**
     * Called to notify the plugin that an attached {@link ArSystem} desires to
     * resolve the service described by the provided {@code query}.
     * <p>
     * If this method throws an exception the returned {@code Future} is failed
     * with the same exception. The plugin is not detached.
     *
     * @param plug  Plug, representing this plugin's connection to a system.
     * @param query An incomplete description of the service being queried.
     * @return {@code Future} that will complete with a collection of service
     * descriptions, out of which some <i>may</i> match the provided query.
     */
    @ThreadSafe
    default Future<Collection<ServiceDescription>> onServiceQueried(final Plug plug, final ServiceQuery query)
        throws Exception
    {
        return Future.success(Collections.emptyList());
    }
}
