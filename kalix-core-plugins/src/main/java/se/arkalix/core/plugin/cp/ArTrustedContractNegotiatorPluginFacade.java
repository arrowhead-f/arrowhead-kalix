package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;
import se.arkalix.util.concurrent.Future;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link PluginFacade Facade} allowing for contract negotiations to be carried
 * out with state management support.
 */
@SuppressWarnings("unused")
public interface ArTrustedContractNegotiatorPluginFacade extends PluginFacade {
    /**
     * Listens for incoming contract offers to a certain receiver.
     * <p>
     * More specifically, this method sets the factory method invoked to create
     * a new negotiation handler whenever an offer is received that is not
     * associated with an existing negotiation session. If no factory method is
     * set, such offers are ignored.
     *
     * @param receiverName   Name of offer receiver.
     * @param handlerFactory Function called to create a new negotiation handler
     *                       every time a contract offer not associated with an
     *                       existing negotiation session is received.
     */
    void listen(final String receiverName, Supplier<TrustedContractNegotiatorHandler> handlerFactory);

    /**
     * Starts new contract negotiation by making given {@code offer} and then
     * uses provided negotiation {@code handler} to handle any response from
     * the party receiving the offer.
     *
     * @param offer   Contract offer to make.
     * @param handler Handler used to track and react to any acceptance,
     *                rejection or counter-offers made by the party receiving
     *                the offer.
     * @return Future completed successfully with a unique negotiation
     * identifier if the offer could be submitted to its intended receiver, or
     * a proxy acting for it.
     */
    Future<Long> offer(TrustedContractOfferDto offer, TrustedContractNegotiatorHandler handler);

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
     * @return Future completed successfully with a unique negotiation
     * identifier if the offer could be submitted to its intended receiver, or
     * a proxy acting for it.
     * @see se.arkalix.core.plugin.cp Package documentation for details about names
     */
    default Future<Long> offer(
        final String offerorName,
        final String receiverName,
        final Duration validFor,
        final List<TrustedContractDto> contracts,
        final TrustedContractNegotiatorHandler handler)
    {
        final var now = Instant.now();
        return offer(new TrustedContractOfferBuilder()
            .offerorName(offerorName)
            .receiverName(receiverName)
            .validAfter(now)
            .validUntil(now.plus(validFor))
            .contracts(contracts)
            .offeredAt(now)
            .build(), handler);
    }
}
