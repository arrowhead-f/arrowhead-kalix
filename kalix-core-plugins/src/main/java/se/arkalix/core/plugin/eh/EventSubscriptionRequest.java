package se.arkalix.core.plugin.eh;

import se.arkalix.internal.core.plugin.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A request for some {@link #subscriber() subscriber} to receive {@link
 * EventIncoming events} matching certain {@link #topic() topic} and {@link
 * #metadata() metadata} requirements.
 */
@DtoWritableAs(JSON)
@DtoToString
public interface EventSubscriptionRequest {
    /**
     * Identifies the general category of events being subscribed to.
     */
    @JsonName("eventType")
    String topic();

    /**
     * Information about the subscribing system.
     */
    @JsonName("subscriberSystem")
    SystemDetails subscriber();

    /**
     * Metadata key/value pairs that must exist in each {@link EventOutgoing} matched
     * by this subscription.
     */
    @JsonName("filterMetaData")
    Map<String, String> metadata();

    /**
     * The URI to which matching {@link EventIncoming events} will be sent as
     * an HTTP POST request.
     * <p>
     * In other words, whenever an eligible {@link EventIncoming} becomes
     * available to the Event Handler receiving this subscription, the {@link
     * #subscriber() subscriber} will be sent an HTTP POST message with this
     * URI, containing the matching {@link EventIncoming} as message body.
     */
    @JsonName("notifyUri")
    String sendToUri();

    /**
     * {@code true} only if the {@link #metadata() metadata} map is to be used
     * when deciding what {@link EventOutgoing events} to forward to the {@link
     * #subscriber() subscriber}.
     */
    @JsonName("matchMetaData")
    boolean useMetadata();

    /**
     * The instant at which this subscription starts to be applied, unless as
     * soon as possible.
     *
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @JsonName("startDate")
    Optional<String> startsAt();

    /**
     * The instant at which this subscription is terminated, if ever.
     *
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @JsonName("endDate")
    Optional<String> stopsAt();

    /**
     * The systems from which events are to be received.
     * <p>
     * If no publishers are specified at all, this list will be treated as if
     * containing all possible publishers.
     */
    @JsonName("sources")
    List<SystemDetails> publishers();
}
