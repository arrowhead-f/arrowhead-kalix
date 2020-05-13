package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

import java.time.Instant;

/**
 * A service useful for making contract offers, as well as accepting or
 * rejecting received contract offers.
 * <p>
 * The services that may be consumed via this interface must be trusted not to
 * alter the messages sent to them.
 */
@SuppressWarnings("unused")
public interface ArContractNegotiationTrusted {
    /**
     * Accepts {@link TrustedSession#candidate() session offer} identified by
     * given {@code acceptance}.
     *
     * @param acceptance Identifies accepted session offer.
     * @return Future completed successfully only if acceptance succeeds.
     */
    Future<?> accept(TrustedAcceptanceDto acceptance);

    /**
     * Accepts {@link TrustedSession#candidate() session offer} identified by
     * given session and candidate identifiers.
     *
     * @param sessionId    Identifies negotiation session the accepted offer is
     *                     part of.
     * @param candidateSeq Identifies the state of the session when the
     *                     accepted offer was its candidate. If the session
     *                     would change before this message is received, this
     *                     call fails.
     * @return Future completed successfully only if acceptance succeeds.
     */
    default Future<?> accept(final long sessionId, final long candidateSeq) {
        return accept(new TrustedAcceptanceBuilder()
            .sessionId(sessionId)
            .candidateSeq(candidateSeq)
            .acceptedAt(Instant.now())
            .build());
    }

    /**
     * Makes a new contract {@link TrustedOffer offer}.
     * <p>
     * In particular, this call creates new negotiation session, or replaces
     * the {@link TrustedSession#candidate() candidate} in an existing session.
     *
     * @param offer Offer details.
     * @return Future completed successfully only if the offer could be made.
     */
    Future<?> offer(TrustedOfferDto offer);

    /**
     * Rejects {@link TrustedSession#candidate() session offer} identified by
     * given {@code rejection}.
     *
     * @param rejection Identifies rejected session offer.
     * @return Future completed successfully only if rejection succeeds.
     */
    Future<?> reject(TrustedRejectionDto rejection);

    /**
     * Rejects {@link TrustedSession#candidate() session offer} identified by
     * given session identifier.
     * <p>
     * In contrast to {@link #reject(long, long)}, this call succeeds even if
     * the current session candidate is replaced before the message it sends
     * arrives.
     *
     * @param sessionId Identifies negotiation session the rejected offer is
     *                  part of.
     * @return Future completed successfully only if rejection succeeds.
     */
    default Future<?> reject(final long sessionId) {
        return reject(new TrustedRejectionBuilder()
            .sessionId(sessionId)
            .rejectedAt(Instant.now())
            .build());
    }

    /**
     * Rejects {@link TrustedSession#candidate() session offer} identified by
     * given session and candidate identifiers.
     *
     * @param sessionId    Identifies negotiation session the rejected offer is
     *                     part of.
     * @param candidateSeq Identifies the state of the session when the rejected
     *                     offer was its candidate. If the session would change
     *                     before this message is received, this call fails.
     * @return Future completed successfully only if rejection succeeds.
     */
    default Future<?> reject(final long sessionId, final long candidateSeq) {
        return reject(new TrustedRejectionBuilder()
            .sessionId(sessionId)
            .candidateSeq(candidateSeq)
            .rejectedAt(Instant.now())
            .build());
    }
}
