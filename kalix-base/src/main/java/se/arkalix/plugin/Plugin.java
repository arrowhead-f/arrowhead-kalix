package se.arkalix.plugin;

import se.arkalix.ArSystem;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.concurrent.Future;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An {@link ArSystem} plugin.
 * <p>
 * A plugin attaches to one or more systems in order to react to certain
 * life-cycle events. A plugin could, for example, modify {@link
 * se.arkalix.ArService services} about to be {@link
 * ArSystem#provide(se.arkalix.ArService) provided} by its system, react to
 * services being {@link se.arkalix.ArServiceHandle#dismiss() dismissed}, or
 * help resolve {@link ArSystem#consume() service consumption queries}.
 * <p>
 * At a technical level, implementations of this interface can be regarded as
 * factory classes for creating {@link PluginAttached} instances via the {@link
 * #attachTo(ArSystem, Map)} method each of them provides.
 */
@SuppressWarnings("unused")
public interface Plugin {
    /**
     * Names plugins, if any, this plugin depend on.
     * <p>
     * If a plugin depended upon would be not be explicitly {@link
     * ArSystem.Builder#plugins(se.arkalix.plugin.Plugin...) provided} to the
     * same system as this plugin, instances of the dependencies will be
     * created automatically, if possible, and provided to the system in
     * question. Automatic dependency creation is possible only if each
     * dependency has either (1) a public static {@code instance()} method that
     * returns an instance of the plugin, or (2) a public constructor that
     * takes no arguments. Not having provided a dependency that cannot be
     * created automatically will prevent the {@link ArSystem} in question from
     * being instantiated at all.
     * <p>
     * Dependencies are always {@link #attachTo(ArSystem, Map)} attached}
     * <i>before</i> the plugins that depend on them.
     */
    default Set<Class<? extends Plugin>> dependencies() {
        return Collections.emptySet();
    }

    /**
     * Indication of when this {@code Plugin} should be attached in relation to
     * to other such provided to the same {@link ArSystem}. Plugins with lower
     * ordinals are loaded first.
     * <p>
     * If this {@code Plugin} has any {@link #dependencies() dependencies}, it
     * will be loaded after the plugins depended upon, irrespective of the
     * ordinals those plugins state.
     * <p>
     * Explicitly stating an ordinal by overriding this method will primarily
     * be relevant to plugins that provide functionality indirectly depended
     * upon by other plugins, or that indirectly depend on all other plugins.
     * If, for example, a plugin performs {@link
     * PluginAttached#onServiceQueried(ServiceQuery) service query resolution}, it might
     * be relevant to ensure it is loaded before other plugins that may depend
     * on it being available.
     *
     * @return Ordinal used for determining plugin {@link
     * #attachTo(ArSystem, Map) attachment} order.
     */
    default int ordinal() {
        return 0;
    }

    /**
     * Attaches plugin to given {@code system}.
     *
     * @param system       System to which this plugin is attached.
     * @param dependencies Mappings between the {@link #dependencies()
     *                     dependencies} of this plugin who provided {@link
     *                     PluginAttached#facade() facades} when attached.
     * @return {@link Future} that, if successful, completes with an object
     * useful for concretely handling the life-cycle events of the given
     * {@code system}.
     */
    Future<PluginAttached> attachTo(ArSystem system, Map<Class<? extends Plugin>, PluginFacade> dependencies)
        throws Exception;
}
