package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.EventSubscriptionRequestDto;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an Arrowhead event subscription service.
 */
public interface ArEventSubscribe {
    /**
     * Sends given {@code subscription} request.
     *
     * @param subscription Subscription details.
     * @return {@code Future} completed when the subscription attempt is known
     * to have succeeded or failed.
     */
    Future<?> subscribe(EventSubscriptionRequestDto subscription);
}
