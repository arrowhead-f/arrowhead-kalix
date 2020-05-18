package se.arkalix.core.plugin.cp;

import se.arkalix.core.plugin.eh.ArEventSubscriberPlugin;
import se.arkalix.plugin.Plugin;

import java.util.Set;

/**
 * A {@link Plugin plugin} that observes contract negotiation updates.
 * <p>
 * Use of this plugin requires that another plugin is available that performs
 * service resolution, such as the {@link
 * se.arkalix.core.plugin.HttpJsonCloudPlugin HttpJsonCloudPlugin}.
 */
public interface ArTrustedContractObserverPlugin extends Plugin {
    @Override
    default Set<Class<? extends Plugin>> dependencies() {
        return Set.of(ArEventSubscriberPlugin.class);
    }
}
