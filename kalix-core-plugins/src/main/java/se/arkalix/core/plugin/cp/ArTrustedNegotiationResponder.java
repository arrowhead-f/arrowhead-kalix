package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

/**
 * Some object useful for responding to a {@link TrustedContractOffer contract offer}.
 */
@SuppressWarnings("unused")
public interface ArTrustedNegotiationResponder {
    /**
     * Accept {@link TrustedContractOffer contract offer}.
     *
     * @return Future completing successfully only if an acceptance can be
     * successfully submitted.
     */
    Future<?> accept();

    /**
     * Send {@link TrustedContractOffer contract counter-offer}.
     *
     * @param offer New offer to send to original offeror.
     * @return Future completing successfully only if the offer can be
     * successfully submitted.
     */
    Future<?> offer(SimplifiedContractCounterOffer offer);

    /**
     * Reject {@link TrustedContractOffer contract offer}.
     *
     * @return Future completing successfully only if a rejection can be
     * successfully submitted.
     */
    Future<?> reject();
}
