package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.dto.ErrorException;
import se.arkalix.core.plugin.dto.EventIncomingDto;
import se.arkalix.core.plugin.dto.SystemDetails;
import se.arkalix.core.plugin.dto.SystemDetailsDto;
import se.arkalix.description.ProviderDescription;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static final int STATE_UNATTACHED = 0;
    private static final int STATE_ATTACHED = 1;
    private static final int STATE_DETACHED = 2;

    private final ConcurrentHashMap<String, SubscriptionHandle> topicToSubscription = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<FutureSubscription> pendingSubscriptions = new ConcurrentLinkedQueue<>();
    private final AtomicInteger state = new AtomicInteger(STATE_UNATTACHED);

    private String basePath = null;
    private ArSystem system = null;
    private SystemDetailsDto subscriber = null;

    /**
     * Registers new event subscription.
     *
     * @param topic   Topic, or "eventType", that must be matched by
     *                received events. Topics are case insensitive and can
     *                only be subscribed to once.
     * @param handler Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    public Future<ArEventSubscriptionHandle> subscribe(final String topic, final ArEventSubscriptionHandler handler) {
        return subscribe(topic, null, null, handler);
    }

    /**
     * Registers new event subscription.
     *
     * @param topic    Topic, or "eventType", that must be matched by
     *                 received events. Topics are case insensitive and can
     *                 only be subscribed to once.
     * @param metadata Metadata pairs that must be matched by received
     *                 events.
     * @param handler  Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    public Future<ArEventSubscriptionHandle> subscribe(
        final String topic,
        final Map<String, String> metadata,
        final ArEventSubscriptionHandler handler)
    {
        return subscribe(topic, metadata, null, handler);
    }

    /**
     * Registers new event subscription.
     *
     * @param topic     Topic, or "eventType", that must be matched by
     *                  received events. Topics are case insensitive and
     *                  can only be subscribed to once.
     * @param providers Event providers to receive events from.
     * @param handler   Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    public Future<ArEventSubscriptionHandle> subscribe(
        final String topic,
        final Collection<ProviderDescription> providers,
        final ArEventSubscriptionHandler handler)
    {
        return subscribe(topic, null, providers, handler);
    }

    /**
     * Registers new event subscription.
     *
     * @param topic     Topic, or "eventType", that must be matched by
     *                  received events. Topics are case insensitive and
     *                  can only be subscribed to once.
     * @param metadata  Metadata pairs that must be matched by received
     *                  events.
     * @param providers Event providers to receive events from.
     * @param handler   Handler to receive matching events.
     * @return Future completed when subscription is registered.
     */
    public Future<ArEventSubscriptionHandle> subscribe(
        final String topic,
        final Map<String, String> metadata,
        final Collection<ProviderDescription> providers,
        final ArEventSubscriptionHandler handler)
    {
        return subscribe(new ArEventSubscription()
            .topic(topic)
            .metadata(metadata)
            .providers(providers)
            .handler(handler));
    }

    /**
     * Registers new event subscription.
     *
     * @param subscription Subscription to register.
     * @return This builder.
     */
    public Future<ArEventSubscriptionHandle> subscribe(final ArEventSubscription subscription) {
        final var futureSubscription = new FutureSubscription(subscription);
        switch (state.get()) {
        case STATE_UNATTACHED:
            pendingSubscriptions.add(futureSubscription);
            break;

        case STATE_ATTACHED:
            futureSubscription.subscribe();
            break;

        case STATE_DETACHED:
            throw new IllegalStateException("This plugin \"" + getClass() +
                "\" is not attached to any system; cannot create subscription");

        default:
            throw new IllegalStateException("Illegal plugin state; cannot " +
                "create subscription");
        }
        return futureSubscription;
    }

    @Override
    public void onAttach(final Plug plug, final Set<Plugin> plugins) {
        logger.info("Attaching to system \"{}\" ...", plug.system().name());

        this.system = plug.system();
        this.subscriber = SystemDetails.from(this.system);
        this.basePath = "/events/" + this.system.name();

        final var system = plug.system();
        system.consume()
            .using(HttpJsonEventSubscribe.factory())
            .flatMap(eventSubscribe -> {
                if (logger.isInfoEnabled()) {
                    final var service = eventSubscribe.service();
                    final var provider = eventSubscribe.service().provider();
                    logger.info("Registering system \"{}\" event subscriptions via \"{}\" provided by \"{}\" {} ...",
                        plug.system().name(), service.name(), provider.name(), provider.socketAddress());
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
                    .ifSuccess(serviceHandle -> {
                        this.system = system;
                        state.set(STATE_ATTACHED);
                        for (final var subscription : pendingSubscriptions) {
                            subscription.subscribe();
                        }
                        pendingSubscriptions.clear();
                    })
                    .mapFault(Throwable.class, fault -> new Exception("" +
                        "Failed to setup event receiver for the \"" +
                        plug.system().name() + "\" system", fault));
            })
            .ifSuccess(ignored -> {
                if (logger.isInfoEnabled()) {
                    logger.info("Attached to system \"{}\" and registered " +
                        "all pending event subscriptions", plug.system().name());
                }
            })
            .onFailure(fault -> {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to attach to system \"" +
                        plug.system().name() + "\"", fault);
                }
            });
    }

    @Override
    public void onDetach(final Plug plug) {
        if (logger.isInfoEnabled()) {
            logger.info("Detaching from system \"{}\" and unregistering " +
                "event subscriptions ...", plug.system().name());
        }
        state.set(STATE_DETACHED);

        final var system = plug.system();
        system.consume()
            .using(HttpJsonEventUnsubscribe.factory())
            .ifSuccess(consumer -> {
                for (final var subscription : topicToSubscription.values()) {
                    subscription.unsubscribe();
                }
                logger.info("Detached from system \"{}\"", plug.system().name());
            })
            .onFailure(fault -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to unregister the event subscriptions " +
                        "of the \"" + plug.system().name() + "\" system", fault);
                }
            });
    }

    private class FutureSubscription implements Future<ArEventSubscriptionHandle> {
        private final ArEventSubscription subscription;
        private final SubscriptionHandle subscriptionHandle;

        private Consumer<Result<ArEventSubscriptionHandle>> consumer = null;

        public FutureSubscription(final ArEventSubscription subscription) {
            this.subscription = Objects.requireNonNull(subscription);

            final var topic = subscription.topic()
                .orElseThrow(() -> new IllegalArgumentException("Expected subscription topic to be set"));

            final var handler = subscription.handler()
                .orElseThrow(() -> new IllegalArgumentException("Expected subscription handler to be set"));

            this.subscriptionHandle = new SubscriptionHandle(topic, handler);
            final var existingSubscription = topicToSubscription.putIfAbsent(topic, this.subscriptionHandle);
            if (existingSubscription != null) {
                throw new IllegalStateException("A subscription already exists " +
                    "for the topic \"" + topic + "\" " + existingSubscription +
                    "; cannot register " + subscription);
            }
        }

        public void subscribe() {
            final var topic = subscriptionHandle.topic;
            final var request = subscription.toSubscriptionRequest(subscriber, basePath + '/' + topic);
            logger.info("Subscribing system \"{}\" to topic \"{}\" ...", system.name(), topic);
            system.consume()
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
                    }))
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
            pendingSubscriptions.remove(this);
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
