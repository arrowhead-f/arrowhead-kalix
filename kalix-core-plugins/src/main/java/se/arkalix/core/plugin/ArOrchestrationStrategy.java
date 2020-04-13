package se.arkalix.core.plugin;

/**
 * Enumerates the different kind of strategies that can be used when querying
 * an {@link ArOrchestration orchestration service} for {@link
 * se.arkalix.core.plugin.dto.ServiceConsumable services to consume}.
 * <p>
 * TODO: This enum needs to be replaced or complemented with some kind of
 * configurator that leaves more room for being very specific about
 * orchestration details (such as if inter-cloud orchestration is allowed or
 * not). The existing enum flags could probably be replaced with constants
 * containing default configurations representing the same strategies as
 * employed if using their current enum equivalents.
 */
public enum ArOrchestrationStrategy {
    /**
     * Only request predefined orchestration rules.
     * <p>
     * This strategy is also referred to as <i>store orchestration</i>. When
     * used, the {@link ArOrchestration orchestration service} in question is
     * consulted without telling it explicitly what services are wanted. The
     * orchestration service is assumed to already know of certain predefined
     * orchestration rules associated with the requesting {@link
     * se.arkalix.ArSystem system}, which are returned if available.
     */
    STORED_ONLY,

    /**
     * First request predefined orchestration rules, then request dynamic
     * orchestration rules if no suitable predefined rules were available.
     * <p>
     * In other words, if a rules-only request results in no {@link
     * se.arkalix.core.plugin.dto.ServiceConsumable entry} matching
     * whatever {@link se.arkalix.query.ServiceQuery query} caused the {@link
     * ArOrchestration orchestration service} to be contacted, a dynamic
     * orchestration attempt is made afterwards.
     */
    STORED_THEN_DYNAMIC,

    /**
     * Only request dynamic orchestration rules.
     * <p>
     * When used, the {@link ArOrchestration orchestration service} in question
     * is asked directly for explicitly specified services to consume.
     */
    DYNAMIC_ONLY,
}
