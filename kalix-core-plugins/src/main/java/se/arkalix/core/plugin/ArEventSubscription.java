package se.arkalix.core.plugin.internal;

import se.arkalix.core.plugin.ArEventSubscriptionHandler;
import se.arkalix.core.plugin.dto.EventSubscriptionRequestBuilder;
import se.arkalix.core.plugin.dto.EventSubscriptionRequestDto;
import se.arkalix.core.plugin.dto.SystemDetails;
import se.arkalix.core.plugin.dto.SystemDetailsDto;
import se.arkalix.description.ProviderDescription;
import se.arkalix.util.annotation.Internal;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Internal
public class ArEventSubscription {
    private final String topic;
    private final Map<String, String> metadata;
    private final Collection<ProviderDescription> providers;
    private final ArEventSubscriptionHandler handler;

    private ArEventSubscription(final Builder builder) {
        topic = Objects.requireNonNull(builder.topic, "Expected topic");
        metadata = builder.metadata;
        providers = builder.providers;
        handler = Objects.requireNonNull(builder.handler, "Expected handler");
    }

    public String topic() {
        return topic;
    }

    public void publish(final Map<String, String> metadata, final String data) {
        handler.onPublish(metadata, data);
    }

    public EventSubscriptionRequestDto toSubscriberRequest(final SystemDetailsDto subscriber, final String uri) {
        return new EventSubscriptionRequestBuilder()
            .topic(topic)
            .subscriber(subscriber)
            .sendToUri(uri)
            .metadata(metadata)
            .publishers(providers != null
                ? providers.stream().map(SystemDetails::from).collect(Collectors.toUnmodifiableList())
                : null)
            .useMetadata(metadata != null && !metadata.isEmpty())
            .build();
    }

    @Override
    public String toString() {
        return "ArEventSubscription{" +
            "topic='" + topic + '\'' +
            ", metadata=" + metadata +
            ", providers=" + providers +
            '}';
    }

    public static class Builder {
        private String topic;
        private Map<String, String> metadata;
        private Collection<ProviderDescription> providers;
        private ArEventSubscriptionHandler handler;

        public Builder topic(final String topic) {
            this.topic = topic;
            return this;
        }

        public Builder metadata(final Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder providers(final Collection<ProviderDescription> providers) {
            this.providers = providers;
            return this;
        }

        public Builder handler(final ArEventSubscriptionHandler handler) {
            this.handler = handler;
            return this;
        }

        public ArEventSubscription build() {
            return new ArEventSubscription(this);
        }
    }
}
