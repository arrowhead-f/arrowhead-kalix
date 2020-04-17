package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.core.plugin.dto.ErrorException;
import se.arkalix.core.plugin.dto.EventIncomingDto;
import se.arkalix.core.plugin.dto.SystemDetails;
import se.arkalix.core.plugin.internal.ArEventSubscription;
import se.arkalix.description.ProviderDescription;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.concurrent.Future;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    private final Collection<ArEventSubscription> subscriptions;
    private final String basePath;

    private HttpJsonEventSubscriptionPlugin(final Builder builder) {
        basePath = Objects.requireNonNull(builder.basePath, "Expected basePath");
        subscriptions = Objects.requireNonNull(builder.subscriptions, "Expected subscriptions").values();
    }

    @Override
    public void onAttach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("HTTP/JSON event subscription plugin attached to \"{}\"", plug.system().name());
        }
    }

    @Override
    public void afterAttach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("Registering event subscriptions of the \"{}\" system ...", plug.system().name());
        }

        final var system = plug.system();
        system.consume()
            .using(HttpJsonEventSubscribe.factory())
            .flatMap(eventSubscribe -> {
                if (logger.isInfoEnabled()) {
                    final var service = eventSubscribe.service();
                    final var provider = eventSubscribe.service().provider();
                    logger.info("Registering subscriptions via \"{}\" provided by \"{}\" {} for the \"{}\" system ...",
                        service.name(), provider.name(), provider.socketAddress(), plug.system().name());
                }

                final var subscriber = SystemDetails.from(system);
                final var eventSubscriber = new HttpService()
                    .name("event-subscriber")
                    .basePath(basePath)
                    .accessPolicy(AccessPolicy.whitelist(eventSubscribe.service().provider().name()))
                    .encodings(JSON);

                for (final var subscription : subscriptions) {
                    eventSubscriber.post("/" + subscription.topic(), (request, response) ->
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

                return system.provide(eventSubscriber)
                    .ifSuccess(serviceHandle -> {
                        for (final var subscription : subscriptions) {
                            final var sendToUri = basePath + "/" + subscription.topic();
                            final var subscriptionRequest = subscription.toSubscriberRequest(subscriber, sendToUri);
                            eventSubscribe.subscribe(subscriptionRequest)
                                .flatMapCatch(ErrorException.class, fault -> {
                                    final var error = fault.error();
                                    if ("INVALID_PARAMETER".equals(error.type())) {
                                        return system.consume()
                                            .using(HttpJsonEventUnsubscribe.factory())
                                            .flatMap(eventUnsubscribe -> eventUnsubscribe
                                                .unsubscribe(subscription.topic(), system))
                                            .flatMap(ignored -> eventSubscribe.subscribe(subscriptionRequest))
                                            .pass(null);
                                    }
                                    return Future.failure(fault);
                                })
                                .ifSuccess(ignored -> {
                                    if (logger.isInfoEnabled()) {
                                        logger.info("Registered {} for the \"{}\" system",
                                            subscription.toString(), plug.system().name());
                                    }
                                })
                                .onFailure(fault -> {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("Failed to register " + subscription, fault);
                                    }
                                });
                        }
                    })
                    .mapFault(Throwable.class, fault -> new Exception("" +
                        "Failed to setup event receiver for the \"" +
                        plug.system().name() + "\" system", fault));
            })
            .ifSuccess(ignored -> {
                if (logger.isInfoEnabled()) {
                    logger.info("Registered all event subscriptions of " +
                        "the \"{}\" system ...", plug.system().name());
                }
            })
            .onFailure(fault -> {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to register the event subscriptions " +
                        "of the \"" + plug.system().name() + "\" system", fault);
                }
            });
    }

    @Override
    public void beforeDetach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("Unregistering event subscriptions of the \"{}\" system ...", plug.system().name());
        }

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
                    logger.warn("Failed to unregister the event subscriptions " +
                        "of the \"" + plug.system().name() + "\" system", fault);
                }
            });
    }

    @Override
    public void onDetach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("HTTP/JSON event subscription plugin detached from \"{}\"", plug.system().name());
        }
    }

    @Override
    public void onDetach(final Plug plug, final Throwable cause) {
        if (logger.isErrorEnabled()) {
            logger.error("HTTP/JSON event subscription plugin forcibly " +
                "detached from \"" + plug.system().name() + "\"", cause);
        }
    }

    /**
     * Builder used to construct {@link HttpJsonEventSubscriptionPlugin}
     * instances.
     */
    public static class Builder {
        private final Map<String, ArEventSubscription> subscriptions = new HashMap<>();

        private String basePath;

        /**
         * Sets base path to be associated with {@link HttpService} that will
         * be set up to receive incoming events.
         *
         * @param basePath HTTP URI base path.
         * @return This builder.
         */
        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * Adds new desired subscription to builder.
         *
         * @param topic   Topic, or "eventType", that must be matched by
         *                received events. Topics are case insensitive and can
         *                only be subscribed to once.
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
         *                 received events. Topics are case insensitive and can
         *                 only be subscribed to once.
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
         *                  received events. Topics are case insensitive and
         *                  can only be subscribed to once.
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
         *                  received events. Topics are case insensitive and
         *                  can only be subscribed to once.
         * @param metadata  Metadata pairs that must be matched by received
         *                  events.
         * @param providers Event providers to receive events from.
         * @param handler   Handler to receive matching events.
         * @return This builder.
         */
        public Builder subscribe(
            String topic,
            final Map<String, String> metadata,
            final Collection<ProviderDescription> providers,
            final ArEventSubscriptionHandler handler)
        {
            topic = Objects.requireNonNull(topic, "Expected topic").toLowerCase();

            subscriptions.putIfAbsent(topic, new ArEventSubscription.Builder()
                .topic(topic)
                .metadata(metadata)
                .providers(providers)
                .handler(handler)
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
