package se.arkalix.plugin;

import java.lang.annotation.*;
import java.util.Set;

/**
 * Signifies that the annotated {@link Plugin} must be {@link
 * Plugin#onAttach(Plug, Set) attached} <i>after</i> and {@link
 * Plugin#onDetach(Plug) detached} <i>before</i> all {@link #value() identified}
 * plugins provided to the same {@link
 * se.arkalix.ArSystem.Builder#plugins(Plugin...) system}. If two plugins
 * provided to the systems both require to be attached after each other, an
 * exception is thrown and the system is not constructed.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginAfter {
    /**
     * @return Array of plugins that must be attached <i>after</i> and detached
     * <i>before</i> plugins with this annotation.
     */
    Class<? extends Plugin>[] value();
}
