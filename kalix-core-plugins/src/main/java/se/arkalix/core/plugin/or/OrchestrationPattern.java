package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceProviderDto;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.query.ServiceQuery;

import java.util.*;

/**
 * Describes a particular way in which orchestration rules can be queried from
 * an orchestration service.
 */
@SuppressWarnings("unused")
public class OrchestrationPattern {
    private boolean isDynamic = false;
    private List<ServiceProviderDto> providers;
    private Map<OrchestrationOption, Boolean> options;

    /**
     * Whether or not to use dynamic orchestration, which means that the
     * service requested is explicitly asked for.
     *
     * @return {@code true} only if dynamic orchestration is to be used.
     */
    public boolean isDynamic() {
        return isDynamic;
    }

    /**
     * Sets whether ot not dynamic orchestration is to be used, which means
     * that requested services are explicitly asked for.
     *
     * @param isDynamic {@code true} if dynamic orchestration is to be used.
     * @return This pattern.
     */
    public OrchestrationPattern isDynamic(final boolean isDynamic) {
        this.isDynamic = isDynamic;
        return this;
    }

    /**
     * Whether or not this matches the so-called plain storage pattern, which
     * is non-dynamic, has no provider preference and no options. The pattern
     * will result in queries where all orchestration store rules are requested
     * for the requesting system.
     *
     * @return {@code true} only if plain storage orchestration is to be used.
     */
    public boolean isPlainStorePattern() {
        return !isDynamic && providers == null || providers.isEmpty() && options == null || options.isEmpty();
    }

    /**
     * A list of systems the querying system prefers to provide the requested
     * service.
     *
     * @return Preferred service providers.
     */
    public List<ServiceProviderDto> providers() {
        return providers == null
            ? Collections.emptyList()
            : providers;
    }

    /**
     * Sets preferred service providers.
     *
     * @param providers Preferred service providers.
     * @return This pattern.
     */
    public OrchestrationPattern providers(final ServiceProviderDto... providers) {
        return providers(Arrays.asList(providers));
    }

    /**
     * Sets preferred service providers.
     *
     * @param providers Preferred service providers.
     * @return This pattern.
     */
    public OrchestrationPattern providers(final List<ServiceProviderDto> providers) {
        this.providers = providers;
        return this;
    }

    /**
     * Sets orchestration option.
     *
     * @param option Option to set.
     * @param value  Option value.
     * @return This pattern.
     */
    public OrchestrationPattern option(final OrchestrationOption option, final boolean value) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.put(option, value);
        return this;
    }

    /**
     * A map of orchestration {@link OrchestrationOption options}.
     *
     * @return Pattern orchestration options.
     */
    public Map<OrchestrationOption, Boolean> options() {
        return options == null
            ? Collections.emptyMap()
            : options;
    }

    /**
     * Sets orchestration options, replacing any such previously set.
     *
     * @param options Options to set.
     * @return This pattern.
     */
    public OrchestrationPattern options(final Map<OrchestrationOption, Boolean> options) {
        this.options = options;
        return this;
    }

    /**
     * Converts this pattern into a concrete orchestration query.
     *
     * @param requester A description of the system making the query.
     * @param service   The service desired by the requester.
     * @return New orchestration query.
     */
    public OrchestrationQueryDto toQuery(final SystemDetailsDto requester, final ServiceQuery service) {
        final var useService = isDynamic && service != null;
        final Map<OrchestrationOption, Boolean> options0;
        if (useService && !service.metadata().isEmpty() && !options.get(OrchestrationOption.METADATA_SEARCH)) {
            options0 = new HashMap<>(options);
            options0.put(OrchestrationOption.METADATA_SEARCH, true);
        }
        else {
            options0 = options;
        }
        return new OrchestrationQueryBuilder()
            .requester(requester)
            .service(useService ? se.arkalix.core.plugin.sr.ServiceQuery.from(service) : null)
            .providers(providers)
            .options(options0)
            .build();
    }
}
