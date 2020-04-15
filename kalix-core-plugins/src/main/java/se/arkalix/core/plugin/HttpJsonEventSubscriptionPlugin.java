package se.arkalix.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.core.plugin.dto.Event;
import se.arkalix.core.plugin.dto.EventDto;
import se.arkalix.core.plugin.dto.EventSubscriptionBuilder;
import se.arkalix.core.plugin.dto.SystemDetails;
import se.arkalix.description.ProviderDescription;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.concurrent.Schedulers;
import se.arkalix.util.function.ThrowingConsumer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static se.arkalix.descriptor.EncodingDescriptor.JSON;

public class HttpJsonEventSubscriptionPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonEventSubscriptionPlugin.class);

    private final List<Subscription> subscriptions;

    private HttpJsonEventSubscriptionPlugin(final Builder builder) {
        subscriptions = Objects.requireNonNull(builder.subscriptions, "Expected subscriptions");
    }

    @Override
    public void onAttach(final Plug plug) {
        Schedulers.fixed().execute(() -> {
            try {
                final String basePath;
                {
                    final var nounce = new byte[5];
                    SecureRandom.getInstanceStrong().nextBytes(nounce);
                    basePath = "/" + Base64.getUrlEncoder().encodeToString(nounce);
                }

                final var system = plug.system();

                final var eventReceiver = new HttpService()
                    .name("event-subscriber")
                    .basePath(basePath)
                    // TODO: It would be better if only the event handler was allowed access. Whitelist "event_handler"?
                    .accessPolicy(AccessPolicy.cloud())
                    .encodings(JSON);

                final var subscriber = SystemDetails.from(system);
                system.consume()
                    .using(HttpJsonEventSubscribe.factory())
                    .ifSuccess(consumer -> {
                        for (final var subscription : subscriptions) {
                            consumer.subscribe(new EventSubscriptionBuilder()
                                .topic(subscription.topic)
                                .subscriber(subscriber)
                                .sendToUri(basePath + subscription.uri)
                                .metadata(subscription.metadata)
                                .publishers(subscription.providers.stream()
                                    .map(SystemDetails::from)
                                    .collect(Collectors.toUnmodifiableList()))
                                .useMetadata(subscription.metadata != null && !subscription.metadata.isEmpty())
                                .build())
                                .ifSuccess(ignored -> eventReceiver.post(subscription.uri, (request, response) ->
                                    request.bodyAs(EventDto.class)
                                        .ifSuccess(event -> {
                                            subscription.handler.accept(event);
                                            response.status(HttpStatus.OK);
                                        })
                                        .ifFailure(Throwable.class, fault -> {
                                            if (logger.isWarnEnabled()) {
                                                logger.warn("Failed to " +
                                                    "receive published event " +
                                                    subscription, fault);
                                            }
                                            response.status(HttpStatus.BAD_REQUEST);
                                        })))
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
            catch (final Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to subscribe to events", throwable);
                }
            }
        });
    }

    @Override
    public void onDetach(final Plug plug) {
        final var system = plug.system();
        final var subscriber = SystemDetails.from(system);
        system.consume()
            .using(HttpJsonEventUnsubscribe.factory())
            .ifSuccess(consumer -> {
                for (final var subscription : subscriptions) {
                    consumer.unsubscribe(
                        subscription.topic,
                        subscriber.name(),
                        subscriber.hostname(),
                        subscriber.port())
                        .onFailure(fault -> {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Failed to unregister " + subscription, fault);
                            }
                        });;
                }
            });
    }

    private static class Subscription {
        private final String topic;
        private final Map<String, String> metadata;
        private final Collection<ProviderDescription> providers;
        private final ThrowingConsumer<Event> handler;
        private final String uri;

        private Subscription(
            final String topic,
            final Map<String, String> metadata,
            final Collection<ProviderDescription> providers,
            final ThrowingConsumer<Event> handler)
        {
            this.topic = Objects.requireNonNull(topic, "Expected topic");
            this.metadata = metadata;
            this.providers = providers;
            this.handler = Objects.requireNonNull(handler, "Expected handler");

            try {
                final var nounce = new byte[20];
                SecureRandom.getInstanceStrong().nextBytes(nounce);
                this.uri = "/" + Base64.getUrlEncoder().encodeToString(nounce);
            }
            catch (final NoSuchAlgorithmException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public String toString() {
            return "Subscription{" +
                "topic='" + topic + '\'' +
                ", metadata=" + metadata +
                ", providers=" + providers +
                ", uri='" + uri + '\'' +
                '}';
        }
    }

    public static class Builder {
        private final List<Subscription> subscriptions = new ArrayList<>();

        public Builder subscribe(final String topic, final ThrowingConsumer<Event> handler) {
            return subscribe(topic, null, null, handler);
        }

        public Builder subscribe(
            final String topic,
            final Map<String, String> metadata,
            final ThrowingConsumer<Event> handler)
        {
            return subscribe(topic, metadata, null, handler);
        }

        public Builder subscribe(
            final String topic,
            final Collection<ProviderDescription> providers,
            final ThrowingConsumer<Event> handler)
        {
            return subscribe(topic, null, providers, handler);
        }

        public Builder subscribe(
            final String topic,
            final Map<String, String> metadata,
            final Collection<ProviderDescription> providers,
            final ThrowingConsumer<Event> handler)
        {
            subscriptions.add(new Subscription(topic, metadata, providers, handler));
            return this;
        }

        public HttpJsonEventSubscriptionPlugin build() {
            return new HttpJsonEventSubscriptionPlugin(this);
        }
    }
}
