package se.arkalix.core.plugin.eh;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.core.plugin._internal.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A request for some {@link #subscriber() subscriber} to receive {@link
 * EventIncoming events} matching certain {@link #topic() topic} and {@link
 * #metadata() metadata} requirements.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface EventSubscriptionRequest {
    /**
     * Identifies the general category of events being subscribed to.
     *
     * @return Event topic name.
     */
    @DtoJsonName("eventType")
    String topic();

    /**
     * Information about the subscribing system.
     *
     * @return Subscriber details.
     */
    @DtoJsonName("subscriberSystem")
    SystemDetails subscriber();

    /**
     * Metadata key/value pairs that must exist in each {@link EventOutgoing}
     * matched by this subscription.
     *
     * @return Meta data filter map.
     */
    @DtoJsonName("filterMetaData")
    Map<String, String> metadata();

    /**
     * The URI to which matching {@link EventIncoming events} will be sent as
     * an HTTP POST request.
     * <p>
     * In other words, whenever an eligible {@link EventIncoming} becomes
     * available to the Event Handler receiving this subscription, the {@link
     * #subscriber() subscriber} will be sent an HTTP POST message with this
     * URI, containing the matching {@link EventIncoming} as message body.
     *
     * @return Destination URI for matching events.
     */
    @DtoJsonName("notifyUri")
    String sendToUri();

    /**
     * {@code true} only if the {@link #metadata() metadata} map is to be used
     * when deciding what {@link EventOutgoing events} to forward to the {@link
     * #subscriber() subscriber}.
     *
     * @return {@code true} if metadata filtering is enabled.
     */
    @DtoJsonName("matchMetaData")
    boolean useMetadata();

    /**
     * The instant at which this subscription starts to be applied, unless as
     * soon as possible.
     *
     * @return Instant at which subscription starts.
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @DtoJsonName("startDate")
    Optional<String> startsAt();

    /**
     * The instant at which this subscription is terminated, if ever.
     *
     * @return Instant at which subscription stops.
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @DtoJsonName("endDate")
    Optional<String> stopsAt();

    /**
     * The systems from which events are to be received.
     * <p>
     * If no publishers are specified at all, this list will be treated as if
     * containing all possible publishers.
     *
     * @return List of systems generating the desired events.
     */
    @DtoJsonName("sources")
    List<SystemDetails> publishers();
}
