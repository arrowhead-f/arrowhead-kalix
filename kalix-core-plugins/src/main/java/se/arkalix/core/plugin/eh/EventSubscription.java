package se.arkalix.core.plugin.eh;

import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.SystemRecord;

import java.util.*;
import java.util.stream.Collectors;

public class EventSubscription {
    private String topic;
    private Map<String, String> metadata;
    private Set<SystemRecord> providers;
    private EventSubscriptionHandler handler;

    public Optional<String> topic() {
        return Optional.ofNullable(topic);
    }

    public EventSubscription topic(final String topic) {
        this.topic = topic != null ? topic.toLowerCase() : null;
        return this;
    }

    public Map<String, String> metadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    public EventSubscription metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public EventSubscription metadata(final String key, final String value) {
        metadata().put(key, value);
        return this;
    }

    public EventSubscription provider(final SystemRecord provider) {
        providers().add(provider);
        return this;
    }

    public Set<SystemRecord> providers() {
        if (providers == null) {
            providers = new HashSet<>();
        }
        return providers;
    }

    public EventSubscription providers(final Collection<SystemRecord> providers) {
        this.providers = providers != null ? new HashSet<>(providers) : null;
        return this;
    }

    public EventSubscription providers(final SystemRecord... providers) {
        return providers(Arrays.asList(providers));
    }

    public Optional<EventSubscriptionHandler> handler() {
        return Optional.ofNullable(handler);
    }

    public EventSubscription handler(final EventSubscriptionHandler handler) {
        this.handler = handler;
        return this;
    }

    public EventSubscriptionRequestDto toSubscriptionRequest(final SystemDetailsDto subscriber, final String uri) {
        return new EventSubscriptionRequestDto.Builder()
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
        return "EventSubscription{" +
            "topic='" + topic + '\'' +
            ", metadata=" + metadata +
            ", providers=" + providers +
            '}';
    }
}
