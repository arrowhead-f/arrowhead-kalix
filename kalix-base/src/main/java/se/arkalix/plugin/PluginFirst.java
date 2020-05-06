package se.arkalix.plugin;

import java.lang.annotation.*;
import java.util.Set;

/**
 * Signifies that the annotated {@link Plugin} must be {@link
 * Plugin#onAttach(Plug, Set) attached} <i>before</i> and {@link
 * Plugin#onDetach(Plug) detached} <i>after</i> all other plugins provided to
 * the same {@link se.arkalix.ArSystem.Builder#plugins(Plugin...) system}
 * without this annotation. If multiple plugins provided to the same system
 * have this annotation, the attachment and detachment orders are undefined
 * unless {@link PluginAfter} is used.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginFirst {}
