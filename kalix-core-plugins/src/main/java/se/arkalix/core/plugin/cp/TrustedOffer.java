package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A concrete offer for two parties to enter into a legally binding contract.
 * <p>
 * Each offer creates or updates a so-called <i>negotiation {@link
 * TrustedSession session}</i>, which contains all data currently associated
 * with an on-going or previously closed negotiation.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedOffer {
    /**
     * Identifies the negotiation {@link TrustedSession session} associated
     * with this offer. Each such session is uniquely identified by the names
     * of the negotiating parties and this number, which should be selected
     * randomly.
     */
    long sessionId();

    /**
     * A number incremented each time a negotiation session is updated without
     * being accepted or rejected.
     * <p>
     * If this offer is meant to create a new session, it should be zero. If it
     * is expected to update an existing session by replacing its {@link
     * TrustedSession#offer() offer}, it must be equal to the previous {@link
     * TrustedSessionOffer#offerSeq() offer sequence number} plus one.
     */
    long offerSeq();

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
     * Offered contracts.
     */
    List<TrustedContract> contracts();

    /**
     * Time at which this offer was created.
     */
    Instant offeredAt();
}
