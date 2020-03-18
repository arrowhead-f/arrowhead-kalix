package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.*;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientResponseRejectedException;
import eu.arrowhead.kalix.plugin.Plug;
import eu.arrowhead.kalix.plugin.Plugin;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

public class HttpServiceRegistrationPlugin implements Plugin {
    private final String basePath;
    private final DataEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private HttpServiceRegistry serviceRegistry = null;

    public HttpServiceRegistrationPlugin(final Builder builder) {
        basePath = builder.basePath;
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");
    }

    @Override
    public void onAttach(final Plug plug) throws Exception {
        serviceRegistry = new HttpServiceRegistry.Builder()
            .basePath(basePath)
            .client(HttpClient.from(plug.system()))
            .encoding(encoding)
            .remoteSocketAddress(remoteSocketAddress)
            .build();
    }

    @Override
    public Future<?> onServiceProvided(final Plug plug, final ServiceDescription service) {
        final var system = plug.system();
        final var form = new ServiceRecordFormBuilder()
            .name(service.name())
            .provider(new SystemDefinitionFormBuilder()
                .name(system.name())
                .hostname(system.localAddress().getHostAddress())
                .port(system.localPort())
                .publicKeyBase64(system.isSecure()
                    ? system.keyStore().publicKeyBase64()
                    : null)
                .build())
            .qualifier(service.qualifier())
            .security(service.security())
            .metadata(service.metadata())
            .version(service.version())
            .supportedInterfaces(service.supportedInterfaces())
            .build();

        return serviceRegistry.register(form)
            .flatMapCatch(HttpClientResponseRejectedException.class, fault -> {
                if (fault.status() == HttpStatus.BAD_REQUEST) {
                    return serviceRegistry.unregister(service.name(), system.name(), system.localSocketAddress())
                        .flatMap(ignored -> serviceRegistry.register(form).pass(null));
                }
                return Future.failure(fault);
            });
    }

    @Override
    public void onServiceDismissed(final Plug plug, final ServiceDescription service) {
        final var system = plug.system();
        serviceRegistry.unregister(service.name(), system.name(), system.localSocketAddress())
            .onFailure(fault -> {
                System.err.println("Failed to unregister service \"" + service.name() + "\"; cause:");
                fault.printStackTrace(); // TODO: Log properly.
            });
    }

    public static class Builder {
        private String basePath;
        private DataEncoding encoding;
        private InetSocketAddress remoteSocketAddress;

        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder encoding(final DataEncoding encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder remoteSocketAddress(final InetSocketAddress remoteSocketAddress) {
            this.remoteSocketAddress = remoteSocketAddress;
            return this;
        }

        public HttpServiceRegistrationPlugin build() {
            return new HttpServiceRegistrationPlugin(this);
        }
    }
}
