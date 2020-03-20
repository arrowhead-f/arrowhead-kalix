package se.arkalix.core.plugin.srv;

import se.arkalix.core.plugin.srv.dto.ServiceRegistrationBuilder;
import se.arkalix.core.plugin.srv.dto.SystemDefinitionBuilder;
import se.arkalix.description.ServiceDescription;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.ArSystem;
import se.arkalix.http.HttpStatus;
import se.arkalix.http.client.HttpClient;
import se.arkalix.http.client.HttpClientResponseRejectedException;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * A plugin that manages automatic registration and unregistration of services
 * with a specific HTTP service registry.
 * <p>
 * The plugin must be instantiated and then provided to one or more
 * {@link ArSystem systems} before they provide any
 * services. When those systems are assigned services to provide, or have their
 * services dismissed, this plugin will automatically attempt to register or
 * unregister those services from one particular HTTP service registry.
 */
public class HttpServiceRegistrationPlugin implements Plugin {
    private final String basePath;
    private final DtoEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private HttpServiceRegistry serviceRegistry = null;

    private HttpServiceRegistrationPlugin(final Builder builder) {
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
        final var registration = new ServiceRegistrationBuilder()
            .name(service.name())
            .provider(new SystemDefinitionBuilder()
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

        return serviceRegistry.register(registration)
            .flatMapCatch(HttpClientResponseRejectedException.class, fault -> {
                // If registration fails with 400 BAD REQUEST, try to unregister it and then try again.
                if (fault.status() == HttpStatus.BAD_REQUEST) {
                    return serviceRegistry.unregister(service.name(), system.name(), system.localSocketAddress())
                        .flatMap(ignored -> serviceRegistry.register(registration).pass(null));
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

    /**
     * Builder useful for constructing {@link HttpServiceRegistrationPlugin}
     * instances.
     */
    public static class Builder {
        private String basePath;
        private DtoEncoding encoding;
        private InetSocketAddress remoteSocketAddress;

        /**
         * Service registry base path.
         * <p>
         * Defaults to "/serviceregistry".
         *
         * @param basePath Base path.
         * @return This builder.
         */
        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        /**
         * Encoding that must be used to encode and decode messages sent to and
         * received from the service registry. <b>Must be specified.</b>
         *
         * @param encoding HTTP body encoding.
         * @return This builder.
         */
        public Builder encoding(final DtoEncoding encoding) {
            this.encoding = encoding;
            return this;
        }

        /**
         * The hostname/port of the service registry. <b>Must be specified.</b>
         *
         * @param remoteSocketAddress Hostname/port.
         * @return This builder.
         */
        public Builder remoteSocketAddress(final InetSocketAddress remoteSocketAddress) {
            this.remoteSocketAddress = remoteSocketAddress;
            return this;
        }

        /**
         * Finishes construction of {@link HttpServiceRegistrationPlugin}.
         *
         * @return New HTTP service registry plugin.
         */
        public HttpServiceRegistrationPlugin build() {
            return new HttpServiceRegistrationPlugin(this);
        }
    }
}
