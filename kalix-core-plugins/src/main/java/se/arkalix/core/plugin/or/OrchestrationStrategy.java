package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceConsumable;

import java.util.List;
import java.util.Objects;

/**
 * Enumerates the different kind of strategies that can be used when querying
 * an {@link ArOrchestrationService orchestration service} for {@link
 * ServiceConsumable services to consume}.
 */
@SuppressWarnings("unused")
public class OrchestrationStrategy {
    private final List<OrchestrationPattern> patterns;

    /**
     * Only request predefined orchestration rules.
     * <p>
     * This strategy is also referred to as <i>store orchestration</i>. When
     * used, the {@link ArOrchestrationService orchestration service} in question is
     * consulted without telling it explicitly what services are wanted. The
     * orchestration service is assumed to already know of certain predefined
     * orchestration rules associated with the requesting {@link
     * se.arkalix.ArSystem system}, which are returned if available.
     */
    public static final OrchestrationStrategy STORED_ONLY = new OrchestrationStrategy(
        new OrchestrationPattern()
            .isIncludingService(false));

    /**
     * First request predefined orchestration rules, then request dynamic
     * orchestration rules if no suitable predefined rules were available.
     * <p>
     * In other words, if a rules-only request results in no {@link
     * ServiceConsumable entry} matching
     * whatever {@link se.arkalix.query.ServiceQuery query} caused the {@link
     * ArOrchestrationService orchestration service} to be contacted, a dynamic
     * orchestration attempt is made afterwards.
     */
    public static final OrchestrationStrategy STORED_THEN_DYNAMIC = new OrchestrationStrategy(
        new OrchestrationPattern()
            .isIncludingService(false),
        new OrchestrationPattern()
            .option(OrchestrationOption.OVERRIDE_STORE, true)
            .option(OrchestrationOption.PING_PROVIDERS, true));

    /**
     * Only request dynamic orchestration rules.
     * <p>
     * When used, the {@link ArOrchestrationService orchestration service} in question
     * is asked directly for explicitly specified services to consume.
     */
    public static final OrchestrationStrategy DYNAMIC_ONLY = new OrchestrationStrategy(
        new OrchestrationPattern()
            .option(OrchestrationOption.OVERRIDE_STORE, true)
            .option(OrchestrationOption.PING_PROVIDERS, true));

    /**
     * Creates new orchestration strategy, which entails attempting the given
     * orchestration requests in order until a matching service is found.
     *
     * @param patterns Requests to attempt.
     */
    public OrchestrationStrategy(final OrchestrationPattern... patterns) {
        this(List.of(patterns));
    }

    /**
     * Creates new orchestration strategy, which entails attempting the given
     * orchestration requests in order until a matching service is found.
     *
     * @param patterns Requests to attempt.
     */
    public OrchestrationStrategy(final List<OrchestrationPattern> patterns) {
        this.patterns = Objects.requireNonNull(patterns, "requests");
    }

    /**
     * Orchestration strategy requests to attempt.
     *
     * @return Requests.
     */
    public List<OrchestrationPattern> patterns() {
        return patterns;
    }
}
