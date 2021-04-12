package se.arkalix.core.plugin.eh;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.core.plugin._internal.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.DtoJsonName;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * An event description, as received.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface EventIncoming {
    /**
     * Category of event.
     *
     * @return Name of event topic.
     */
    @DtoJsonName("eventType")
    String topic();

    /**
     * Information about the system publishing this event.
     *
     * @return Event publisher details.
     */
    @DtoJsonName("source")
    Optional<SystemDetails> publisher();

    /**
     * Arbitrary details about this event.
     *
     * @return Event metadata map.
     */
    @DtoJsonName("metaData")
    Map<String, String> metadata();

    /**
     * An arbitrary string payload associated with this event.
     *
     * @return Arbitrary event payload.
     */
    @DtoJsonName("payload")
    String data();

    /**
     * The time at which this event was created.
     *
     * @return Time of event creation, as AITIA timestamp.
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @DtoJsonName("timeStamp")
    String createdAt();
}
