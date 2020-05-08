package se.arkalix.core.plugin.eh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.ErrorException;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static se.arkalix.descriptor.EncodingDescriptor.JSON;

/**
 * HTTP/JSON event subscriber plugin.
 * <p>
 * This plugin helps manage subscriptions for events using the {@link
 * HttpJsonEventSubscribe} and {@link HttpJsonEventUnsubscribe} services. It
 * assumes that only one version of each of these services are available to the
 * {@link ArSystem} it attaches to, and uses them to register and deregister
 * event subscriptions.
 * <p>
 * Subscriptions are currently assumed to remain valid for the entire lifetime
 * of the system this plugin attaches to. No renewal attempts are made
 * automatically.
 * <p>
 * Use of this plugin requires either that the mentioned services are preloaded
 * into the service cache of built systems, or that a plugin that performs
 * service resolution is loaded before this plugin. If a preloaded service
 * cache is used, make sure that its entries do not expire during the lifetime
 * of any subscribing systems.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class HttpJsonEventSubscriberPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonEventSubscriberPlugin.class);

    @Override
    public PluginAttached attachTo(
        final ArSystem system,
        final Map<Class<? extends Plugin>, PluginFacade> dependencies)
    {
        return new Attached(system);
    }

    public static class Attached implements PluginAttached {
        private final String basePath;
        private final ArSystem system;
        private final SystemDetailsDto subscriber;

        private final ConcurrentHashMap<String, SubscriptionHandle> topicToSubscription = new ConcurrentHashMap<>();
        private final AtomicBoolean isDetached = new AtomicBoolean(false);

        Attached(final ArSystem system) {
            logger.info("Attaching to system \"{}\" ...", system.name());

            this.system = system;
            this.subscriber = SystemDetails.from(this.system);
            this.basePath = "/events/" + this.system.name();

            system.consume()
                .using(HttpJsonEventSubscribe.factory())
                .flatMap(eventSubscribe -> {
                    if (logger.isInfoEnabled()) {
                        final var service = eventSubscribe.service();
                        final var provider = eventSubscribe.service().provider();
                        logger.info("Registering system \"{}\" event subscriptions via \"{}\" provided by \"{}\" {} ...",
                            system.name(), service.name(), provider.name(), provider.socketAddress());
                    }

                    final var service = new HttpService()
                        .name("event-subscriber")
                        .basePath(this.basePath)
                        .accessPolicy(AccessPolicy.whitelist(eventSubscribe.service().provider().name()))
                        .encodings(JSON)

                        .post("/#topic", (request, response) -> {
                            final var topic = request.pathParameter(0);
                            return request.bodyAs(EventIncomingDto.class)
                                .ifSuccess(event -> {
                                    try {
                                        final var subscription = topicToSubscription.get(topic.toLowerCase());
                                        if (subscription != null) {
                                            subscription.publish(event.metadata(), event.data());
                                        }
                                        else if (logger.isWarnEnabled()) {
                                            logger.warn("Received unexpected event [topic=" + topic + "]: {}", event);
                                        }
                                    }
                                    catch (final Throwable throwable) {
                                        if (logger.isErrorEnabled()) {
                                            logger.error("Unexpected event handler exception [topic=" + topic + ']', throwable);
                                        }
                                    }
                                    finally {
                                        response.status(HttpStatus.OK);
                                    }
                                })
                                .mapCatch(Throwable.class, fault -> {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("Failed to handle received event [topic=" + topic + "]", fault);
                                    }
                                    if (response.status().isEmpty()) {
                                        response.status(HttpStatus.BAD_REQUEST);
                                    }
                                    return null;
                                });
                        });


                    return system.provide(service)
                        .mapFault(Throwable.class, fault -> new Exception("" +
                            "Failed to setup event receiver for the \"" +
                            system.name() + "\" system", fault));
                })
                .ifSuccess(ignored -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("Attached to system \"{}\" and registered " +
                            "all pending event subscriptions", system.name());
                    }
                })
                .onFailure(fault -> {
                    if (logger.isErrorEnabled()) {
                        logger.error("Failed to attach to system \"" +
                            system.name() + "\"", fault);
                    }
                });
        }

        @Override
        public void onDetach() {
            if (isDetached.getAndSet(true)) {
                return;
            }

            if (logger.isInfoEnabled()) {
                logger.info("Detaching from system \"{}\" and unregistering " +
                    "event subscriptions ...", system.name());
            }

            system.consume()
                .using(HttpJsonEventUnsubscribe.factory())
                .ifSuccess(consumer -> {
                    for (final var subscription : topicToSubscription.values()) {
                        subscription.unsubscribe();
                    }
                    logger.info("Detached from system \"{}\"", system.name());
                })
                .onFailure(fault -> {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to unregister the event subscriptions " +
                            "of the \"" + system.name() + "\" system", fault);
                    }
                });
        }

        private class Facade implements ArEventSubscriberPluginFacade {
            @Override
            public Future<ArEventSubscriptionHandle> subscribe(final ArEventSubscription subscription) {
                if (isDetached.get()) {
                    throw new IllegalStateException("Plugin \"" +
                        HttpJsonEventSubscriberPlugin.class +
                        "\" is detached from its system \"" + system.name() +
                        "\"; cannot create subscription");
                }
                return new FutureSubscription(subscription);
            }
        }

        private class FutureSubscription implements Future<ArEventSubscriptionHandle> {
            private Future<?> cancellableFuture;
            private Consumer<Result<ArEventSubscriptionHandle>> consumer = null;

            public FutureSubscription(final ArEventSubscription subscription) {
                final ArEventSubscription subscription1 = Objects.requireNonNull(subscription);

                final var topic = subscription.topic()
                    .orElseThrow(() -> new IllegalArgumentException("Expected subscription topic to be set"));

                final var handler = subscription.handler()
                    .orElseThrow(() -> new IllegalArgumentException("Expected subscription handler to be set"));

                final var subscriptionHandle = new SubscriptionHandle(topic, handler);
                final var existingSubscription = topicToSubscription.putIfAbsent(topic, subscriptionHandle);
                if (existingSubscription != null) {
                    throw new IllegalStateException("A subscription already exists " +
                        "for the topic \"" + topic + "\" " + existingSubscription +
                        "; cannot register " + subscription);
                }

                final var request = subscription.toSubscriptionRequest(subscriber, basePath + '/' + topic);
                logger.info("Subscribing system \"{}\" to topic \"{}\" ...", system.name(), topic);
                cancellableFuture = system.consume()
                    .using(HttpJsonEventSubscribe.factory())
                    .flatMap(eventSubscribe -> eventSubscribe.subscribe(request)
                        .flatMapCatch(ErrorException.class, fault -> {
                            final var error = fault.error();
                            if ("INVALID_PARAMETER".equals(error.type())) {
                                return system.consume()
                                    .using(HttpJsonEventUnsubscribe.factory())
                                    .flatMap(eventUnsubscribe -> eventUnsubscribe
                                        .unsubscribe(topic, system))
                                    .flatMap(ignored -> eventSubscribe.subscribe(request))
                                    .pass(null);
                            }
                            return Future.failure(fault);
                        }));

                cancellableFuture
                    .ifSuccess(ignored -> {
                        logger.info("Subscribed system \"{}\" to topic \"{}\"", system.name(), topic);
                        if (consumer != null) {
                            consumer.accept(Result.success(subscriptionHandle));
                        }
                    })
                    .onFailure(fault -> {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Subscribing system \"" + system.name() +
                                "\" to topic \"" + topic + "\" failed", fault);
                        }
                        topicToSubscription.remove(topic);
                        if (consumer != null) {
                            consumer.accept(Result.failure(fault));
                        }
                    });
            }

            @Override
            public void onResult(final Consumer<Result<ArEventSubscriptionHandle>> consumer) {
                this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                if (cancellableFuture != null) {
                    cancellableFuture.cancel(mayInterruptIfRunning);
                    cancellableFuture = null;
                }
            }
        }

        private class SubscriptionHandle implements ArEventSubscriptionHandle {
            private final String topic;
            private final ArEventSubscriptionHandler handler;
            private final AtomicBoolean isUnsubscribed = new AtomicBoolean(false);

            public SubscriptionHandle(final String topic, final ArEventSubscriptionHandler handler) {
                this.topic = Objects.requireNonNull(topic, "Expected topic");
                this.handler = Objects.requireNonNull(handler, "Expected handler");
            }

            public void publish(final Map<String, String> metadata, final String data) {
                handler.onPublish(
                    Objects.requireNonNullElse(metadata, Collections.emptyMap()),
                    Objects.requireNonNull(data, "Expected data"));
            }

            @Override
            public void unsubscribe() {
                if (!isUnsubscribed.compareAndSet(false, true)) {
                    return;
                }
                logger.info("Unsubscribing system \"{}\" from topic \"{}\" ...", system.name(), topic);
                topicToSubscription.remove(topic);
                system.consume()
                    .using(HttpJsonEventUnsubscribe.factory())
                    .flatMap(eventUnsubscribe -> eventUnsubscribe.unsubscribe(topic, system))
                    .ifSuccess(ignored -> logger.info("Unsubscribed " +
                        "system \"{}\" from topic \"{}\"", system.name(), topic))
                    .onFailure(fault -> logger.warn("Failed to unsubscribe " +
                        "system \"" + system.name() + "\" from topic \"" + topic + "\"", fault));
            }
        }
    }
}
