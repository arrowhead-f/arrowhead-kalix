package se.arkalix.core.plugin.dto;

import java.util.Objects;

/**
 * A warning included in a {@link ServiceConsumable} in the
 * {@link OrchestrationQueryResult results} of an {@link OrchestrationQuery
 * orchestration query}.
 */
public final class OrchestrationWarning {
    private final String name;

    private OrchestrationWarning(final String name) {
        this.name = name;
    }

    /**
     * Signifies that a {@link ServiceConsumable certain service} is located
     * outside the local cloud of the receiving system.
     */
    public static final OrchestrationWarning FROM_OTHER_CLOUD = new OrchestrationWarning("FROM_OTHER_CLOUD");

    /**
     * Signifies that the service registry entry of a {@link ServiceConsumable
     * certain service} is about to expire.
     */
    public static final OrchestrationWarning TTL_EXPIRING = new OrchestrationWarning("TTL_EXPIRING");

    /**
     * Signifies that the service registry entry of a {@link ServiceConsumable
     * certain service} recently expired.
     */
    public static final OrchestrationWarning TTL_EXPIRED = new OrchestrationWarning("TTL_EXPIRED");

    /**
     * Signifies that the service registry entry of a {@link ServiceConsumable
     * certain service} might be stale.
     */
    public static final OrchestrationWarning TTL_UNKNOWN = new OrchestrationWarning("TTL_UNKNOWN");

    /**
     * Resolves {@link OrchestrationWarning} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link OrchestrationWarning}.
     */
    public static OrchestrationWarning valueOf(final String name) {
        switch (Objects.requireNonNull(name, "name").toUpperCase()) {
        case "FROM_OTHER_CLOUD": return FROM_OTHER_CLOUD;
        case "TTL_EXPIRING": return TTL_EXPIRING;
        case "TTL_EXPIRED": return TTL_EXPIRED;
        case "TTL_UNKNOWN": return TTL_UNKNOWN;
        default:
            return new OrchestrationWarning(name);
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final OrchestrationWarning that = (OrchestrationWarning) other;
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
