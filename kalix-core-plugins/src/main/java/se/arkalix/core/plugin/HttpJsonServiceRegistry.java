package se.arkalix.core.plugin;

import se.arkalix.core.plugin.dto.ServiceQueryDto;
import se.arkalix.core.plugin.dto.ServiceQueryResultDto;
import se.arkalix.core.plugin.dto.ServiceRegistrationDto;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

import static se.arkalix.dto.DtoEncoding.JSON;
import static se.arkalix.net.http.HttpMethod.DELETE;
import static se.arkalix.net.http.HttpMethod.POST;

/**
 * A remote {@link ArServiceRegistry} that is communicated with via HTTP/JSON
 * in either secure or insecure mode.
 */
public class HttpJsonServiceRegistry implements ArServiceRegistry {
    private final HttpClient client;
    private final InetSocketAddress remoteSocketAddress;

    private final String uriQuery;
    private final String uriRegister;
    private final String uriUnregister;

    private HttpJsonServiceRegistry(final Builder builder) {
        client = Objects.requireNonNull(builder.client, "Expected client");
        remoteSocketAddress = Objects.requireNonNull(builder.remoteSocketAddress, "Expected remoteSocketAddress");

        final var basePath = Objects.requireNonNullElse(builder.basePath, "/serviceregistry");
        uriQuery = basePath + "/query";
        uriRegister = basePath + "/register";
        uriUnregister = basePath + "/unregister";
    }

    @Override
    public Future<ServiceQueryResultDto> query(final ServiceQueryDto query) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(POST)
                .uri(uriQuery)
                .body(JSON, query))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString() // TODO: Parse error message and present better string.
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to query " +
                    "service registry for \"" + query.name() + "\""));
            });
    }

    @Override
    public Future<?> register(final ServiceRegistrationDto registration) {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(POST)
                .uri(uriRegister)
                .body(JSON, registration))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString() // TODO: Parse error message and present better string.
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to register service \"" + registration.name() + "\""));
            });
    }

    @Override
    public Future<?> unregister(
        final String serviceName,
        final String systemName,
        final String hostname,
        final int port)
    {
        return client
            .send(remoteSocketAddress, new HttpClientRequest()
                .method(DELETE)
                .uri(uriUnregister)
                .queryParameter("service_definition", serviceName)
                .queryParameter("system_name", systemName)
                .queryParameter("address", hostname)
                .queryParameter("port", Integer.toString(port)))
            .flatMap(response -> {
                final var status = response.status();
                if (status.isSuccess()) {
                    return Future.done();
                }
                if (status.isClientError() && response.headers().getAsInteger("content-length").orElse(0) > 0) {
                    return response.bodyAsString() // TODO: Parse error message and present better string.
                        .mapThrow(HttpClientResponseException::new);
                }
                return Future.failure(response.reject("Failed to unregister service \"" + serviceName + "\""));
            });
    }

    /**
     * Builder useful for constructing {@link HttpJsonServiceRegistry} instances.
     */
    public static class Builder {
        private String basePath;
        private HttpClient client;
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
         * Finishes construction of {@link HttpJsonServiceRegistry}.
         *
         * @return New HTTP service registry object.
         */
        public HttpJsonServiceRegistry build() {
            return new HttpJsonServiceRegistry(this);
        }
    }
}
