package se.arkalix.core.plugin.cp;

import java.time.Duration;

/**
 * Various constants relevant to the Arrowhead Contract services.
 */
@SuppressWarnings("unused")
public final class ArContractNegotiationConstants {
    private ArContractNegotiationConstants() {}

    /**
     * The skew tolerance accepted when comparing time constraints with the
     * current system time.
     */
    public static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofSeconds(30);

    /**
     * The time sent negotiation offers remain valid by default.
     */
    public static final Duration DEFAULT_OFFER_VALIDITY_PERIOD = Duration.ofHours(30);

    /**
     * Event handler topic used when negotiation updates are published.
     */
    public static final String TOPIC_SESSION_UPDATE = "ContractNegotiation.SessionUpdate";
}
