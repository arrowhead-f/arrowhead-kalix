package eu.arrowhead.kalix.core.plugins.srv;

import eu.arrowhead.kalix.core.plugins.srv.dto.ServiceRegistrationData;
import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.client.HttpClient;
import eu.arrowhead.kalix.net.http.client.HttpClientRequest;
import eu.arrowhead.kalix.net.http.client.HttpClientResponseException;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * A {@link AhfServiceRegistry} that communicated with over HTTP.
 */
public class HttpServiceRegistry implements AhfServiceRegistry {
    private final HttpClient client;
    private final DataEncoding encoding;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriRegister;
    private final String uriUnregister;

    private HttpServiceRegistry(final Builder builder) {
        client = Objects.requireNonNull(builder.client, "Expected client");
        encoding = Objects.requireNonNull(builder.encoding, "Expected encoding");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/serviceregistry");
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public Future<?> register(final ServiceRegistrationData registration) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(HttpMethod.POST)
                .uri(uriRegister)
                .body(encoding, registration))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.header("content-length").isPresent()) {
                    return response.bodyAsString()
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to register service \"" + registration.name() + "\""));
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

    /**
     * Builder useful for constructing {@link HttpServiceRegistry} instances.
     */
    public static class Builder {
        private String basePath;
        private HttpClient client;
        private DataEncoding encoding;
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
         * HTTP client to use for making requests. <b>Must be specified.</b>
         *
         * @param client HTTP client.
         * @return This builder.
         */
        public Builder client(final HttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Encoding that must be used to encode and decode messages sent to and
         * received from the service registry. <b>Must be specified.</b>
         *
         * @param encoding HTTP body encoding.
         * @return This builder.
         */
        public Builder encoding(final DataEncoding encoding) {
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
         * Finishes construction of {@link HttpServiceRegistry}.
         *
         * @return New HTTP service registry object.
         */
        public HttpServiceRegistry build() {
            return new HttpServiceRegistry(this);
        }
    }
}
