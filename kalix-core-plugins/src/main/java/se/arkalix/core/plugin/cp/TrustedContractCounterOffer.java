package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An offer countering a previously received such.
 * <p>
 * Each counter-offer updates a so-called <i>negotiation {@link
 * TrustedContractSession session}</i>, which contains all data currently
 * associated with an on-going or previously closed negotiation.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedContractCounterOffer {
    /**
     * Identifies the {@link TrustedContractSession session} containing the
     * countered offer.
     */
    long sessionId();

    /**
     * Name of offer sender.
     *
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    String offerorName();

    /**
     * Name of offer receiver.
     *
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    String receiverName();

    /**
     * Instant after which this offer becomes acceptable.
     */
    Instant validAfter();

    /**
     * Instant after which this offer can no longer be accepted or rejected.
     */
    Instant validUntil();

    /**
     * Duration until this counter-offer can no longer be accepted or rejected.
     */
    default Duration expiresIn() {
        return Duration.between(Instant.now(), validUntil());
    }

    /**
     * Offered contracts.
     */
    List<TrustedContract> contracts();

    /**
     * Time at which this offer was created.
     */
    Instant offeredAt();
}
