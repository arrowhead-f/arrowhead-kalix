package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * {@link PluginFacade Facade} allowing for contract negotiations to be carried
 * out with state management support.
 */
@SuppressWarnings("unused")
public interface ArContractNegotiationTrustedPluginFacade extends PluginFacade {
    /**
     * Starts new contract negotiation by making given {@code offer} and then
     * uses provided negotiation {@code handler} to handle any response from
     * the party receiving the offer.
     *
     * @param offer   Contract offer to make.
     * @param handler Handler used to track and react to any acceptance,
     *                rejection or counter-offers made by the party receiving
     *                the offer.
     */
    void offer(TrustedContractOfferDto offer, ArTrustedNegotiationHandler handler);

    /**
     * Starts new contract negotiation by making the described offer and then
     * uses provided negotiation {@code handler} to handle any response from
     * the party receiving the offer.
     *
     * @param offerorName  Name of offer sender.
     * @param receiverName Name of offer receiver.
     * @param validFor     The duration from the current time for which the
     *                     offer is to remain valid.
     * @param contracts    Offered contracts.
     * @param handler      Handler used to track and react to any acceptance,
     *                     rejection or counter-offers made by the party
     *                     receiving the offer.
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    default void offer(
        final String offerorName,
        final String receiverName,
        final Duration validFor,
        final List<TrustedContractDto> contracts,
        final ArTrustedNegotiationHandler handler)
    {
        final var now = Instant.now();
        offer(new TrustedContractOfferBuilder()
            .offerorName(offerorName)
            .receiverName(receiverName)
            .validAfter(now)
            .validUntil(now.plus(validFor))
            .contracts(contracts)
            .offeredAt(now)
            .build(), handler);
    }
}
