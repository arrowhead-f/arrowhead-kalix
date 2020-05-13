package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Signals the desire to end a contract negotiation.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedRejection {
    /**
     * Identifies the {@link TrustedSession session} containing the rejected
     * offer.
     */
    long sessionId();

    /**
     * Identifies the {@link TrustedSession#offer() session offer} being
     * rejected.
     * <p>
     * If a candidate sequence number is specified and the rejected session
     * changes before this message arrives, then the number will no longer
     * match the session offer sequence number and the rejection fails.
     */
    Optional<Long> offerSeq();

    /**
     * The instant at which the offer was rejected.
     */
    Instant rejectedAt();
}
