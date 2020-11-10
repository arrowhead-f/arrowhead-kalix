package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * An acceptance of an offer to enter into one or more legally binding contacts.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
@SuppressWarnings("unused")
public interface TrustedContractAcceptance {
    /**
     * Identifies the {@link TrustedContractNegotiation negotiation session}
     * containing the accepted offer.
     */
    long negotiationId();

    /**
     * Name of party accepting identified offer.
     */
    String acceptorName();

    /**
     * Name of party originally making the accepted offer.
     */
    String offerorName();

    /**
     * The instant at which the offer was accepted.
     */
    Instant acceptedAt();
}
