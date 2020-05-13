package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A {@link TrustedOffer}, as represented when part of a {@link TrustedSession},
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedSessionOffer {
    /**
     * Offer sequence number.
     * <p>
     * This number is incremented every time the offer of a {@link
     * TrustedSession} is updated.
     */
    long offerSeq();

    /**
     * Name of party that made this offer.
     */
    String offerorName();

    /**
     * Name of party that received this offer.
     */
    String receiverName();

    /**
     * Instant after which this candidate can be accepted, unless already
     * rejected.
     */
    Instant validAfter();

    /**
     * Instant after which this candidate can no longer be accepted or
     * rejected, unless it is already accepted or rejected.
     */
    Instant validUntil();

    /**
     * Contracts offered by {@link #offerorName() offereror} to {@link
     * #receiverName() receiver}.
     */
    List<TrustedContract> contracts();

    /**
     * Instant when this offer was created.
     */
    Instant createdAt();
}
