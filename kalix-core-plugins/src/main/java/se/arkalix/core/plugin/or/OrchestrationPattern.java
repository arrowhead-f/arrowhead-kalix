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
    private boolean isIncludingService = true;
    private List<ServiceProviderDto> providers;
    private Map<OrchestrationOption, Boolean> options;

    /**
     * Use {@link #isIncludingService()} instead.
     *
     * @return {@code true} if requested services are to be included in
     * orchestration requests.
     */
    @Deprecated(since = "0.4.2")
    public boolean isDynamic() {
        return isIncludingService;
    }

    /**
     * Whether or not the specific service requested is to be included in
     * orchestration requests. Defaults to {@code true}.
     *
     * @return {@code true} only if dynamic orchestration is to be used.
     */
    public boolean isIncludingService() {
        return isIncludingService;
    }

    /**
     * Use {@link #isIncludingService(boolean)} instead.
     *
     * @param isDynamic {@code true} if requested services are to be included in
     *                  orchestration requests.
     * @return This pattern.
     */
    @Deprecated(since = "0.4.2")
    public OrchestrationPattern isDynamic(final boolean isDynamic) {
        this.isIncludingService = isDynamic;
        return this;
    }

    /**
     * Sets whether or not the specific service requested is to be included in
     * orchestration requests. Defaults to {@code true}.
     *
     * @param isIncludingService {@code true} if requested services are to be
     *                           included in orchestration requests.
     * @return This pattern.
     */
    public OrchestrationPattern isIncludingService(final boolean isIncludingService) {
        this.isIncludingService = isIncludingService;
        return this;
    }

    /**
     * Whether or not this matches the so-called plain storage pattern, which
     * does not include the requested services, has no provider preference and
     * no options. The pattern will result in queries where all orchestration
     * store rules are requested for the requesting system.
     *
     * @return {@code true} only if plain storage orchestration is to be used.
     */
    public boolean isPlainStorePattern() {
        return !isIncludingService && (providers == null || providers.isEmpty()) && (options == null || options.isEmpty());
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
     * @param query     The service desired by the requester.
     * @return New orchestration query.
     */
    public OrchestrationQueryDto toQuery(final SystemDetailsDto requester, final ServiceQuery query) {
        return new OrchestrationQueryDto.Builder()
            .requester(requester)
            .service(isIncludingService && query != null
                ? se.arkalix.core.plugin.sr.ServiceQuery.from(query)
                : null)
            .providers(providers)
            .options(options)
            .build();
    }
}
