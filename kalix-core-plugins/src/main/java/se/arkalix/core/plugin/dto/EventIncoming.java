package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An event description, as received.
 */
@DtoReadableAs(JSON)
@DtoToString
public interface EventIncoming {
    /**
     * Category of event.
     */
    @JsonName("eventType")
    String topic();

    /**
     * Information about the system publishing this event.
     */
    @JsonName("source")
    Optional<SystemDetails> publisher();

    /**
     * Arbitrary details about this event.
     */
    @JsonName("metaData")
    Map<String, String> metadata();

    /**
     * An arbitrary string payload associated with this event.
     */
    @JsonName("payload")
    String data();

    /**
     * The time at which this event was created.
     *
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @JsonName("timeStamp")
    String createdAt();
}
