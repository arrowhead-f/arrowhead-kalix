package se.arkalix.core.plugin.cp;

/**
 * Enumerates the different states a contract negotiation session can be in.
 */
public enum ContractNegotiationStatus {
    /**
     * Session is still active.
     * <p>
     * The current {@link TrustedContractNegotiation#offer() session offer} can be replaced
     * or rejected by either negotiation party, as well accepted by the party
     * being the receiver of the offer.
     */
    OFFERING,

    /**
     * The session is closed and the current {@link TrustedContractNegotiation#offer()
     * offer} is accepted.
     */
    ACCEPTED,

    /**
     * The session is closed and the current {@link TrustedContractNegotiation#offer()
     * offer} is rejected.
     */
    REJECTED,

    /**
     * The session is closed and the current {@link TrustedContractNegotiation#offer()
     * offer} can no longer be replaced, rejected or accepted due to having
     * {@link TrustedContractOffer#validUntil() expired}.
     */
    EXPIRED,
}
