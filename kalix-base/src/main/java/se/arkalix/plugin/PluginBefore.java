package se.arkalix.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Signifies that the annotated {@link Plugin} must be {@link
 * Plugin#onAttach(Plug, Set) attached} <i>before</i> and {@link
 * Plugin#onDetach(Plug) detached} <i>after</i> all {@link #value() identified}
 * plugins provided to the same {@link
 * se.arkalix.ArSystem.Builder#plugins(Plugin...) system}. If two plugins
 * provided to the systems both require to be attached before each other, an
 * exception is thrown and the system is not constructed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginBefore {
    /**
     * @return Array of plugins that must be attached <i>after</i> and detached
     * <i>before</i> plugins with this annotation.
     */
    Class<? extends Plugin>[] value();
}
