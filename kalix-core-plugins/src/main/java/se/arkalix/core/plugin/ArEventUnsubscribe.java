package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.EventSubscription;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead event subscription cancellation service.
 */
public interface ArEventUnsubscribe {
    /**
     * Sends request for a {@link ArEventSubscribe subscription} to be
     * cancelled.
     *
     * @param eventType      The subscription {@link EventSubscription#topic()
     *                       event type}.
     * @param subscriberName The name of the subscribing {@link
     *                       EventSubscription#subscriber() system}.
     * @param hostname       The hostname of the subscribing {@link
     *                       EventSubscription#subscriber() system}.
     * @param port           The port of the subscribing {@link
     *                       EventSubscription#subscriber() system}.
     * @return {@code Future} completed when the unsubscription attempt is
     * known to have succeeded or failed.
     */
    Future<?> unsubscribe(String eventType, String subscriberName, String hostname, int port);
}
