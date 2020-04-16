package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.core.plugin.dto.EventIncomingDto;
import se.arkalix.core.plugin.dto.SystemDetails;
import se.arkalix.core.plugin.internal.ArEventSubscription;
import se.arkalix.description.ProviderDescription;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.security.access.AccessPolicy;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import static se.arkalix.descriptor.EncodingDescriptor.JSON;

/**
 * HTTP/JSON event subscription plugin.
 * <p>
 * This plugin helps manage subscriptions that last for the entire lifetime of
 * one or more systems. When a system given this plugin starts up, the plugin
 * looks up the "event-subscribe" and "event-unsubscribe" services, out of
 * which it is assumed to only exist one each, and uses the former service to
 * subscribe to all desired events. When the systems in question shuts down,
 * the latter service is used to unsubscribe to the same events.
 * <p>
 * Use of this plugin requires either that the mentioned services are preloaded
 * into the service cache of built systems, or that a plugin that performs
 * service resolution is loaded before this plugin. If a preloaded service
 * cache is used, make sure that its entries do not expire during the lifetime
 * of any subscribing systems.
 */
@SuppressWarnings("unused")
public class HttpJsonEventSubscriptionPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonEventSubscriptionPlugin.class);

    private final List<ArEventSubscription> subscriptions;
    private final String basePath;

    private HttpJsonEventSubscriptionPlugin(final Builder builder) {
        basePath = Objects.requireNonNull(builder.basePath, "Expected basePath");
        subscriptions = Objects.requireNonNull(builder.subscriptions, "Expected subscriptions");
    }

    @Override
    public void onAttach(final Plug plug) {
        final var system = plug.system();
        system.consume()
            .using(HttpJsonEventSubscribe.factory())
            .ifSuccess(consumer -> {
                final var subscriber = SystemDetails.from(system);
                final var eventSubscriber = new HttpService()
                    .name("event-subscriber")
                    .basePath(basePath)
                    .accessPolicy(AccessPolicy.whitelist(consumer.service().provider().name()))
                    .encodings(JSON);

                for (final var subscription : subscriptions) {
                    eventSubscriber.post(subscription.uri(), (request, response) ->
                        request.bodyAs(EventIncomingDto.class)
                            .ifSuccess(event -> {
                                try {
                                    subscription.publish(event.metadata(), event.data());
                                }
                                finally {
                                    response.status(HttpStatus.OK);
                                }
                            })
                            .mapCatch(Throwable.class, fault -> {
                                if (logger.isWarnEnabled()) {
                                    logger.warn("Failed to read received " +
                                        "event " + subscription, fault);
                                }
                                response.status(HttpStatus.BAD_REQUEST);
                                return null;
                            }));
                }

                system.provide(eventSubscriber);

                for (final var subscription : subscriptions) {
                    consumer.subscribe(subscription.toSubscriberRequest(subscriber, basePath))
                        .onFailure(fault -> {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Failed to register " + subscription, fault);
                            }
                        });
                }

            })
            .onFailure(fault -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to access event-subscribe service", fault);
                }
            });
    }

    @Override
    public void onDetach(final Plug plug) {
        final var system = plug.system();
        system.consume()
            .using(HttpJsonEventUnsubscribe.factory())
            .ifSuccess(consumer -> {
                for (final var subscription : subscriptions) {
                    consumer.unsubscribe(subscription.topic(), system)
                        .onFailure(fault -> {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Failed to unregister " + subscription, fault);
                            }
                        });
                }
            })
            .onFailure(fault -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to access event-unsubscribe service", fault);
                }
            });
    }

    /**
     * Builder used to construct {@link HttpJsonEventSubscriptionPlugin}
     * instances.
     */
    public static class Builder {
        private final String basePath;
        private final List<ArEventSubscription> subscriptions = new ArrayList<>();

        /**
         * Creates new builder.
         */
        public Builder() {
            try {
                final var nounce = new byte[5];
                SecureRandom.getInstanceStrong().nextBytes(nounce);
                basePath = "/" + Base64.getUrlEncoder().encodeToString(nounce);
            }
            catch (final NoSuchAlgorithmException exception) {
                throw new RuntimeException(exception);
            }
        }

        /**
         * Adds new desired subscription to builder.
         *
         * @param topic   Topic, or "eventType", that must be matched by
         *                received events.
         * @param handler Handler to receive matching events.
         * @return This builder.
         */
        public Builder subscribe(final String topic, final ArEventSubscriptionHandler handler) {
            return subscribe(topic, null, null, handler);
        }

        /**
         * Adds new desired subscription to builder.
         *
         * @param topic    Topic, or "eventType", that must be matched by
         *                 received events.
         * @param metadata Metadata pairs that must be matched by received
         *                 events.
         * @param handler  Handler to receive matching events.
         * @return This builder.
         */
        public Builder subscribe(
            final String topic,
            final Map<String, String> metadata,
            final ArEventSubscriptionHandler handler)
        {
            return subscribe(topic, metadata, null, handler);
        }

        /**
         * Adds new desired subscription to builder.
         *
         * @param topic     Topic, or "eventType", that must be matched by
         *                  received events.
         * @param providers Event providers to receive events from.
         * @param handler   Handler to receive matching events.
         * @return This builder.
         */
        public Builder subscribe(
            final String topic,
            final Collection<ProviderDescription> providers,
            final ArEventSubscriptionHandler handler)
        {
            return subscribe(topic, null, providers, handler);
        }

        /**
         * Adds new desired subscription to builder.
         *
         * @param topic     Topic, or "eventType", that must be matched by
         *                  received events.
         * @param metadata  Metadata pairs that must be matched by received
         *                  events.
         * @param providers Event providers to receive events from.
         * @param handler   Handler to receive matching events.
         * @return This builder.
         */
        public Builder subscribe(
            final String topic,
            final Map<String, String> metadata,
            final Collection<ProviderDescription> providers,
            final ArEventSubscriptionHandler handler)
        {
            final String uri;
            try {
                final var nounce = new byte[20];
                SecureRandom.getInstanceStrong().nextBytes(nounce);
                uri = Base64.getUrlEncoder().encodeToString(nounce);
            }
            catch (final NoSuchAlgorithmException exception) {
                throw new RuntimeException(exception);
            }
            subscriptions.add(new ArEventSubscription.Builder()
                .topic(topic)
                .metadata(metadata)
                .providers(providers)
                .handler(handler)
                .uri(uri)
                .build());
            return this;
        }

        /**
         * Finalizes construction of {@link HttpJsonEventSubscriptionPlugin}.
         *
         * @return New {@link HttpJsonEventSubscriptionPlugin}.
         */
        public HttpJsonEventSubscriptionPlugin build() {
            return new HttpJsonEventSubscriptionPlugin(this);
        }
    }
}
