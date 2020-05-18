package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.sr.ServiceQuery;

import java.util.Objects;

/**
 * An {@link OrchestrationQuery orchestration query} option.
 * <p>
 * Options like the ones listed below may be used to affect the behavior of an
 * {@link ArOrchestrationService orchestration service} while it handles {@link
 * OrchestrationQuery orchestration queries}.
 */
public final class OrchestrationOption {
    private final String name;

    private OrchestrationOption(final String name) {
        this.name = name;
    }

    /**
     * If set to {@code true}, only a single service is to be included in the
     * {@link OrchestrationQueryResult result}, unless no
     * {@link OrchestrationQuery matching services} are available at all.
     */
    public static final OrchestrationOption MATCHMAKING = new OrchestrationOption("matchmaking");

    /**
     * If set to {@code true}, the {@link OrchestrationQuery#service() service}
     * {@link ServiceQuery#metadata() metadata pairs} in the {@link
     * OrchestrationQuery query}, if any, will be considered while searching
     * for matching services.
     */
    public static final OrchestrationOption METADATA_SEARCH = new OrchestrationOption("metadataSearch");

    /**
     * If set to {@code true}, only services provided by the {@link
     * OrchestrationQuery#providers() systems listed in the query} will be
     * included in the {@link OrchestrationQuery query} {@link
     * OrchestrationQueryResult result}.
     */
    public static final OrchestrationOption ONLY_PREFERRED = new OrchestrationOption("onlyPreferred");

    /**
     * If set to {@code true}, causes all services whose providers do not
     * respond to a PING message to be removed from the {@link
     * OrchestrationQuery query} {@link OrchestrationQueryResult result}.
     */
    public static final OrchestrationOption PING_PROVIDERS = new OrchestrationOption("pingProviders");

    /**
     * If set to {@code true}, <i>dynamic orchestration</i> is enabled, which
     * means that, if allowed, the orchestrator will ignore its orchestration
     * rules and see if any mathing service exists that can be included in the
     * {@link OrchestrationQuery query} {@link OrchestrationQueryResult result}.
     */
    public static final OrchestrationOption OVERRIDE_STORE = new OrchestrationOption("overrideStore");

    /**
     * If set to {@code true}, the services provided by the local cloud will be
     * ignored in favor of those offered by other clouds.
     */
    public static final OrchestrationOption TRIGGER_INTER_CLOUD = new OrchestrationOption("triggerInterCloud");

    /**
     * If set to {@code true}, the {@link OrchestrationQuery query} is allowed
     * to include services situated in other local clouds.
     */
    public static final OrchestrationOption ENABLE_INTER_CLOUD = new OrchestrationOption("enableInterCloud");

    /**
     * Resolves {@link OrchestrationOption} from given {@code name}.
     *
     * @param name Name to resolve. Case sensitive.
     * @return Cached or new {@link OrchestrationOption}.
     */
    public static OrchestrationOption valueOf(final String name) {
        switch (Objects.requireNonNull(name, "name")) {
        case "matchmaking": return MATCHMAKING;
        case "metadataSearch": return METADATA_SEARCH;
        case "onlyPreferred": return ONLY_PREFERRED;
        case "pingProviders": return PING_PROVIDERS;
        case "overrideStore": return OVERRIDE_STORE;
        case "triggerInterCloud": return TRIGGER_INTER_CLOUD;
        case "enableInterCloud": return ENABLE_INTER_CLOUD;
        default:
            return new OrchestrationOption(name);
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final OrchestrationOption that = (OrchestrationOption) other;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
