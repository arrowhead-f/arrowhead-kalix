package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.EventDto;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead event publishing service.
 */
public interface ArEventPublish {
    /**
     * Publishes given {@code event}.
     *
     * @param event Event to publish.
     * @return {@code Future} completed when the publishing attempt is known to
     * have succeeded or failed.
     */
    Future<?> publish(EventDto event);
}
