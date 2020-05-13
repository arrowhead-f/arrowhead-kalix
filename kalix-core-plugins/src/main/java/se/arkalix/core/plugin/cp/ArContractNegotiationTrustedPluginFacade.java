package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;

import java.security.SecureRandom;
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
     * Random generator used for producing negotiation session identifiers.
     */
    SecureRandom RANDOM = new SecureRandom();

    /**
     * Sends negotiation {@code offer} and registers negotiation {@code
     * handler}.
     *
     * @param offer   Contract offer to make.
     * @param handler Handler used to track and react to any acceptance,
     *                rejection or counter-offers made by the party receiving
     *                the offer.
     */
    void offer(TrustedOfferDto offer, ArTrustedNegotiationHandler handler);

    /**
     * Sends negotiation offer and registers negotiation {@code handler}.
     *
     * @param offerorName  Name of offer sender. This is not the name of the
     *                     system making the offer, but the name of a
     *                     certificate owned by the system receiving the offer
     *                     message.
     * @param receiverName Name of offer receiver.
     * @param contracts    Offered contracts.
     * @param handler      Handler used to track and react to any acceptance,
     *                     rejection or counter-offers made by the party
     *                     receiving the offer.
     */
    default void offer(
        final String offerorName,
        final String receiverName,
        final List<TrustedContractDto> contracts,
        final ArTrustedNegotiationHandler handler)
    {
        offer(
            offerorName,
            receiverName,
            ArContractNegotiationConstants.DEFAULT_OFFER_VALIDITY_PERIOD,
            contracts,
            handler);
    }

    /**
     * Sends negotiation offer and registers negotiation {@code handler}.
     *
     * @param offerorName  Name of offer sender. This is not the name of the
     *                     system making the offer, but the name of a
     *                     certificate owned by the system receiving the offer
     *                     message.
     * @param receiverName Name of offer receiver.
     * @param validFor     The duration from the current time for which the
     *                     offer remains valid.
     * @param contracts    Offered contracts.
     * @param handler      Handler used to track and react to any acceptance,
     *                     rejection or counter-offers made by the party
     *                     receiving the offer.
     */
    default void offer(
        final String offerorName,
        final String receiverName,
        final Duration validFor,
        final List<TrustedContractDto> contracts,
        final ArTrustedNegotiationHandler handler)
    {
        final var now = Instant.now();
        offer(new TrustedOfferBuilder()
            .sessionId(RANDOM.nextLong())
            .offerSeq(0)
            .offerorName(offerorName)
            .receiverName(receiverName)
            .validAfter(now)
            .validUntil(now.plus(validFor))
            .contracts(contracts)
            .offeredAt(now)
            .build(), handler);
    }
}
