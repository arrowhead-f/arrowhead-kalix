package se.arkalix.core.plugin.cp;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import static se.arkalix.dto.DtoEncoding.JSON;

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
public interface TrustedSession {
    /**
     * Session identifier, uniquely identifying this session in combination
     * with the names of the two parties using it to negotiate.
     */
    long id();

    /**
     * The last {@link TrustedOffer offer} submitted to the session.
     */
    TrustedSessionOffer offer();

    /**
     * The status of the session.
     */
    TrustedSessionStatus status();
}
