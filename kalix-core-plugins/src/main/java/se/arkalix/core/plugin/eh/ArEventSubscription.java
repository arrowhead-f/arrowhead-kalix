package se.arkalix.core.plugin.eh;

import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.description.ProviderDescription;

import java.util.*;
import java.util.stream.Collectors;

public class ArEventSubscription {
    private String topic;
    private Map<String, String> metadata;
    private Set<ProviderDescription> providers;
    private ArEventSubscriptionHandler handler;

    public Optional<String> topic() {
        return Optional.ofNullable(topic);
    }

    public ArEventSubscription topic(final String topic) {
        this.topic = topic != null ? topic.toLowerCase() : null;
        return this;
    }

    public Map<String, String> metadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    public ArEventSubscription metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public ArEventSubscription metadata(final String key, final String value) {
        metadata().put(key, value);
        return this;
    }

    public ArEventSubscription provider(final ProviderDescription provider) {
        providers().add(provider);
        return this;
    }

    public Set<ProviderDescription> providers() {
        if (providers == null) {
            providers = new HashSet<>();
        }
        return providers;
    }

    public ArEventSubscription providers(final Collection<ProviderDescription> providers) {
        this.providers = providers != null ? new HashSet<>(providers) : null;
        return this;
    }

    public ArEventSubscription providers(final ProviderDescription... providers) {
        return providers(Arrays.asList(providers));
    }

    public Optional<ArEventSubscriptionHandler> handler() {
        return Optional.ofNullable(handler);
    }

    public ArEventSubscription handler(final ArEventSubscriptionHandler handler) {
        this.handler = handler;
        return this;
    }

    public EventSubscriptionRequestDto toSubscriptionRequest(final SystemDetailsDto subscriber, final String uri) {
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
}
