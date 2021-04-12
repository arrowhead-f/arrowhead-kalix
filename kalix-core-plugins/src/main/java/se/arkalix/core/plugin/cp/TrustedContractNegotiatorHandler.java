package se.arkalix.core.plugin.cp;

import org.slf4j.LoggerFactory;

/**
 * Handler used to receive and react to different kinds of contract negotiation
 * events.
 */
public interface TrustedContractNegotiatorHandler {
    /**
     * Called to indicate that a previously made contract offer was accepted by
     * its receiver.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param negotiation Session containing accepted offer.
     */
    void onAccept(TrustedContractNegotiationDto negotiation);

    /**
     * Called to notify about a new offer or counter-offer being received.
     *
     * @param negotiation Session containing the received offer or
     *                    counter-offer.
     * @param responder   Object useful for responding to the received offer.
     */
    void onOffer(TrustedContractNegotiationDto negotiation, TrustedContractNegotiatorResponder responder);

    /**
     * Called to indicate that a previously made contract offer was rejected by
     * its receiver.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param negotiation Session containing rejected offer.
     */
    void onReject(TrustedContractNegotiationDto negotiation);

    /**
     * Called to indicate that a previously made or received contract offer
     * expired before being responded to.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param negotiationId Identifier of expires negotiation session.
     */
    default void onExpiry(final long negotiationId) {
        final var logger = LoggerFactory.getLogger(getClass());
        logger.warn("Contract negotiation #{} expired: {}", negotiationId, this);
    }

    /**
     * Called to indicate that some submitted offer could not be sent due to
     * an unexpected exception.
     * <p>
     * This method being invoked does not necessarily imply that the
     * negotiation session in question is closed.
     *
     * @param negotiationId Identifier of affected negotiation session.
     * @param throwable     Exception preventing offer submission.
     */
    default void onFault(final long negotiationId, final Throwable throwable) {
        final var logger = LoggerFactory.getLogger(getClass());
        logger.error("Contract negotiation #" + negotiationId + " failed " +
            "due to an unexpected exception being thrown", throwable);
    }
}
