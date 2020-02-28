package eu.arrowhead.kalix;

import eu.arrowhead.kalix.descriptor.ServiceDescriptor;
import eu.arrowhead.kalix.http.service.HttpService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArrowheadSystemHttp extends ArrowheadSystem<HttpService> {
    private final HashSet<HttpService> providedServices = new HashSet<>();
    private final HashSet<ServiceDescriptor> providedServiceDescriptors = new HashSet<>();

    private ArrowheadSystemHttp(final Builder builder) {
        super(builder);
    }

    @Override
    public synchronized Set<ServiceDescriptor> providedServices() {
        return Collections.unmodifiableSet(providedServiceDescriptors);
    }

    @Override
    public synchronized void provideService(final HttpService service) {
        if (providedServices.add(service)) {
            // TODO: Start service.
        }
    }

    @Override
    public synchronized void dismissService(final HttpService service) {
        if (providedServices.remove(service)) {
            // TODO: Stop service.
        }
    }

    public static class Builder extends ArrowheadSystem.Builder<Builder, ArrowheadSystemHttp> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ArrowheadSystemHttp build() {
            return new ArrowheadSystemHttp(this);
        }
    }
}
