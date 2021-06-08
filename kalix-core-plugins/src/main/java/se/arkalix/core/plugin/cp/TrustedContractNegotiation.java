package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * The state associated with an on-going or previously closed negotiation
 * session.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedContractNegotiation {
    /**
     * Negotiation identifier, uniquely identifying this negotiation session in
     * combination with the names of the two parties using it to negotiate.
     *
     * @return Negotiation identifier.
     */
    long id();

    /**
     * The last offer to be submitted to this session.
     *
     * @return Last session offer.
     */
    TrustedContractOffer offer();

    /**
     * The current state of this negotiation session.
     *
     * @return Session status.
     */
    ContractNegotiationStatus status();
}
