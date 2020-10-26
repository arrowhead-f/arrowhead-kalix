package se.arkalix.core.plugin.eh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.CloudException;
import se.arkalix.core.plugin.ErrorResponseException;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.SystemRecord;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static se.arkalix.net.Encoding.JSON;

/**
 * HTTP/JSON event subscriber plugin.
 * <p>
 * This plugin helps manage subscriptions for events using the {@link
 * HttpJsonEventSubscribeService} and {@link HttpJsonEventUnsubscribeService} services. It
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
public class HttpJsonEventSubscriberPlugin implements ArEventSubscriberPlugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonEventSubscriberPlugin.class);

    private final List<EventSubscription> defaultSubscriptions = new CopyOnWriteArrayList<>();

    @Override
    public HttpJsonEventSubscriberPlugin subscribe(final EventSubscription subscription) {
        defaultSubscriptions.add(subscription);
        return this;
    }

    @Override
    public Future<PluginAttached> attachTo(
        final ArSystem system,
        final Map<Class<? extends Plugin>, PluginFacade> dependencies)
    {
        logger.info("HTTP/JSON event subscriber attaching to system \"{}\" ...", system.name());

        final var pluginAttached = new Attached(system);
        return pluginAttached.registerEventReceiver(defaultSubscriptions)
            .pass(pluginAttached);
    }

    private static class Attached implements PluginAttached {
        private final Facade facade = new Facade();

        private final String basePath;
        private final ArSystem system;
        private final SystemDetailsDto subscriber;

        private final ConcurrentHashMap<String, Topic> nameToTopic = new ConcurrentHashMap<>();
        private final AtomicBoolean isDetached = new AtomicBoolean(false);

        Attached(final ArSystem system) {
            this.system = system;
            this.subscriber = SystemDetails.from(this.system);
            this.basePath = "/events/" + system.name();
        }

        private Future<?> registerEventReceiver(final List<EventSubscription> defaultSubscriptions) {
            return system.consume()
                .oneUsing(HttpJsonEventSubscribeService.factory())
                .flatMap(eventSubscribe -> {
                    if (logger.isInfoEnabled()) {
                        final var service = eventSubscribe.service();
                        final var provider = eventSubscribe.service().provider();
                        logger.info("HTTP/JSON event subscriber can now " +
                                "receive events for system \"{}\" via " +
                                "\"{}\" provided by \"{}\" {} ...",
                            system.name(), service.name(), provider.name(), provider.socketAddress());
                    }

                    final var service = new HttpService()
                        .name("event-subscriber")
                        .basePath(basePath)
                        .accessPolicy(AccessPolicy.whitelist(eventSubscribe.service().provider().name()))
                        .encodings(JSON)

                        .post("/#topic", (request, response) -> {
                            final var topicName = request.pathParameter(0);
                            return request.bodyAs(EventIncomingDto.class)
                                .ifSuccess(event -> {
                                    try {
                                        final var topic = nameToTopic.get(topicName.toLowerCase());
                                        if (topic != null) {
                                            final var provider = event.publisher()
                                                .map(SystemDetails::toSystemDescription)
                                                .orElse(null);

                                            topic.publish(provider, event.metadata(), event.data());
                                        }
                                        else if (logger.isWarnEnabled()) {
                                            logger.warn("HTTP/JSON event " +
                                                "subscriber received " +
                                                "unexpected event " +
                                                "[topic=" + topicName + "]: {}", event);
                                        }
                                    }
                                    finally {
                                        response.status(HttpStatus.OK);
                                    }
                                })
                                .mapCatch(Throwable.class, fault -> {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("HTTP/JSON event subscriber failed to " +
                                            "handle received event [topic=" + topicName + "]", fault);
                                    }
                                    if (response.status().isEmpty()) {
                                        response.status(HttpStatus.BAD_REQUEST);
                                    }
                                    return null;
                                });
                        });

                    return system.provide(service)
                        .flatMap(ignored -> Futures.serialize(defaultSubscriptions.stream().map(this::subscribe)))
                        .mapFault(Throwable.class, fault -> new CloudException("" +
                            "HTTP/JSON event subscriber failed to setup event " +
                            "receiver for the \"" + system.name() + "\" system", fault));
                })
                .ifSuccess(ignored -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("HTTP/JSON event subscriber attached to " +
                            "system \"{}\" and registered all default event " +
                            "subscriptions", system.name());
                    }
                })
                .ifFailure(Throwable.class, fault -> {
                    if (logger.isErrorEnabled()) {
                        logger.error("HTTP/JSON event subscriber failed " +
                            "to attach to system \"" + system.name() + "\"", fault);
                    }
                });
        }

        private Future<EventSubscriptionHandle> subscribe(final EventSubscription subscription) {
            if (isDetached.get()) {
                throw new IllegalStateException("Plugin \"" +
                    HttpJsonEventSubscriberPlugin.class +
                    "\" is detached from its system \"" + system.name() +
                    "\"; cannot create subscription");
            }

            final var topicName = subscription.topic()
                .orElseThrow(() -> new IllegalArgumentException(subscription +
                    " does not specify a topic; cannot register subscription"))
                .toLowerCase();

            final var isUnsubscribed = new AtomicBoolean(false);
            final var handle = new AtomicReference<EventSubscriptionHandle>(null);
            nameToTopic.compute(topicName, (topicName0, topic) -> {
                if (topic == null) {
                    topic = new Topic(topicName0, this::unsubscribe);
                    isUnsubscribed.set(true);
                }
                handle.set(topic.register(subscription));
                logger.info("HTTP/JSON event subscriber registered {} to " +
                    "topic \"{}\" for system \"{}\"", subscription, topicName0, system.name());
                return topic;
            });

            if (!isUnsubscribed.get()) {
                return Future.success(handle.get());
            }

            final var request = subscription.toSubscriptionRequest(subscriber, basePath + '/' + topicName);
            logger.info("HTTP/JSON event subscriber is subscribing system " +
                "\"{}\" to topic \"{}\" ...", system.name(), topicName);

            return system.consume()
                .oneUsing(HttpJsonEventSubscribeService.factory())
                .flatMap(eventSubscribe -> eventSubscribe.subscribe(request)
                    .flatMapCatch(ErrorResponseException.class, fault -> {
                        final var error = fault.error();
                        if ("INVALID_PARAMETER".equals(error.type())) {
                            return system.consume()
                                .oneUsing(HttpJsonEventUnsubscribeService.factory())
                                .flatMap(eventUnsubscribe -> eventUnsubscribe
                                    .unsubscribe(topicName, system))
                                .flatMap(ignored -> eventSubscribe.subscribe(request))
                                .pass(null);
                        }
                        return Future.failure(fault);
                    }))
                .map(ignored -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("HTTP/JSON event subscriber did " +
                                "subscribe system \"{}\" to topic \"{}\"",
                            system.name(), topicName);
                    }
                    return (EventSubscriptionHandle) handle.get();
                })
                .ifFailure(Throwable.class, fault -> {
                    if (logger.isWarnEnabled()) {
                        logger.warn("HTTP/JSON event subscriber did " +
                            "fail to subscribe system \"" + system.name() +
                            "\" to topic \"" + topicName + "\"", fault);
                    }
                    nameToTopic.remove(topicName);
                });
        }

        private void unsubscribe(final String topic) {
            logger.info("HTTP/JSON event subscriber is unsubscribing " +
                "system \"{}\" from topic \"{}\" ...", system.name(), topic);

            system.consume()
                .oneUsing(HttpJsonEventUnsubscribeService.factory())
                .flatMap(eventUnsubscribe -> eventUnsubscribe.unsubscribe(topic, system))
                .ifSuccess(ignored -> logger.info("HTTP/JSON event " +
                    "subscriber unsubscribed system \"{}\" from topic " +
                    "\"{}\"", system.name(), topic))
                .onFailure(fault -> logger.warn("HTTP/JSON event " +
                    "subscriber failed to unsubscribe system \"" +
                    system.name() + "\" from topic \"" + topic +
                    "\"", fault));
        }

        @Override
        public Optional<PluginFacade> facade() {
            return Optional.of(facade);
        }

        @Override
        public void onDetach() {
            if (isDetached.getAndSet(true)) {
                return;
            }

            if (logger.isInfoEnabled()) {
                logger.info("HTTP/JSON event subscriber is detaching from " +
                    "system \"{}\" and unregistering its event " +
                    "subscriptions ...", system.name());
            }

            system.consume()
                .oneUsing(HttpJsonEventUnsubscribeService.factory())
                .ifSuccess(consumer -> {
                    for (final var topicName : nameToTopic.keySet()) {
                        unsubscribe(topicName);
                    }
                    nameToTopic.clear();
                    logger.info("HTTP/JSON event subscriber detached from " +
                        "system \"{}\"", system.name());
                })
                .onFailure(fault -> {
                    if (logger.isWarnEnabled()) {
                        logger.warn("HTTP/JSON event subscriber failed to " +
                            "unregister the event subscriptions of the \"" +
                            system.name() + "\" system", fault);
                    }
                });
        }

        private class Facade implements ArEventSubscriberPluginFacade {
            @Override
            public Future<EventSubscriptionHandle> subscribe(final EventSubscription subscription) {
                return Attached.this.subscribe(subscription);
            }
        }
    }

    private static class Topic {
        private final String name;
        private final Consumer<String> onEmpty;
        private final Set<Handle> handles = Collections.synchronizedSet(new HashSet<>());

        private Topic(final String name, final Consumer<String> onEmpty) {
            this.name = Objects.requireNonNull(name, "Expected name");
            this.onEmpty = Objects.requireNonNull(onEmpty, "Expected onEmpty");
        }

        public String name() {
            return name;
        }

        public void publish(final SystemRecord provider, final Map<String, String> metadata, final String data) {
            for (final var subscription : handles) {
                try {
                    subscription.publish(provider, metadata, data);
                }
                catch (final Throwable throwable) {
                    logger.error("HTTP/JSON event subscription threw " +
                        "unexpected exception while handling event " +
                        "[topic=" + name + "]", throwable);
                }
            }
        }

        public EventSubscriptionHandle register(final EventSubscription subscription) {
            final var handle = new Handle(subscription, this::remove);
            handles.add(handle);
            return handle;
        }

        private void remove(final Handle handle) {
            if (handles.remove(handle)) {
                if (handles.isEmpty()) {
                    onEmpty.accept(name);
                }
            }
        }
    }

    private static class Handle implements EventSubscriptionHandle {
        private final EventSubscriptionHandler handler;
        private final Map<String, String> metadata;
        private final Set<SystemRecord> providers;
        private final Consumer<Handle> onUnsubscribe;
        private final AtomicBoolean isUnsubscribed = new AtomicBoolean(false);

        private Handle(
            final EventSubscription subscription,
            final Consumer<Handle> onUnsubscribe)
        {
            Objects.requireNonNull(subscription, "Expected subscription");
            this.onUnsubscribe = Objects.requireNonNull(onUnsubscribe, "Expected onUnsubscribe");

            handler = subscription.handler()
                .orElseThrow(() -> new IllegalArgumentException(subscription +
                    " does not contain an event handler; " +
                    "cannot register subscription"));
            metadata = subscription.metadata().isEmpty() ? null : subscription.metadata();
            providers = subscription.providers().isEmpty() ? null : subscription.providers();
        }

        public void publish(final SystemRecord provider, final Map<String, String> metadata, final String data) {
            if (providers != null && !providers.contains(provider)) {
                return;
            }
            if (this.metadata != null) {
                for (final var entry : this.metadata.entrySet()) {
                    final var value = metadata.get(entry.getKey());
                    if (value == null || !value.equals(entry.getValue())) {
                        return;
                    }
                }
            }
            handler.onPublish(metadata, data);
        }

        @Override
        public void unsubscribe() {
            if (!isUnsubscribed.compareAndSet(false, true)) {
                return;
            }
            onUnsubscribe.accept(this);
        }
    }
}
