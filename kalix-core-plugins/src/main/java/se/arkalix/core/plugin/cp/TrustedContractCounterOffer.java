package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An {@link TrustedContractOffer offer} countering a previously received such.
 * <p>
 * Each counter-offer updates a so-called {@link TrustedContractNegotiation
 * negotiation session}, which contains all data currently associated with an
 * on-going or previously closed negotiation.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedContractCounterOffer extends TrustedContractOffer {
    /**
     * Identifies the {@link TrustedContractNegotiation negotiation session}
     * containing the countered offer.
     */
    long negotiationId();

    /**
     * Name of offer sender.
     *
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    @Override
    String offerorName();

    /**
     * Name of offer receiver.
     *
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    @Override
    String receiverName();

    /**
     * Instant after which this offer becomes acceptable.
     */
    @Override
    Instant validAfter();

    /**
     * Instant after which this offer can no longer be accepted or rejected.
     */
    @Override
    Instant validUntil();

    /**
     * Offered contracts.
     */
    @Override
    List<TrustedContract> contracts();

    /**
     * Time at which this offer was created.
     */
    @Override
    Instant offeredAt();
}
