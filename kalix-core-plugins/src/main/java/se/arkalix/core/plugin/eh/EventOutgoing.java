package se.arkalix.core.plugin.eh;

import se.arkalix.internal.core.plugin.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An event description, as sent.
 */
@DtoWritableAs(JSON)
@DtoToString
public interface EventOutgoing {
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
    Optional<String> createdAt();
}
