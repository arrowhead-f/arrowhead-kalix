package se.arkalix.core.plugin.cp;

import java.time.Duration;

@SuppressWarnings("unused")
public final class ContractProxyConstants {
    private ContractProxyConstants() {}

    public static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofSeconds(30);
    public static final Duration DEFAULT_OFFER_VALIDITY_PERIOD = Duration.ofHours(30);

    public static final String TOPIC = "ContractProxy.SessionUpdate";
}
