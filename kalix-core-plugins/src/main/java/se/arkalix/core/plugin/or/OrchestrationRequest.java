package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceProviderDto;
import se.arkalix.core.plugin.SystemDetailsDto;
import se.arkalix.core.plugin.sr.ServiceQueryDto;
import se.arkalix.query.ServiceQuery;

import java.util.*;

public class OrchestrationRequest {
    private boolean isDynamic = false;
    private List<ServiceProviderDto> providers;
    private Map<OrchestrationOption, Boolean> options;

    public boolean isDynamic() {
        return isDynamic;
    }

    public OrchestrationRequest isDynamic(final boolean isDynamic) {
        this.isDynamic = isDynamic;
        return this;
    }

    /**
     * A list of systems the querying system prefers to provide the requested
     * service.
     */
    public List<ServiceProviderDto> providers() {
        return providers == null
            ? Collections.emptyList()
            : providers;
    }

    public OrchestrationRequest providers(final ServiceProviderDto... providers) {
        return providers(Arrays.asList(providers));
    }

    public OrchestrationRequest providers(final List<ServiceProviderDto> providers) {
        this.providers = providers;
        return this;
    }

    public OrchestrationRequest option(final OrchestrationOption option, final boolean value) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.put(option, value);
        return this;
    }

    /**
     * A map of orchestration {@link OrchestrationOption options}.
     */
    public Map<OrchestrationOption, Boolean> options() {
        return options == null
            ? Collections.emptyMap()
            : options;
    }

    public OrchestrationRequest options(final Map<OrchestrationOption, Boolean> options) {
        this.options = options;
        return this;
    }

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
