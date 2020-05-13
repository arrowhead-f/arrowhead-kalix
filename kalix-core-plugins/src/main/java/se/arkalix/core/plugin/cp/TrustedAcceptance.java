package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An acceptance of an offer to enter into one or more legally binding
 * contacts.
 * <p>
 * Instances of this type are trusted in the sense that they either (1) come
 * from trusted sources or (2) will be sent to systems that trust their senders.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface TrustedAcceptance {
    /**
     * Identifies the {@link TrustedSession session} containing the accepted
     * candidate offer.
     */
    long sessionId();

    /**
     * Identifies the {@link TrustedSession#candidate() session candidate}
     * being accepted.
     * <p>
     * If the accepted session would change before this message arrives, then
     * this identifier will no longer match the session candidate identifier
     * and this acceptance fails.
     */
    long candidateSeq();

    /**
     * The instant at which the offer was accepted.
     */
    Instant acceptedAt();
}
