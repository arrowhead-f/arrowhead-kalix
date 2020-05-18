package se.arkalix.core.plugin.eh;

import se.arkalix.ArSystem;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead event subscription cancellation service.
 */
public interface ArEventUnsubscribeService {
    /**
     * Sends request for a {@link ArEventSubscribeService subscription} to be
     * cancelled.
     *
     * @param topic          The subscription
     *                       {@link EventSubscriptionRequest#topic() event
     *                       category}.
     * @param subscriberName The name of the subscribing {@link
     *                       EventSubscriptionRequest#subscriber() system}.
     * @param hostname       The hostname of the subscribing {@link
     *                       EventSubscriptionRequest#subscriber() system}.
     * @param port           The port of the subscribing {@link
     *                       EventSubscriptionRequest#subscriber() system}.
     * @return {@code Future} completed when the unsubscription attempt is
     * known to have succeeded or failed.
     */
    Future<?> unsubscribe(String topic, String subscriberName, String hostname, int port);

    /**
     * Sends request for a {@link ArEventSubscribeService subscription} to be
     * cancelled.
     *
     * @param topic      The subscription
     *                   {@link EventSubscriptionRequest#topic() event
     *                   category}.
     * @param subscriber The system currently receiving the events in question.
     * @return {@code Future} completed when the unsubscription attempt is
     * known to have succeeded or failed.
     */
    default Future<?> unsubscribe(final String topic, final ArSystem subscriber) {
        return unsubscribe(
            topic,
            subscriber.name(),
            subscriber.localSocketAddress().getHostString(),
            subscriber.localPort());
    }
}
