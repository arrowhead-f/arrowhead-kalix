package se.arkalix.core.plugin.cp;

/**
 * Enumerates the different states a {@link TrustedSession} can be in.
 */
public enum TrustedSessionStatus {
    /**
     * Session is still active.
     * <p>
     * The current {@link TrustedSession#offer() session offer} can be replaced
     * or rejected by either negotiation party, as well accepted by the party
     * being the receiver of the offer.
     */
    OFFERING,

    /**
     * The session is closed and the current {@link TrustedSession#offer()
     * offer} is accepted.
     */
    ACCEPTED,

    /**
     * The session is closed and the current {@link TrustedSession#offer()
     * offer} is rejected.
     */
    REJECTED,
}
