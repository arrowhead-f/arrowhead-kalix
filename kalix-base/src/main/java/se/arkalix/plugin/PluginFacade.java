package se.arkalix.plugin;

import se.arkalix.ArSystem;

import java.util.Map;

/**
 * Allows for {@link Plugin plugins} {@link PluginAttached attached} to the
 * same {@link se.arkalix.ArSystem system} to interact with each other.
 *
 * @see PluginAttached#facade()
 * @see Plugin#attachTo(ArSystem, Map)
 */
public interface PluginFacade {}
