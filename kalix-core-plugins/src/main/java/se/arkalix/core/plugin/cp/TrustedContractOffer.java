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
 * A concrete offer for two parties to enter into a legally binding contract.
 * <p>
 * Each offer creates a so-called <i>negotiation {@link
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
public interface TrustedContractOffer {
    /**
     * Name of offer sender.
     * <p>
     * This is not necessarily the name of the system sending this message, but
     * of the identity that is to concretely make the offer, which may or may
     * not be the same as the name of the system sending this message.
     */
    String offerorName();

    /**
     * Name of offer receiver.
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
     * Duration until this offer expires.
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
