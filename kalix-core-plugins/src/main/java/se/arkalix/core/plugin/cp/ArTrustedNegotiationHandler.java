package se.arkalix.core.plugin.cp;

import org.slf4j.LoggerFactory;

public interface ArTrustedNegotiationHandler {
    void onAccept(TrustedSessionCandidate candidate);

    void onOffer(TrustedSessionCandidate candidate, ArTrustedNegotiationResponder responder);

    void onReject(TrustedSessionCandidate candidate);

    default void onFault(final Throwable throwable) {
        final var logger = LoggerFactory.getLogger(ArTrustedNegotiationHandler.class);
        logger.error("Contract negotiation failed due to an unexpected " +
            "exception being thrown", throwable);
    }
}
