package se.arkalix.core.plugin.cp;

import org.slf4j.LoggerFactory;

/**
 * Handler used to receive and react to different kinds of contract negotiation
 * events.
 */
public interface ArTrustedNegotiationHandler {
    /**
     * Called to indicate that a previously made contract offer was accepted by
     * its receiver.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param candidate Accepted offer.
     */
    void onAccept(TrustedSessionOffer candidate);

    /**
     * Called to notify about a counter-offer being received by a party that
     * previously received a contract offer.
     *
     * @param candidate Counter-offer.
     * @param responder Object useful for reacting to the counter-offer.
     */
    void onOffer(TrustedSessionOffer candidate, ArTrustedNegotiationResponder responder);

    /**
     * Called to indicate that a previously made contract offer was rejected by
     * its receiver.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param candidate Rejected offer.
     */
    void onReject(TrustedSessionOffer candidate);

    /**
     * Called to indicate that a previously made or received contract offer
     * expired before being responded to.
     * <p>
     * The negotiation session in question must be considered permanently
     * closed when this method is invoked.
     *
     * @param candidate Expired offer.
     */
    default void onExpiry(final TrustedSessionOffer candidate) {
        final var logger = LoggerFactory.getLogger(getClass());
        logger.warn("Contract negotiation session expired: {}", candidate);
    }

    /**
     * Called to indicate that some submitted offer could not be sent due to
     * an unexpected exception.
     * <p>
     * This method being invoked does not necessarily imply that the
     * negotiation session in question is closed.
     *
     * @param throwable Exception preventing offer submission.
     */
    default void onFault(final Throwable throwable) {
        final var logger = LoggerFactory.getLogger(getClass());
        logger.error("Contract negotiation failed due to an unexpected " +
            "exception being thrown", throwable);
    }
}
