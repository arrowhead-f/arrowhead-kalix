package eu.arrowhead.kalix;

import eu.arrowhead.kalix.concurrent.Future;
import eu.arrowhead.kalix.security.KeyStore;
import eu.arrowhead.kalix.security.TrustStore;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Represents an abstract Arrowhead Framework (AHF) system.
 * <p>
 * An instance of this class is used manage the existence of a single AHF
 * system, which exists to <i>provide</i> and <i>consume</i>
 * {@link ArrowheadService}s.
 * <p>
 * Provided services are instantiated and given, with any other configuration
 * details, to some subclass of the abstract {@link Builder}, which should be
 * useful for creating concrete instances of this class. If no services are
 * given, no services are provided by the system.
 * <p>
 * Once building an instance is complete, the {@link #start()} method is used
 * to start the services, if any, and acquire an {@link ArrowheadClient}
 * object, which can be used to consume the services of remote AHF systems.
 */
public class ArrowheadSystem {
    private final String name;
    private final InetSocketAddress inetSocketAddress;
    private final KeyStore keyStore;
    private final TrustStore trustStore;
    private final List<ArrowheadService> providedServices;

    protected ArrowheadSystem(final Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "System name is mandatory");
        this.inetSocketAddress = Objects.requireNonNullElseGet(builder.inetSocketAddress,
            () -> new InetSocketAddress(0));
        this.keyStore = builder.keyStore;
        this.trustStore = builder.trustStore;
        this.providedServices = builder.providedServices;
    }

    public String getName() {
        return name;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public static class Builder {
        private String name;
        private InetSocketAddress inetSocketAddress;
        private KeyStore keyStore;
        private TrustStore trustStore;
        private List<ArrowheadService> providedServices = new ArrayList<>(0);

        public final Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public final Builder setInetSocketAddress(final InetSocketAddress inetSocketAddress) {
            this.inetSocketAddress = inetSocketAddress;
            return this;
        }

        public final Builder setKeyStore(final KeyStore keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public final Builder setTrustStore(final TrustStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public final Builder setProducedServices(List<ArrowheadService> providedServices) {
            this.providedServices = providedServices;
            return this;
        }

        public final Builder setProducedServices(ArrowheadService... services) {
            this.providedServices = Arrays.asList(services);
            return this;
        }

        public final Builder addProducedService(final ArrowheadService service) {
            this.providedServices.add(service);
            return this;
        }

        public final Builder addProducedServices(final ArrowheadService... services) {
            Collections.addAll(this.providedServices, services);
            return this;
        }

        public ArrowheadSystem build() {
            return new ArrowheadSystem(this);
        }
    }
}
