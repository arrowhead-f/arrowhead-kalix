package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.Map;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An event description.
 */
@DtoWritableAs(JSON)
@DtoReadableAs(JSON)
public interface Event {
    /**
     * Category of event.
     */
    @JsonName("eventType")
    String topic();

    /**
     * Information about the system publishing this event.
     */
    @JsonName("source")
    SystemDetails publisher();

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
