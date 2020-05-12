package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;
import se.arkalix.util.concurrent.Future;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface ArContractNegotiationTrustedPluginFacade extends PluginFacade {
    SecureRandom RANDOM = new SecureRandom();

    void offer(TrustedOfferDto offer, ArTrustedNegotiationHandler handler);

    default void offer(
        final String offerorName,
        final String receiverName,
        final List<TrustedContractDto> contracts,
        final ArTrustedNegotiationHandler handler)
    {
        offer(
            offerorName,
            receiverName,
            ContractProxyConstants.DEFAULT_OFFER_VALIDITY_PERIOD,
            contracts,
            handler);
    }

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
            .offerorName(offerorName)
            .receiverName(receiverName)
            .validAfter(now)
            .validUntil(now.plus(validFor))
            .contracts(contracts)
            .offeredAt(now)
            .build(), handler);
    }
}
