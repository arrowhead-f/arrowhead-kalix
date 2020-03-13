package eu.arrowhead.kalix.plugin;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadServiceBuilder;
import eu.arrowhead.kalix.ArrowheadSystem;

/**
 * An {@link ArrowheadSystem} plugin.
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
     * {@link ArrowheadSystem}.
     * <p>
     * This method is guaranteed to be called exactly once for every system the
     * plugin is attached to.
     *
     * @param plug Plug, representing the plugin's connection to the system.
     */
    default void onAttach(final Plug plug) {}

    /**
     * Called to notify the plugin that it now is detached from its
     * {@link ArrowheadSystem}.
     * <p>
     * This event is caused either by (1) the {@link Plug#detach()} method of
     * the {@link Plug} owned by this plugin was called, or (2) the
     * {@link ArrowheadSystem} is being irreversibly shutdown due to its
     * {@link eu.arrowhead.kalix.util.concurrent.FutureScheduler scheduler}
     * being told to shut down. The method is guaranteed to never be called
     * more than once. The two scenarios can be told apart via the
     * {@link Plug#isSystemShuttingDown()} method. When this method is invoked,
     * no more events will be propagated to this system from the system.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void onDetach(final Plug plug) {}

    /**
     * Called to notify the plugin that a new service is about to be finished
     * being built.
     * <p>
     * As built services cannot be modified, this method provides the only
     * chance for this plugin to affect the internals of the service.
     * <p>
     * Note that There is typically no point in calling the
     * {@link ArrowheadServiceBuilder#build()} method, as it the service it
     * returns will not be added to the attached system. The builder must be
     * cast to a concrete service builder implementation to be of practical
     * use.
     * <p>
     * The {@link #onServiceProvided(Plug, ArrowheadService)} method is called
     * right after this one.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param builder Builder being used to configure a service.
     */
    default void onServiceBuilding(final Plug plug, final ArrowheadServiceBuilder builder) {}

    /**
     * Called to notify the plugin that a new service is about to be provided
     * by an attached {@link ArrowheadSystem}.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service The service being added.
     */
    default void onServiceProvided(final Plug plug, final ArrowheadService service) {}

    /**
     * Called to notify the plugin that an existing service is about to be
     * dismissed by an attached {@link ArrowheadSystem}.
     *
     * @param plug    Plug, representing this plugin's connection to a system.
     * @param service The service being removed.
     */
    default void onServiceDismissed(final Plug plug, final ArrowheadService service) {}

    /**
     * Called to notify the plugin that an attached {@link ArrowheadSystem} was
     * stopped and now started again due to being provided with a new service.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void onSystemStarted(final Plug plug) {}

    /**
     * Called to notify the plugin that an attached {@link ArrowheadSystem} is
     * being stopped.
     * <p>
     * This event is caused either by (1) the {@link ArrowheadSystem#stop()}
     * method of the {@link ArrowheadSystem} attached to this plugin being
     * called, or (2) the {@link ArrowheadSystem} being irreversibly shutdown
     * due to its
     * {@link eu.arrowhead.kalix.util.concurrent.FutureScheduler scheduler}
     * being told to shut down. In the case of the former, the system can be
     * started again. The two scenarios can be told apart via the
     * {@link Plug#isSystemShuttingDown()} method.
     *
     * @param plug Plug, representing this plugin's connection to a system.
     */
    default void onSystemStopped(final Plug plug) {}

}
