package eu.arrowhead.kalix.net.http;

import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.descriptor.ServiceDescriptor;
import eu.arrowhead.kalix.net.http.service.HttpService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HttpArrowheadSystem extends ArrowheadSystem<HttpService> {
    private final HashSet<HttpService> providedServices = new HashSet<>();
    private final HashSet<ServiceDescriptor> providedServiceDescriptors = new HashSet<>();

    private HttpArrowheadSystem(final Builder builder) {
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

    @Override
    public synchronized void dismissAllServices() {
        providedServices.clear(); // TODO: Stop services.
        providedServiceDescriptors.clear();
    }

    public static class Builder extends ArrowheadSystem.Builder<Builder, HttpArrowheadSystem> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public HttpArrowheadSystem build() {
            return new HttpArrowheadSystem(this);
        }
    }
}
