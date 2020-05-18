package se.arkalix.core.plugin.eh;

import java.util.Map;

/**
 * An event receiver associated with a particular topic, a set of possible
 * publishing systems and a required set of metadata parameters.
 */
@FunctionalInterface
public interface EventSubscriptionHandler {
    /**
     * Called to notify this handler about the availability of a matching
     * event.
     *
     * @param metadata Metadata in event.
     * @param data     Data in event.
     */
    void onPublish(final Map<String, String> metadata, final String data);
}
