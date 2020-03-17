package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.*;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.plugin.Plug;
import eu.arrowhead.kalix.plugin.Plugin;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

public class HttpServiceRegistrationPlugin implements Plugin {
    private final DataEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriRegister;
    private final String uriUnregister;

    public HttpServiceRegistrationPlugin(final Builder builder) {
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/serviceregistry");
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public Future<?> onServiceProvided(final Plug plug, final ServiceDescription service) throws Exception {
        final var system = plug.system();
        return HttpClient.from(system)
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri(uriRegister)
                .body(encoding, new ServiceRecordFormBuilder()
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
                    .expiresAt(null) // TODO: Short expiration and regular refresh.
                    .security(service.security())
                    .metadata(service.metadata())
                    .version(service.version())
                    .supportedInterfaces(service.supportedInterfaces())
                    .build()));
    }

    @Override
    public void onServiceDismissed(final Plug plug, final ServiceDescription service) throws Exception {
        final var system = plug.system();
        HttpClient.from(system)
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.DELETE)
                .uri(uriUnregister)
                .queryParameter("service_definition", service.name())
                .queryParameter("system_name", system.name())
                .queryParameter("address", system.localAddress().getHostAddress())
                .queryParameter("port", Integer.toString(system.localPort())))
            .onFailure(Throwable::printStackTrace); // TODO: Log properly.
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
