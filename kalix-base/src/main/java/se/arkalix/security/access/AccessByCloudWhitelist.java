package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.description.ConsumerDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.*;

/**
 * Same-cloud and whitelist access policy.
 * <p>
 * A consuming system is granted access only if it can (1) present a
 * certificate issued by the same local cloud certificate as the system using
 * this policy, as well as (2) the {@link se.arkalix.security.identity system
 * name} of the consuming system exists in the white-list of this instance.
 * <p>
 * Note that access policy instances of this type can be shared by multiple
 * services.
 */
public class AccessByCloudWhitelist implements AccessPolicy {
    private final Set<String> whitelist;

    /**
     * Creates new same-cloud and whitelist access policy.
     *
     * @param whitelist Collection of names of systems to be allowed access.
     */
    public AccessByCloudWhitelist(final Collection<String> whitelist) {
        this.whitelist = Collections.synchronizedSet(new HashSet<>(
            Objects.requireNonNull(whitelist, "Expected whitelist")));
    }

    /**
     * @return Modifiable and synchronized set containing white-listed system
     * names.
     */
    @ThreadSafe
    public Set<String> whitelist() {
        return whitelist;
    }

    @Override
    public SecurityDescriptor descriptor() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(
        final ConsumerDescription consumer,
        final ArSystem provider,
        final ServiceDescription service,
        final String token)
    {
        Objects.requireNonNull(consumer, "Expected consumer");
        Objects.requireNonNull(service, "Expected service");

        return Objects.equals(consumer.identity().cloud(), provider.identity().cloud()) &&
            whitelist.contains(consumer.name());
    }
}
