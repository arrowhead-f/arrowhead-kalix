package se.arkalix.core.plugin.eh;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.core.plugin._internal.Instants;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An event description, as sent.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface EventOutgoing {
    /**
     * Category of event.
     */
    @DtoJsonName("eventType")
    String topic();

    /**
     * Information about the system publishing this event.
     */
    @DtoJsonName("source")
    SystemDetails publisher();

    /**
     * Arbitrary details about this event.
     */
    @DtoJsonName("metaData")
    Map<String, String> metadata();

    /**
     * An arbitrary string payload associated with this event.
     */
    @DtoJsonName("payload")
    String data();

    /**
     * The time at which this event was created.
     *
     * @see Instants#fromAitiaDateTimeString(String)
     */
    @DtoJsonName("timeStamp")
    Optional<String> createdAt();
}
