package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

/**
 * Some object useful for responding to a {@link TrustedOffer contract offer}.
 */
@SuppressWarnings("unused")
public interface ArTrustedNegotiationResponder {
    /**
     * Accept {@link TrustedOffer contract offer}.
     *
     * @return Future completing successfully only if an acceptance can be
     * successfully submitted.
     */
    Future<?> accept();

    /**
     * Send {@link TrustedOffer contract counter-offer}.
     *
     * @param offer New offer to send to original offeror.
     * @return Future completing successfully only if the offer can be
     * successfully submitted.
     */
    Future<?> offer(TrustedCounterOffer offer);

    /**
     * Reject {@link TrustedOffer contract offer}.
     *
     * @return Future completing successfully only if a rejection can be
     * successfully submitted.
     */
    Future<?> reject();
}
