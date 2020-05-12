package se.arkalix.core.plugin.cp;

import se.arkalix.util.concurrent.Future;

import java.time.Instant;

public interface ArContractNegotiationTrusted {
    Future<?> accept(TrustedAcceptanceDto acceptance);

    default Future<?> accept(final long sessionId) {
        return accept(new TrustedAcceptanceBuilder()
            .sessionId(sessionId)
            .acceptedAt(Instant.now())
            .build());
    }

    Future<?> offer(TrustedOfferDto offer);

    Future<?> reject(TrustedRejectionDto rejection);

    default Future<?> reject(final long sessionId) {
        return reject(new TrustedRejectionBuilder()
            .sessionId(sessionId)
            .rejectedAt(Instant.now())
            .build());
    }
}
