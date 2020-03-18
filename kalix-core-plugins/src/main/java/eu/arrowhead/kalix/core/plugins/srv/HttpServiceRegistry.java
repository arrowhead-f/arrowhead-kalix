package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.ServiceRecordFormData;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.net.http.client.HttpClientResponseException;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

public class HttpServiceRegistry implements AhfServiceRegistry {
    private final HttpClient client;
    private final DataEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriRegister;
    private final String uriUnregister;

    public HttpServiceRegistry(final Builder builder) {
        client = Objects.requireNonNull(builder.client, "Expected client");
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/serviceregistry");
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public Future<?> register(final ServiceRecordFormData form) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri(uriRegister)
                .body(encoding, form))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.header("content-length").isPresent()) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to register service \"" + form.name() + "\""));
            });
    }

    @Override
    public Future<?> unregister(final String serviceName, final String systemName, final InetSocketAddress systemSocketAddress) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.DELETE)
                .uri(uriUnregister)
                .queryParameter("service_definition", serviceName)
                .queryParameter("system_name", systemName)
                .queryParameter("address", systemSocketAddress.getHostString())
                .queryParameter("port", Integer.toString(systemSocketAddress.getPort())))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.header("content-length").map(Integer::parseInt).orElse(0) > 0) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to unregister service \"" + serviceName + "\""));
            });
    }

    public static class Builder {
        private String basePath;
        private HttpClient client;
        private DataEncoding encoding;
        private InetSocketAddress remoteSocketAddress;

        public Builder basePath(final String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder client(final HttpClient client) {
            this.client = client;
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

        public HttpServiceRegistry build() {
            return new HttpServiceRegistry(this);
        }
    }
}
