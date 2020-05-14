package se.arkalix.core.plugin.cp;

/**
 * Enumerates the different states a contract negotiation session can be in.
 */
public enum ContractSessionStatus {
    /**
     * Session is still active.
     * <p>
     * The current {@link TrustedContractSession#offer() session offer} can be replaced
     * or rejected by either negotiation party, as well accepted by the party
     * being the receiver of the offer.
     */
    OFFERING,

    /**
     * The session is closed and the current {@link TrustedContractSession#offer()
     * offer} is accepted.
     */
    ACCEPTED,

    /**
     * The session is closed and the current {@link TrustedContractSession#offer()
     * offer} is rejected.
     */
    REJECTED,

    /**
     * The session is closed and the current {@link TrustedContractSession#offer()
     * offer} can no longer be replaced, rejected or accepted due to having
     * {@link TrustedContractOffer#validUntil() expired}.
     */
    EXPIRED,
}
