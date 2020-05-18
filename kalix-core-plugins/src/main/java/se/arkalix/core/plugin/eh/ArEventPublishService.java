package se.arkalix.core.plugin.eh;

import se.arkalix.ArSystem;
import se.arkalix.internal.core.plugin.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.util.concurrent.Future;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an Arrowhead event publishing service.
 */
@SuppressWarnings("unused")
public interface ArEventPublishService {
    /**
     * Publishes given {@code event}.
     *
     * @param event Event to publish.
     * @return {@code Future} completed when the publishing attempt is known to
     * have succeeded or failed.
     */
    Future<?> publish(EventOutgoingDto event);

    /**
     * Publishes given arguments as an {@link EventOutgoing}.
     *
     * @param topic     Category of event.
     * @param publisher System publishing the event.
     * @param data      Arbitrary string data associated with event.
     * @return {@code Future} completed when the publishing attempt is known to
     * have succeeded or failed.
     */
    default Future<?> publish(final String topic, final ArSystem publisher, final String data) {
        return publish(topic, publisher, null, data);
    }

    /**
     * Publishes given arguments as an {@link EventOutgoing}.
     *
     * @param topic     Category of event.
     * @param publisher System publishing the event.
     * @param metadata  Arbitrary metadata associated with event.
     * @param data      Arbitrary string data associated with event.
     * @return {@code Future} completed when the publishing attempt is known to
     * have succeeded or failed.
     */
    default Future<?> publish(
        final String topic,
        final ArSystem publisher,
        final Map<String, String> metadata,
        final String data)
    {
        return publish(new EventOutgoingBuilder()
            .topic(topic)
            .publisher(SystemDetails.from(publisher))
            .metadata(metadata)
            .data(data)
            .createdAt(Instants.toAitiaDateTimeString(Instant.now()))
            .build());
    }
}
