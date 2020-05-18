package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;

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
@SuppressWarnings("unused")
public interface TrustedContractRejection {
    /**
     * Identifies the {@link TrustedContractNegotiation negotiation session}
     * containing the rejected offer.
     */
    long negotiationId();

    /**
     * The instant at which the offer was rejected.
     */
    Instant rejectedAt();
}
